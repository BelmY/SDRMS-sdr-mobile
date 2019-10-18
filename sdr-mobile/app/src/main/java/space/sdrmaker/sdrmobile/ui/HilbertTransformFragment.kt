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


class HilbertTransformFragment : Fragment() {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var startButton: Button
    private val offset = 200000
    private val samplingRate = 882000

    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_hilbert, container, false)

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
        tvOutput.append("Transform Start\n")
        thread { this.transformThread() }
    }

    private fun transformThread() {
        val sineWaveSource = SineWaveSource(offset, samplingRate, 1024 * 16)
        val hilbert = HilbertTransform(sineWaveSource)
        val file = FileWriter()
        var writePath = "${context!!.getExternalFilesDir(null)}/hilbert_scaled_dump.iq"
        file.write(hilbert, writePath)
        printOnScreen("Transform Finished.")
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}
