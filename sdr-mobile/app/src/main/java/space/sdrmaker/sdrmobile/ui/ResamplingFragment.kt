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
import space.sdrmaker.sdrmobile.dsp.ComplexResampler
import space.sdrmaker.sdrmobile.dsp.IQFileReader
import space.sdrmaker.sdrmobile.dsp.IQFileWriter
import space.sdrmaker.sdrmobile.dsp.NOAA_TAPS
import kotlin.concurrent.thread

class ResamplingFragment : Fragment() {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var resampleButton: Button

    private val handler = Handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_resampling, container, false)

        // setup UI
        resampleButton = root.findViewById(R.id.resampleButton)
        resampleButton.setOnClickListener {
            resampleFile()
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun resampleFile() {
        thread {
            printOnScreen("Resampling started.\n")
            var readPath = "${context!!.getExternalFilesDir(null)}/noaa_sample.iq"
            var writePath =
                "${context!!.getExternalFilesDir(null)}/noaa_sample_resampled.iq"
            val reader = IQFileReader(readPath)
            val resampler = ComplexResampler(reader, 2, 3, NOAA_TAPS, NOAA_TAPS)

            val writer = IQFileWriter()
            writer.write(resampler, writePath)
            printOnScreen("Resampling finished.\n")
        }
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}