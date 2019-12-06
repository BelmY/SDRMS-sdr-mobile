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
import space.sdrmaker.sdrmobile.noaa.NOAA_FREQUENCIES
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread


class PlotsFragment : Fragment(), HackrfCallbackInterface {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var recButton: Button
    private lateinit var startButton: Button
    private lateinit var fftPlot: FFTView
    private lateinit var waterfallPlot: WaterfallView

    private lateinit var hackrf: Hackrf
    private lateinit var hackRFSignalSource: HackRFSignalSource
//    private var channelFreq = 89800000L
//    private var channelFreq = 137912500L // NOAA 18
//    private var channelFreq = 137100000L // NOAA 19
//    private var channelFreq = 137620000L // NOAA 15
    private var noaa = 15
    private var channelFreq = NOAA_FREQUENCIES[noaa] ?: 137620000L // NOAA 15

    private var offset = 200000
    private var centerFreq = channelFreq + offset
    private var samplingRate = 832000
    private var bandwidth = samplingRate

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
        recButton = root.findViewById(R.id.recButton)
        startButton = root.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            startRX()
        }
        fftPlot = root.fftPlot
        waterfallPlot = root.waterfallPlot

        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        setUIState(UIState.INITIALIZED)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun setupFFTPlotAxis() {
        fftPlot.setupXAxis(channelFreq.toFloat() / 1000000, bandwidth.toFloat() / 1000000, ticks = getFFTXTicks(5))
        fftPlot.setupYAxis()
    }

    private fun getFFTXTicks(numTicks: Int): FloatArray {
        val start = channelFreq.toFloat() / 1000000 - bandwidth.toFloat() / 2000000
        val stop = channelFreq.toFloat() / 1000000 + bandwidth.toFloat() / 2000000
        val step = (stop - start) / (numTicks - 1)
        return FloatArray(numTicks) {i -> start + i * step}
    }

    private fun startRX() {
        tvOutput.append("RX Start\n")
        setupFFTPlotAxis()
        initHackrf()
    }

    private fun stopRX() {
        tvOutput.append("\nRX Stop\n")

        stopRequested = true
        Thread.sleep(10)
        hackRFSignalSource.stop()
        Toast.makeText(
            context,
            "File saved to ${context!!.getExternalFilesDir(null)}",
            Toast.LENGTH_LONG
        ).show()
        setUIState(UIState.INITIALIZED)
    }

    private fun initHackrf() {
        val queueSize = samplingRate * 2    // buffer 1 second
        if (!Hackrf.initHackrf(context, this, queueSize)) {
            tvOutput.append("HackRF initialization failed.\n")
            setUIState(UIState.INITIALIZED)
        }
    }

    private fun setUIState(state: UIState) {
        when (state) {
            UIState.STARTED -> {
                recButton.isEnabled = true
                startButton.isEnabled = false
                stopRequested = false
            }
            UIState.INITIALIZED -> {
                recButton.isEnabled = false
                startButton.isEnabled = true
                stopRequested = false
                startButton.text = "RX Start"
                startButton.setOnClickListener { startRX() }
            }
            UIState.RECEIVING -> {
                recButton.isEnabled = false
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
        thread {
            setUIState(UIState.RECEIVING)
            receiveThread()
        }
    }

    override fun onHackrfError(message: String) {
        tvOutput.append("Error while opening HackRF: $message\n")  // FIXME: message not displayed
        setUIState(UIState.INITIALIZED)
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
            while (!stopRequested) {
                queueSink.write(multiply.next())
            }
        }

        // plotting pipeline
        val fftQueueSource = QueueSource(fftQueue)
        val fft = ComplexFFT(fftQueueSource, 4096)
        thread {
            while (!stopRequested) {
                fftPlot.write(fft.next())
            }
        }

        // FM audio pipeline
        val fmQueueSource = QueueSource(fmQueue)
        val filter = ComplexFIRFilter(fmQueueSource, SR832k_20k_189)
        val fmDemodulator = FMDemodulator(filter, 5000)
        val audioDecimator = FIRFilter(fmDemodulator, CUT20k_FREQ441000_81, 20, 10f)
        val audioSink = AudioSink(41600)
        thread {
            while (!stopRequested && audioDecimator.hasNext()) {
                audioSink.write(audioDecimator.next())
            }
        }
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
