package space.sdrmaker.sdrmobile.ui

import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.mantz_it.hackrf_android.Hackrf
import com.mantz_it.hackrf_android.HackrfCallbackInterface
import kotlinx.android.synthetic.main.fragment_noaarcv.view.*
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.*
import space.sdrmaker.sdrmobile.dsp.taps.*
import space.sdrmaker.sdrmobile.noaa.NOAA_FREQUENCIES
import java.sql.Timestamp
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread
import android.widget.ArrayAdapter


class NOAARcvFragment : Fragment(), HackrfCallbackInterface {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var recButton: Button
    private lateinit var rxButton: Button
    private lateinit var fftPlot: FFTView
    private lateinit var uiState: UIState
    private lateinit var noaaSelector: Spinner

    private lateinit var hackrf: Hackrf
    private lateinit var hackRFSignalSource: HackRFSignalSource
    private lateinit var fileWriter: FileWriter
    private var fileQueue = ArrayBlockingQueue<FloatArray>(1024)
    private var noaa = 15
    private var channelFreq = NOAA_FREQUENCIES[noaa] ?: 137620000L // NOAA 15

    private val offset = 200000
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
        root = inflater.inflate(R.layout.fragment_noaarcv, container, false)

        // setup UI
        recButton = root.recButton
        rxButton = root.rxButton
        rxButton.setOnClickListener {
            startRX()
        }
        fftPlot = root.fftPlot
        noaaSelector = root.noaaSelector
        val spinnerArrayAdapter = ArrayAdapter<String>(
            context!!, android.R.layout.simple_spinner_item,
            arrayOf("NOAA 15", "NOAA 18", "NOAA 19")
        )
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        noaaSelector.adapter = spinnerArrayAdapter
        noaaSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> setNOAA(15)
                    1 -> setNOAA(18)
                    2 -> setNOAA(19)
                    else -> setNOAA(15)
                }
            }
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        setUIState(UIState.STARTED)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun setNOAA(num: Int) {
        noaa = num
        channelFreq = NOAA_FREQUENCIES[noaa] ?: 137620000L // NOAA 15
        centerFreq = channelFreq + offset

    }

    private fun setupFFTPlotAxis() {
        fftPlot.setupXAxis(
            channelFreq.toFloat() / 1000000,
            bandwidth.toFloat() / 1000000,
            ticks = getFFTXTicks(5)
        )
        fftPlot.setupYAxis()
    }

    private fun getFFTXTicks(numTicks: Int): FloatArray {
        val start = channelFreq.toFloat() / 1000000 - bandwidth.toFloat() / 2000000
        val stop = channelFreq.toFloat() / 1000000 + bandwidth.toFloat() / 2000000
        val step = (stop - start) / (numTicks - 1)
        return FloatArray(numTicks) { i -> start + i * step }
    }

    private fun startRX() {
        tvOutput.append("RX Start\n")
        setupFFTPlotAxis()
        initHackrf()
    }

    private fun stopRX() {
        tvOutput.append("\nRX Stop\n")

        stopRequested = true
        Thread.sleep(100)
        if (uiState == UIState.RECORDING) {
            stopRec()
        }
        hackRFSignalSource.stop()
        setUIState(UIState.STARTED)
    }

    private fun initHackrf() {
        val queueSize = samplingRate * 2    // buffer 1 second
        if (!Hackrf.initHackrf(context, this, queueSize)) {
            tvOutput.append("HackRF initialization failed.\n")
            setUIState(UIState.STARTED)
        }
    }

    private fun startRec() {
        fileQueue.clear()
        val fileQueueSource = QueueSource(fileQueue)
        val fileDecimator = FIRFilter(fileQueueSource, SR832k_7k_9k_283t, 100)
        val writePath =
            "${context!!.getExternalFilesDir(null)}/NOAA${noaa}-${Timestamp(System.currentTimeMillis())}-8320Sps.iq"
        fileWriter = FileWriter(writePath)
        setUIState(UIState.RECORDING)
        thread {
            while (uiState == UIState.RECORDING) {
                fileWriter.write(fileDecimator.next())
            }
        }
    }

    private fun stopRec() {
        fileWriter.close()
        Toast.makeText(
            context,
            "File saved to ${context!!.getExternalFilesDir(null)}",
            Toast.LENGTH_LONG
        ).show()
        setUIState(UIState.RECEIVING)
    }

    private fun setUIState(state: UIState) {
        uiState = state
        when (state) {
            UIState.STARTED -> {
                stopRequested = false

                recButton.isEnabled = false
                recButton.text = "REC Start"
                recButton.setOnClickListener { startRec() }

                rxButton.isEnabled = true
                rxButton.text = "RX Start"
                rxButton.setOnClickListener { startRX() }
            }
            UIState.RECEIVING -> {
                recButton.isEnabled = true
                recButton.text = "REC Start"
                recButton.setOnClickListener { startRec() }

                rxButton.isEnabled = true
                rxButton.text = "RX Stop"
                rxButton.setOnClickListener { stopRX() }
            }
            UIState.RECORDING -> {
                recButton.isEnabled = true
                recButton.text = "REC Stop"
                recButton.setOnClickListener { stopRec() }

                rxButton.isEnabled = true
                rxButton.text = "RX Stop"
                rxButton.setOnClickListener { stopRX() }
            }
        }
    }

    override fun onHackrfReady(hackrf: Hackrf) {
        tvOutput.append("HackRF is ready!\n")
        this.hackrf = hackrf
        hackRFSignalSource = HackRFSignalSource(hackrf)
        setUIState(UIState.RECEIVING)
        thread { receiveThread() }
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

        // FM pipeline
        val fmQueueSource = QueueSource(fmQueue)
        val filter =
            ComplexFIRFilter(fmQueueSource, SR832k_20k_189)
        val fmDemodulator = FMDemodulator(filter, 5000)

        val audioQueue = ArrayBlockingQueue<FloatArray>(1024)
        fileQueue = ArrayBlockingQueue(1024)
        val fmSink = QueueSink(audioQueue, fileQueue)

        // FM thread
        thread {
            while (!stopRequested) {
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
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
