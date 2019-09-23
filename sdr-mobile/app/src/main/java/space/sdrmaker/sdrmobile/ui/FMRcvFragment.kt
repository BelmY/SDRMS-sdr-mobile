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
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.*
import kotlin.concurrent.thread

class FMRcvFragment : Fragment(), HackrfCallbackInterface {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var initButton: Button
    private lateinit var startButton: Button
    private lateinit var hackrf: Hackrf
    private var centerFreq = 95700000L
    private var bandwidth = 300000
    private var samplingRate = 1102500
    private var lnaGain = 32
    private var vgaGain = 32
    private var amp = false
    private var antennaPower = false

    var stopRequested = false
    private val handler = Handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_rx, container, false)

        // setup UI
        initButton = root.findViewById(R.id.initButton)
        initButton.setOnClickListener {
            initHackrf()
        }
        startButton = root.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            startRX()
        }
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
        setUIState(UIState.INITIALIZED)
    }

    override fun onHackrfError(message: String) {
        tvOutput.append("Error while opening HackRF: $message\n")  // FIXME: message not displayed
        setUIState(UIState.STARTED)
    }

    private fun receiveThread() {
        setUIState(UIState.RECEIVING)
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

        val hackRFSource = HackRFSignalSource(hackrf) {msg -> printOnScreen(msg)}
        printOnScreen("RX Started\n")
        while (!stopRequested) {
            val fmDemodulator = FMDemodulator(hackRFSource, 75000, ModulationType.WFM)
            val resampler = Decimator(fmDemodulator, 25, FM_TAPS)
            val audioSink = AudioSink()
            audioSink.write(resampler) { msg -> printOnScreen(msg)}
        }
        setUIState(UIState.INITIALIZED)
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }


}