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
        root = inflater.inflate(R.layout.fragment_audio, container, false)

        // setup UI
        playButton = root.findViewById(R.id.playButton)
        playButton.setOnClickListener {
            playSineWave()
        }
        tvOutput = root.findViewById<Button>(R.id.tvOutput)
        tvOutput.movementMethod = ScrollingMovementMethod()
        tvOutput.append("Ready...\n")

        return root
    }

    private fun playSineWave() {
        thread {
            printOnScreen("OldAudioSink test started.\n")
//            val sink = OldAudioSink()
//            val sine = SineWaveSource(440)
//            sink.write(sine) {msg -> println(msg)}
            printOnScreen("OldAudioSink test finished.\n")
        }
    }

    private fun printOnScreen(msg: String) {
        handler.post { tvOutput.append(msg) }
    }

}