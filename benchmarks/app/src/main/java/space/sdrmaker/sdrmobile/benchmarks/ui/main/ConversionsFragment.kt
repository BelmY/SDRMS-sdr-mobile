package space.sdrmaker.sdrmobile.benchmarks.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import space.sdrmaker.sdrmobile.benchmarks.NativeUtils
import space.sdrmaker.sdrmobile.benchmarks.R
import kotlin.math.round

class ConversionsFragment : Fragment() {

    private lateinit var root: View

    private var conversionsToPerform = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_conversions, container, false)

        // setup conversions button listener
        root.findViewById<Button>(R.id.conversionsButton).setOnClickListener {
            onConversionsButtonClick()
        }

        return root
    }

    private fun onConversionsButtonClick() {
        // TODO: perform JVM conversions benchmark
        // TODO: perform NDK conversions benchmark
    }

    private fun setResultText(result: String) {
        root.findViewById<TextView>(R.id.convolutionResultText).text = result
    }

}