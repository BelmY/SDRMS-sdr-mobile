package space.sdrmaker.sdrmobile.ui

import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mantz_it.hackrf_android.Hackrf
import com.mantz_it.hackrf_android.HackrfCallbackInterface
import kotlinx.android.synthetic.main.fragment_plots.view.*
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.*
import space.sdrmaker.sdrmobile.dsp.taps.*
import java.sql.Timestamp
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread


class NOAARcvFragment : Fragment(), HackrfCallbackInterface {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var initButton: Button
    private lateinit var startButton: Button
    private lateinit var fftPlot: FFTView
    private lateinit var waterfallPlot: WaterfallView

    private lateinit var hackrf: Hackrf
    private lateinit var hackRFSignalSource: HackRFSignalSource
    private lateinit var fileWriter: FileWriter
    private var channelFreq = 89800000L
//    private var channelFreq = 137912500L // NOAA 18
//    private var channelFreq = 137100000L // NOAA 19
//    private var channelFreq = 137620000L // NOAA 15
    private var offset = 200000
    private var centerFreq = channelFreq + offset
    private var samplingRate = 832000
    private var bandwidth = samplingRate
    private var audioDecimation = 10
    private val lowpassDecimation = samplingRate / AUDIO_SAMPLE_RATE / audioDecimation

    private var lnaGain = 32
    private var vgaGain = 16
    private var amp = false
    private var antennaPower = false

    private var stopRequested = false
    private val handler = Handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_plots, container, false)

        // setup UI
        initButton = root.findViewById(R.id.initButton)
        initButton.setOnClickListener {
            initHackrf()
        }
        startButton = root.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            startRX()
        }
        fftPlot = root.fftPlot
        waterfallPlot = root.waterfallPlot

        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        setUIState(UIState.STARTED)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun startRX() {
        tvOutput.append("RX Start\n")
        thread { this.receiveThread() }
    }

    private fun stopRX() {
        tvOutput.append("\nRX Stop\n")

        stopRequested = true
        Thread.sleep(10)
        hackRFSignalSource.stop()
        fileWriter.close()
        Toast.makeText(context,"File saved to ${context!!.getExternalFilesDir(null)}",Toast.LENGTH_LONG).show()
        setUIState(UIState.INITIALIZED)
    }

    private fun initHackrf() {
        val queueSize = samplingRate * 2    // buffer 1 second
        if (!Hackrf.initHackrf(context, this, queueSize)) {
            tvOutput.append("HackRF initialization failed.\n")
            setUIState(UIState.STARTED)
        }
    }

    private fun setUIState(state: UIState) {
        when (state) {
            UIState.STARTED -> {
                initButton.isEnabled = true
                startButton.isEnabled = false
                stopRequested = false
            }
            UIState.INITIALIZED -> {
                initButton.isEnabled = false
                startButton.isEnabled = true
                stopRequested = false
                startButton.text = "RX Start"
                startButton.setOnClickListener { startRX() }
            }
            UIState.RECEIVING -> {
                initButton.isEnabled = false
                startButton.isEnabled = true
                startButton.text = "RX Stop"
                startButton.setOnClickListener {
                    stopRX()
                }
            }
        }
    }

    override fun onHackrfReady(hackrf: Hackrf) {
        tvOutput.append("HackRF is ready!\n")
        this.hackrf = hackrf
        hackRFSignalSource = HackRFSignalSource(hackrf)

        setUIState(UIState.INITIALIZED)
    }

    override fun onHackrfError(message: String) {
        tvOutput.append("Error while opening HackRF: $message\n")  // FIXME: message not displayed
        setUIState(UIState.STARTED)
    }

    private fun setupHackRF() {
        printOnScreen("Setting sample rate to $samplingRate sps ... ")
        hackrf.setSampleRate(samplingRate, 1)
        printOnScreen("ok.\nSetting center requency to $centerFreq Hz ... ")
        hackrf.setFrequency(centerFreq)
        printOnScreen("ok.\nSetting baseband filter bandwidth to $bandwidth Hz ... ")
        hackrf.setBasebandFilterBandwidth(bandwidth)
        printOnScreen("ok.\nSetting RX VGA Gain to $vgaGain ... ")
        hackrf.setRxVGAGain(vgaGain)
        printOnScreen("ok.\nSetting LNA Gain to $lnaGain ... ")
        hackrf.setRxLNAGain(lnaGain)
        printOnScreen("ok.\nSetting Amplifier to $amp ... ")
        hackrf.setAmp(amp)
        printOnScreen("ok.\nSetting Antenna Power to $antennaPower ... ")
        hackrf.setAntennaPower(antennaPower)
        printOnScreen("ok.\n\n")
    }

    private fun receiveThread() {
        setUIState(UIState.RECEIVING)
        setupHackRF()
        val sineWaveSource = ComplexSineWaveSource(offset, samplingRate, 1024 * 16)
        val multiply = ComplexMultiply(sineWaveSource, hackRFSignalSource)

        val fmQueue = ArrayBlockingQueue<FloatArray>(1024)
        val fftQueue = ArrayBlockingQueue<FloatArray>(1024)
        val queueSink = QueueSink(fmQueue, fftQueue)

        // Data thread
        thread {
            while(!stopRequested) {
                queueSink.write(multiply.next())
            }
        }

        // plotting pipeline
        val fftQueueSource = QueueSource(fftQueue)
        val fft = ComplexFFT(fftQueueSource, 4096)
        thread {
            while(!stopRequested) {
                fftPlot.write(fft.next())
            }
        }

        // FM pipeline
        val fmQueueSource = QueueSource(fmQueue)
        val filter =
            ComplexFIRFilter(fmQueueSource, SR832k_20k_189)
        val fmDemodulator = FMDemodulator(filter, 5000)

        val audioQueue = ArrayBlockingQueue<FloatArray>(1024)
        val fileQueue = ArrayBlockingQueue<FloatArray>(1024)
        val fmSink = QueueSink(audioQueue, fileQueue)

        // FM thread
        thread {
            while(!stopRequested) {
                fmSink.write(fmDemodulator.next())
            }
        }

        val audioQueueSource = QueueSource(audioQueue)
        val audioDecimator = FIRFilter(audioQueueSource, CUT20k_FREQ441000_81, 20, 10f)
        val audioSink = AudioSink(41600)
        thread {
            while (!stopRequested && audioDecimator.hasNext()) {
                audioSink.write(audioDecimator.next())
            }
        }

        // file pipeline
        val fileQueueSource = QueueSource(fileQueue)
        val fileDecimator = FIRFilter(fileQueueSource, SR832k_7k_9k_283t, 100)
        val writePath = "${context!!.getExternalFilesDir(null)}/NOAA-${Timestamp(System.currentTimeMillis())}-8320kHz.iq"
        fileWriter = FileWriter(writePath)
        thread {
            while (!stopRequested) {
                fileWriter.write(fileDecimator.next())
            }
        }

    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
