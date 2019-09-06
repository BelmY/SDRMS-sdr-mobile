package space.sdrmaker.sdrmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import com.mantz_it.hackrf_android.HackrfCallbackInterface
import kotlinx.android.synthetic.main.activity_main.*
import com.mantz_it.hackrf_android.Hackrf
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import android.text.method.ScrollingMovementMethod


enum class UIState {
    STARTED, INITIALIZED, RECEIVING
}

class MainActivity : AppCompatActivity(), HackrfCallbackInterface {

    // UI
    private lateinit var initButton: Button
    private lateinit var startButton: Button
    private val handler = Handler()

    // SDR
    private lateinit var hackrf: Hackrf
    private var centerFreq = 137000000L  // 137MHz (NOAA frequency)
    private var bandwidth = 46000 // 46kHz
    private var samplingRate = 100000 // 100kHz
    private var lnaGain = 32
    private var vgaGain = 32
    private var amp = false
    private var antennaPower = false

    var stopRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI setup
        initButton = findViewById(R.id.initButton)
        initButton.setOnClickListener {
            initHackrf()
        }
        startButton = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            startRX()
        }
        tvOutput.movementMethod = ScrollingMovementMethod()
        setUIState(UIState.STARTED)
        tvOutput.append("Ready...\n")
    }

    private fun startRX() {
        tvOutput.append("RX Start\n")
        thread {this.receiveThread()}
    }

    private fun stopRX() {
        tvOutput.append("\nRX Stop\n")
        stopRequested = true
    }

    private fun initHackrf() {
        val context = applicationContext
        val queueSize = samplingRate * 2    // buffer 1 second
        if(!Hackrf.initHackrf(context, this, queueSize)) {
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

        printOnScreen("RX Started\n")
        val queue = hackrf.startRX()

        // Run until user hits the 'Stop' button
        var i = 0
        var lastTransceiverPacketCounter = 0L
        var lastTransceivingTime = 0L
        while (!stopRequested) {
            i++    // only for statistics

            // Grab one packet from the top of the queue. Will block if queue is
            // empty and timeout after one second if the queue stays empty.
            val receivedBytes = queue.poll(1000, TimeUnit.MILLISECONDS)

            /*  HERE should be the DSP portion of the app. The receivedBytes
             *  variable now contains a byte array of size hackrf.getPacketSize().
             *  This is currently set to 16KB, but may change in the future.
             *  The bytes are interleaved, 8-bit, signed IQ samples (in-phase
             *  component first, followed by the quadrature component):
             *
             *  [--------- first sample ----------]   [-------- second sample --------]
             *         I                  Q                  I                Q ...
             *  receivedBytes[0]   receivedBytes[1]   receivedBytes[2]       ...
             *
             *  Note: Make sure you read from the queue fast enough, because if it runs
             *  full, the hackrf_android library will abort receiving and go back to
             *  OFF mode.
             */

            //
            if (receivedBytes != null) {
                printOnScreen(".")

                // IMPORTANT: After we used the receivedBytes buffer and don't need it any more,
                // we should return it to the buffer pool of the hackrf! This will save a lot of
                // allocation time and the garbage collector won't go off every second.
                hackrf.returnBufferToBufferPool(receivedBytes)
            } else {
                printOnScreen("Error: buffer underrun, RX stopped\n")
                // TODO: update UI state
                break
            }

            // print statistics
            if (i.rem(100) == 0) {
                val bytes =
                    (hackrf.transceiverPacketCounter - lastTransceiverPacketCounter) * hackrf.packetSize
                val time = (hackrf.transceivingTime - lastTransceivingTime) / 1000.0
                printOnScreen(
                    String.format(
                        "\nCurrent Transfer Rate: %4.1f MB/s\n",
                        bytes / time / 1000000.0
                    )
                )
                printOnScreen("I: ${receivedBytes[0].toInt()} Q: ${receivedBytes[1].toInt()}")
                printOnScreen("I: ${receivedBytes[2].toInt()} Q: ${receivedBytes[3].toInt()}\n")
                printOnScreen("${receivedBytes[0].javaClass.canonicalName}\n")
                lastTransceiverPacketCounter = hackrf.transceiverPacketCounter
                lastTransceivingTime = hackrf.transceivingTime
            }
        }
        setUIState(UIState.INITIALIZED)
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
