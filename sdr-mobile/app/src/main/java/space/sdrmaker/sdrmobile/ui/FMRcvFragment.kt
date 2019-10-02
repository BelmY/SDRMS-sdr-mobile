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
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception
import kotlin.concurrent.thread


class FMRcvFragment : Fragment(), HackrfCallbackInterface {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var initButton: Button
    private lateinit var startButton: Button
    private lateinit var hackrf: Hackrf
    private var channelFreq = 95700000L
    private var offset = 200000
    private var centerFreq = channelFreq + offset
    private var samplingRate = 882000
    private var bandwidth = samplingRate
    private val decimation = samplingRate / AUDIO_SAMPLE_RATE / 10
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
        root = inflater.inflate(R.layout.fragment_fmrcv, container, false)

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
//        setUIState(UIState.INITIALIZED)
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

//    private fun receiveThread() {
////        setUIState(UIState.RECEIVING)
////        printOnScreen("Setting sample rate to $samplingRate sps ... ")
////        hackrf.setSampleRate(samplingRate, 1)
////        printOnScreen("ok.\nSetting center requency to $centerFreq Hz ... ")
////        hackrf.setFrequency(centerFreq)
////        printOnScreen("ok.\nSetting baseband filter bandwidth to $bandwidth Hz ... ")
////        hackrf.setBasebandFilterBandwidth(bandwidth)
////        printOnScreen("ok.\nSetting RX VGA Gain to $vgaGain ... ")
////        hackrf.setRxVGAGain(vgaGain)
////        printOnScreen("ok.\nSetting LNA Gain to $lnaGain ... ")
////        hackrf.setRxLNAGain(lnaGain)
////        printOnScreen("ok.\nSetting Amplifier to $amp ... ")
////        hackrf.setAmp(amp)
////        printOnScreen("ok.\nSetting Antenna Power to $antennaPower ... ")
////        hackrf.setAntennaPower(antennaPower)
////        printOnScreen("ok.\n\n")
//
//        val hackRFSource = HackRFSignalSource(hackrf)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_raw_hackrf.iq"
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_raw_usrp.iq"
//        val hackRFSource = FileReader(readPath)
//        val sine = ComplexSineWaveSource(offset, samplingRate, 16 * 1024)
//        val multiplier = Multiply(hackRFSource, sine)
//        val downsampler = ComplexDecimator(multiplier, decimation, FM_882k_BLACKMAN)
//        val downsampler = ComplexDecimator(multiplier, decimation, FM_TAPS_2)
//        val fmDemodulator = FMDemodulator(downsampler, 75000, ModulationType.WFM)
//        val audioFilter = FIRFilter(fmDemodulator, AUDIO_TAPS)
//        val endpoint = audioFilter
//        val audioSink = AudioSink()
//        while (!stopRequested && endpoint.hasNext()) {
//            try {
//                audioSink.write(endpoint.next())
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
//        }
//        setUIState(UIState.INITIALIZED)
//    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        printOnScreen("Setting sample rate to $samplingRate sps ... ")
//        hackrf.setSampleRate(samplingRate, 1)
//        printOnScreen("ok.\nSetting center requency to $centerFreq Hz ... ")
//        hackrf.setFrequency(centerFreq)
//        printOnScreen("ok.\nSetting baseband filter bandwidth to $bandwidth Hz ... ")
//        hackrf.setBasebandFilterBandwidth(bandwidth)
//        printOnScreen("ok.\nSetting RX VGA Gain to $vgaGain ... ")
//        hackrf.setRxVGAGain(vgaGain)
//        printOnScreen("ok.\nSetting LNA Gain to $lnaGain ... ")
//        hackrf.setRxLNAGain(lnaGain)
//        printOnScreen("ok.\nSetting Amplifier to $amp ... ")
//        hackrf.setAmp(amp)
//        printOnScreen("ok.\nSetting Antenna Power to $antennaPower ... ")
//        hackrf.setAntennaPower(antennaPower)
//        printOnScreen("ok.\n\n")
//
//        val hackRFSource = HackRFSignalSource(hackrf)
////        var readPath = "${context!!.getExternalFilesDir(null)}/fm_raw_hackrf.iq"
////        var readPath = "${context!!.getExternalFilesDir(null)}/fm_raw_usrp.iq"
////        val hackRFSource = FileReader(readPath)
////        val sine = ComplexSineWaveSource(offset, samplingRate, 16 * 1024)
////        val multiplier = Multiply(hackRFSource, sine)
////        val downsampler = ComplexDecimator(multiplier, decimation, FM_882k_BLACKMAN)
////        val downsampler = ComplexDecimator(multiplier, decimation, FM_TAPS_2)
////        val fmDemodulator = FMDemodulator(downsampler, 75000, ModulationType.WFM)
////        val audioFilter = FIRFilter(fmDemodulator, AUDIO_TAPS)
////        val endpoint = audioFilter
////        val audioSink = AudioSink()
//        val fileWriter = FileWriter()
////        while (!stopRequested) {
//            try {
//                fileWriter.write(
//                    hackRFSource,
//                    "${context!!.getExternalFilesDir(null)}/fm_dump.iq"
//                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
////        }
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_dump_wbfm_receive.iq"
//        val file = FileReader(readPath)
//        val audioSink = AudioSink()
//
//        while (!stopRequested && file.hasNext()) {
//        try {
//            audioSink.write(
//                file.next()
//            )
//        } catch (e: Exception) {
//            val sw = StringWriter()
//            val pw = PrintWriter(sw)
//            e.printStackTrace(pw)
//            printOnScreen(sw.toString())
//        }
//        }
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_dump_resampler.iq"
//        val file = FileReader(readPath)
//        val fmDemodulator = FMDemodulator(file, 7500, ModulationType.WFM)
//        val audioSink = AudioSink()
//
//        while (!stopRequested && fmDemodulator.hasNext()) {
//            try {
//                audioSink.write(
//                    fmDemodulator.next()
//                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
//        }
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_dump_resampler_decimation2.iq"
//        val file = FileReader(readPath)
//        val fmDemodulator = FMDemodulator(file, 7500, ModulationType.WFM)
//        val decimator = Decimator(fmDemodulator, 10, CUT22k_FREQ441000)
//        val audioSink = AudioSink()
//
//        while (!stopRequested && decimator.hasNext()) {
//            try {
//                audioSink.write(
//                    decimator.next()
//                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
//        }
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_dump_multiply.iq"
//        val file = FileReader(readPath)
////        val rfDecimator = ComplexDownsampler(file, 2)
//        val rfDecimator = ComplexDecimator(file, 10, CUT200k_FREQ882000)
//        val fmDemodulator = FMDemodulator(rfDecimator, 7500, ModulationType.WFM)
//        val audioDecimator = Decimator(fmDemodulator, 2, CUT22k_FREQ441000)
////        val audioDecimator = fmDemodulator
//        val audioSink = AudioSink()
//        var counter = 0
//        val blockSize = 1024 * 16
//        var timestamp = System.currentTimeMillis()
//        while (!stopRequested && audioDecimator.hasNext()) {
//            try {
//                if (counter++ == 1000) {
//                    val dataRate =
//                        (blockSize.toFloat() / 2) * 10e6 / (System.currentTimeMillis() - timestamp)
//                    println("Data rate: ${dataRate}Sps")
//                    println((System.currentTimeMillis() - timestamp))
//                    timestamp = System.currentTimeMillis()
//                    counter = 0
//                }
//                val next = audioDecimator.next()
//                audioSink.write(
//                    next
//                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
//        }
//
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

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
        val hackRFSignalSource = HackRFSignalSource(hackrf)
//        val rfDecimator = ComplexDecimator(hackRFSignalSource, 2, CUT75k_FREQ882000)
        val rfDecimator = ComplexFIRFilter(hackRFSignalSource, CUT75k_FREQ882000, 2)
        val fmDemodulator = FMDemodulator(rfDecimator, 7500, ModulationType.WFM)
//        val audioDecimator = Decimator(fmDemodulator, 10, CUT22k_FREQ441000)
        val audioDecimator = FIRFilter(fmDemodulator, CUT22k_FREQ441000, 10)
        val audioSink = AudioSink()
        var counter = 0
        val blockSize = 1024 * 16
        var timestamp = System.currentTimeMillis()
        while (!stopRequested && audioDecimator.hasNext()) {
//            try {
//                if (counter++ == 1000) {
//                    val dataRate =
//                        (blockSize.toFloat() / 2) * 10e6 / (System.currentTimeMillis() - timestamp)
//                    println("Data rate: ${dataRate}Sps")
//                    println((System.currentTimeMillis() - timestamp))
//                    timestamp = System.currentTimeMillis()
//                    counter = 0
//                }
                audioSink.write(
                    audioDecimator.next()
                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
        }
        hackrf.stop()
        printOnScreen("RX Stopped.")
        setUIState(UIState.INITIALIZED)
    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_dump_resampler.iq"
//        val file = FileReader(readPath)
//        val fmDemodulator = FMDemodulator(file, 7500, ModulationType.WFM)
//        val audioFilter = FIRFilter(fmDemodulator, AUDIO_TAPS)
//        val audioSink = AudioSink()
//
//        while (!stopRequested && audioFilter.hasNext()) {
//            try {
//                audioSink.write(
//                    audioFilter.next()
//                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
//        }
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

//    private fun receiveThread() {
//        setUIState(UIState.RECEIVING)
//        var readPath = "${context!!.getExternalFilesDir(null)}/fm_dump_multiply.iq"
//        val file = FileReader(readPath)
//        val downsampler = ComplexDecimator(file, decimation, FM_882k_BLACKMAN)
//        val fmDemodulator = FMDemodulator(downsampler, 7500, ModulationType.WFM)
//        val audioSink = AudioSink()
//
//        while (!stopRequested && fmDemodulator.hasNext()) {
//            try {
//                audioSink.write(
//                    fmDemodulator.next()
//                )
//            } catch (e: Exception) {
//                val sw = StringWriter()
//                val pw = PrintWriter(sw)
//                e.printStackTrace(pw)
//                printOnScreen(sw.toString())
//            }
//        }
//        printOnScreen("RX Stopped.")
//        setUIState(UIState.INITIALIZED)
//    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
