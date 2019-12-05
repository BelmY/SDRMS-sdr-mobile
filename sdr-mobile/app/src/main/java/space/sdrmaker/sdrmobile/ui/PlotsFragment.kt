package space.sdrmaker.sdrmobile.ui

import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mantz_it.hackrf_android.Hackrf
import com.mantz_it.hackrf_android.HackrfCallbackInterface
import kotlinx.android.synthetic.main.fragment_plots.view.*
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.*
import space.sdrmaker.sdrmobile.dsp.taps.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread


class PlotsFragment : Fragment(), HackrfCallbackInterface {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var initButton: Button
    private lateinit var startButton: Button
    private lateinit var fftPlot: FFTView
    private lateinit var waterfallPlot: WaterfallView

    private lateinit var hackrf: Hackrf
    private lateinit var hackRFSignalSource: HackRFSignalSource
//    private var channelFreq = 89800000L
    private var channelFreq = 137912500L // NOAA 18
//    private var channelFreq = 137100000L // NOAA 19
//    private var channelFreq = 137620000L // NOAA 15

    private var offset = 200000
    private var centerFreq = channelFreq + offset
//    private var samplingRate = 882000
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

        // FM pipeline
        val fmQueueSource = QueueSource(fmQueue)
        val rfDecimator =
            ComplexFIRFilter(fmQueueSource, CUT75k_FREQ882000_45, lowpassDecimation, 4f)
        val fmDemodulator = FMDemodulator(rfDecimator, 7500)
        val audioDecimator =
            FIRFilter(fmDemodulator, CUT20k_FREQ441000_81, audioDecimation, 10f)
        val audioSink = AudioSink()
        thread {
            while (!stopRequested && audioDecimator.hasNext()) {
                audioSink.write(audioDecimator.next())
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
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
