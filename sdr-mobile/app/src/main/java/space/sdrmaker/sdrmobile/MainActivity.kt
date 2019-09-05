package space.sdrmaker.sdrmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import com.mantz_it.hackrf_android.HackrfCallbackInterface
import kotlinx.android.synthetic.main.activity_main.*
import com.mantz_it.hackrf_android.Hackrf


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
            tvOutput.append("RX Start pressed\n")
        }

        tvOutput.append("Ready...\n")
    }

    private fun initHackrf() {
        val context = applicationContext
        val queueSize = samplingRate * 2    // buffer 1 second
        if(!Hackrf.initHackrf(context, this, queueSize)) {
            val queueSize = samplingRate * 2    // buffer 1 second
        }
    }

    override fun onHackrfReady(hackrf: Hackrf) {
        tvOutput.append("HackRF is ready!\n")
        this.hackrf = hackrf
        // TODO: set appropriate UI state
    }

    override fun onHackrfError(message: String) {
        tvOutput.append("Error while opening HackRF: $message\n")  // FIXME: message not displayed
        // TODO: set appropriate UI state
    }

    private fun receiveThread() {
        printOnScreen("Setting sample rate to $samplingRate sps")
        hackrf.setSampleRate(samplingRate, 1)
        printOnScreen("ok.\nSetting center requency to $centerFreq Hz")
        hackrf.setFrequency(centerFreq)
        printOnScreen("ok.\nSetting baseband filter bandwidth to $bandwidth Hz")
        hackrf.setBasebandFilterBandwidth(bandwidth)
        printOnScreen("ok.\nSetting RX VGA Gain to $vgaGain")
        hackrf.setRxVGAGain(vgaGain);
        printOnScreen("ok.\nSetting LNA Gain to $lnaGain")
        hackrf.setRxLNAGain(lnaGain)
        printOnScreen("ok.\nSetting Amplifier to $amp")
        hackrf.setAmp(amp)
        printOnScreen("ok.\nSetting Antenna Power to $antennaPower")
        hackrf.setAntennaPower(antennaPower)
        printOnScreen("ok.\n\n")
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
