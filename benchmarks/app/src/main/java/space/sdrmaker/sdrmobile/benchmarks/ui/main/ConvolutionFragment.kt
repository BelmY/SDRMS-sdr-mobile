package space.sdrmaker.sdrmobile.benchmarks.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import space.sdrmaker.sdrmobile.benchmarks.NativeUtils
import space.sdrmaker.sdrmobile.benchmarks.R
import space.sdrmaker.sdrmobile.benchmarks.utils.floatConvolutionBenchmark
import space.sdrmaker.sdrmobile.benchmarks.utils.shortConvolutionBenchmark
import kotlin.math.round

class ConvolutionFragment : Fragment() {

    private lateinit var root: View

    private var convolutionFilterLength = 10
    private var convolutionDataLength = 50000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_convolution, container, false)

        // setup convolution button listener
        root.findViewById<Button>(R.id.convolutionButton).setOnClickListener {
            onConvolutionButtonClick()
        }

        // setup convolution filter length slider
        val filterLengthHandler = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                convolutionFilterLength = progress + 1
                setFilterLengthLabel()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        root.findViewById<SeekBar>(R.id.filterLengthBar).setOnSeekBarChangeListener(filterLengthHandler)
        setFilterLengthLabel()

        return root
    }

    private fun onConvolutionButtonClick() {
        // perform JVM convolution benchmark
        val jvmFloatTotalTime =
            floatConvolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmFloatSamplesPerSecond = opsPerSecond(convolutionDataLength, jvmFloatTotalTime)
        val jvmFloatResultLabel =
            "JVM float total time: $jvmFloatTotalTime ms\nJVM float samples/s: $jvmFloatSamplesPerSecond"
        setConvolutionResult(jvmFloatResultLabel)

        val jvmShortTotalTime =
            shortConvolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmShortSamplesPerSecond = opsPerSecond(convolutionDataLength, jvmShortTotalTime)
        val jvmShortResultLabel =
            "JVM short total time: $jvmShortTotalTime ms\nJVM short samples/s: $jvmShortSamplesPerSecond"

        // perform NDK convolution benchmark
        val ndkFloatTotalTime = NativeUtils.ndkFloatConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkFloatSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkFloatTotalTime)
        val ndkFloatResultLabel =
            "NDK float total time: $ndkFloatTotalTime ms\nNDK float samples/s: $ndkFloatSamplesPerSecond"

        val ndkShortTotalTime = NativeUtils.ndkShortConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkShortSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkShortTotalTime)
        val ndkShortResultLabel =
            "NDK short total time: $ndkShortTotalTime ms\nNDK short samples/s: $ndkShortSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n\n$jvmShortResultLabel\n\n$ndkFloatResultLabel\n\n$ndkShortResultLabel")
    }

    private fun opsPerSecond(totalOps: Int, totalTimeMS: Long): Long {
        return round(if (totalTimeMS > 0) totalOps.toDouble() * 1000 / totalTimeMS else totalOps.toDouble() * 1000).toLong()
    }

    private fun setConvolutionResult(result: String) {
        root.findViewById<TextView>(R.id.convolutionResultText).text = result
    }

    private fun setFilterLengthLabel() {
        root.findViewById<TextView>(R.id.filterLengthLabel).text = "Filter length: $convolutionFilterLength"
    }
}