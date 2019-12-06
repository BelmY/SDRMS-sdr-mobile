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
import space.sdrmaker.sdrmobile.R
import space.sdrmaker.sdrmobile.dsp.*
import space.sdrmaker.sdrmobile.dsp.taps.*
import kotlin.concurrent.thread


class NOAAFragment : Fragment() {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var startButton: Button

    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_noaa, container, false)

        // setup UI
        startButton = root.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            start()
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun start() {
        thread { this.transformThread() }
    }

    private fun transformThread() {
        printOnScreen("NOAA demodulation started.\n")
        val readPath = "${context!!.getExternalFilesDir(null)}/NOAA-2019-12-06 10:04:05.014-8320Sps.iq"
        val writePath = "${context!!.getExternalFilesDir(null)}/NOAA-2019-12-06 10:04:05.014-8320Sps.px"

        val reader = FileReader(readPath, blockSize = 16 * 1000)
        val hilbert = HilbertTransform(reader)
        val amDemodulator = AMDemodulator(hilbert)
        val decimator = FIRFilter(amDemodulator, SR8320_2000_2100_75t, 2)
        val normalizer = Normalizer(decimator, 0f, 1f)
        val syncer = NOAALineSyncer(normalizer)
        val image = NOAAImageSink(syncer, verbose = true)

        val writer = FileWriter(writePath)
        writer.write(image.write())
        writer.close()
        printOnScreen("NOAA decoding finished.\n")
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
