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
import space.sdrmaker.sdrmobile.benchmarks.utils.conversionsBenchmark
import space.sdrmaker.sdrmobile.benchmarks.utils.opsPerSecond

class ConversionsFragment : Fragment() {

    private lateinit var root: View

    private var conversionsToPerform = 1000000

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
        // perform JVM conversion benchmark
        val jvmTotalTimes = conversionsBenchmark(conversionsToPerform)
        val jvmConversionsPerSecond = longArrayOf(
            opsPerSecond(conversionsToPerform, jvmTotalTimes[0]),
            opsPerSecond(conversionsToPerform, jvmTotalTimes[1]),
            opsPerSecond(conversionsToPerform, jvmTotalTimes[2])
        )
        val jvmResultLabel =
            "JVM short -> float total time: ${jvmTotalTimes[0]}ms\n" +
                    "JVM short -> float conversions/s: ${jvmConversionsPerSecond[0]}\n\n" +
                    "JVM float -> short total time: ${jvmTotalTimes[1]}ms\n" +
                    "JVM float -> short conversions/s: ${jvmConversionsPerSecond[1]}\n\n" +
                    "JVM short -> complex total time: ${jvmTotalTimes[2]}ms\n" +
                    "JVM short -> complex conversions/s: ${jvmConversionsPerSecond[2]}"

        // perform NDK conversion benchmark
        val ndkShortFloatTotalTime = NativeUtils.ndkShortFloatConversionBenchmark(conversionsToPerform)
        val ndkShortFloatConversionsPerSecond = opsPerSecond(conversionsToPerform, ndkShortFloatTotalTime)

        val ndkFloatShortTotalTime = NativeUtils.ndkFloatShortConversionBenchmark(conversionsToPerform)
        val ndkFloatShortConversionsPerSecond = opsPerSecond(conversionsToPerform, ndkFloatShortTotalTime)

        val ndkShortComplexTotalTime = NativeUtils.ndkShortComplexConversionBenchmark(conversionsToPerform)
        val ndkShortComplexConversionsPerSecond = opsPerSecond(conversionsToPerform, ndkShortComplexTotalTime)

        val ndkResultLabel =
            "NDK short -> float total time: ${ndkShortFloatTotalTime}ms\n" +
                    "NDK short -> float conversions/s: $ndkShortFloatConversionsPerSecond\n\n" +
                    "NDK float -> short total time: ${ndkFloatShortTotalTime}ms\n" +
                    "NDK float -> short conversions/s: $ndkFloatShortConversionsPerSecond\n\n" +
                    "NDK short -> complex total time: ${ndkShortComplexTotalTime}ms\n" +
                    "NDK short -> complex conversions/s: $ndkShortComplexConversionsPerSecond"

        setResultText("$jvmResultLabel\n\n$ndkResultLabel")
    }

    private fun setResultText(result: String) {
        root.findViewById<TextView>(R.id.conversionsResultText).text = result
    }

}