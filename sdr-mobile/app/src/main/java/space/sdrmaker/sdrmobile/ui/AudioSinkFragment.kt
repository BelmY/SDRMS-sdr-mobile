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
import kotlin.concurrent.thread

class AudioSinkFragment: Fragment() {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var playButton: Button

    private val handler = Handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_fmdemod, container, false)

        // setup UI
        playButton = root.findViewById(R.id.demodButton)
        playButton.setOnClickListener {
            demodulateFile()
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun demodulateFile() {
        thread {
            printOnScreen("FM demodulation started.\n")
            var readPath = "${context!!.getExternalFilesDir(null)}/fm.iq"
//            var writePath =
//                "${context!!.getExternalFilesDir(null)}/fm-demod.raw"
            val reader = IQFileReader(readPath)
            val demodulator = FMDemodulator(reader, 75000, ModulationType.WFM)

//            val writer = RawFileWriter()
//            writer.write(demodulator, writePath)
            val sink = AudioSink()
            val sine = SineWaveSource(440)
//            sink.write(sine)
            printOnScreen("FM demodulation finished.\n")
        }
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}