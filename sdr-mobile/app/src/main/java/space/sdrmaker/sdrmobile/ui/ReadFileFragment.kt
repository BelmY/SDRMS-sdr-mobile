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
import space.sdrmaker.sdrmobile.dsp.IQFileReader
import kotlin.concurrent.thread

class ReadFileFragment: Fragment() {

    private lateinit var root: View
    private lateinit var tvOutput: TextView
    private lateinit var fileButton: Button

    private val handler = Handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_readfile, container, false)

        // setup UI
        fileButton = root.findViewById(R.id.fileButton)
        fileButton.setOnClickListener {
            readFile()
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

        private fun readFile() {
        thread {
            var path = "${context!!.getExternalFilesDir(null)}/iqfile.iq"
            val reader = IQFileReader(path)
            while (reader.hasNext()) {
                val iq = reader.next()
                printOnScreen("(${iq.first};${iq.second})\n")
                Thread.sleep(300)
            }
        }
    }

   private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}