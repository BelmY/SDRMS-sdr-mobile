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
import space.sdrmaker.sdrmobile.benchmarks.utils.*
import java.io.File

class BatchFragment : Fragment() {

    private lateinit var root: View

    private var convolutionDataLength = 50000
    private var fftDataLength = 100
    private var conversions = 50
    private var conversionsToPerform = 2000000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_batch, container, false)

        // setup batch benchmark button listener
        root.findViewById<Button>(R.id.batchBenchmarkButton).setOnClickListener {
            onBatchBenchmarkButtonClick()
        }

        return root
    }

    private fun onBatchBenchmarkButtonClick() {
        // perform convolution benchmarks
        val filterLengths = mutableListOf<Int>()
        val jvmFloatConvolutionResults = mutableListOf<Long>()
        val ndkFloatConvolutionResults = mutableListOf<Long>()
        val jvmShortConvolutionResults = mutableListOf<Long>()
        val ndkShortConvolutionResults = mutableListOf<Long>()
        val batchConvolutionFilterLengthMax = 1000
        for (batchConvolutionFilterLength in 10..batchConvolutionFilterLengthMax step 10) {
            if (batchConvolutionFilterLength.rem(100) == 0)
                println("Convolution progress $batchConvolutionFilterLength / $batchConvolutionFilterLengthMax")
            filterLengths.add(batchConvolutionFilterLength)

            val jvmFloatTotalTime = floatConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            jvmFloatConvolutionResults.add(opsPerSecond(convolutionDataLength, jvmFloatTotalTime))
            val jvmShortTotalTime = shortConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            jvmShortConvolutionResults.add(opsPerSecond(convolutionDataLength, jvmShortTotalTime))

            val ndkFloatTotalTime = NativeUtils.ndkFloatConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            ndkFloatConvolutionResults.add(opsPerSecond(convolutionDataLength, ndkFloatTotalTime))
            val ndkShortTotalTime = NativeUtils.ndkShortConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            ndkShortConvolutionResults.add(opsPerSecond(convolutionDataLength, ndkShortTotalTime))
        }

        // perform FFT benchmarks
        val fftWidths = mutableListOf<Int>()
        val jvmComplexFFTResults = mutableListOf<Long>()
        val ndkComplexFFTResults = mutableListOf<Long>()
        val jvmRealFFTResults = mutableListOf<Long>()
        val ndkRealFFTResults = mutableListOf<Long>()
        val batchFFTWidthMax = 1024 * 20
        for (batchFFTWidth in 1024..batchFFTWidthMax step 1024) {
            println("FFT progress $batchFFTWidth / $batchFFTWidthMax")
            fftWidths.add(batchFFTWidth)

            val jvmComplexTotalTime = fftComplexBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            jvmComplexFFTResults.add(opsPerSecond(fftDataLength, jvmComplexTotalTime))
            val jvmRealTotalTime = fftRealBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            jvmRealFFTResults.add(opsPerSecond(fftDataLength, jvmRealTotalTime))
            val ndkComplexTotalTime = NativeUtils.ndkComplexFFTBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            ndkComplexFFTResults.add(opsPerSecond(fftDataLength, ndkComplexTotalTime))
            val ndkRealTotalTime = NativeUtils.ndkRealFFTBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            ndkRealFFTResults.add(opsPerSecond(fftDataLength, ndkRealTotalTime))
        }

        // perform conversion benchmarks
        val jvmShortFloatConversionResults = mutableListOf<Long>()
        val jvmFloatShortConversionResults = mutableListOf<Long>()
        val jvmShortComplexConversionResults = mutableListOf<Long>()

        val ndkShortFloatConversionResults = mutableListOf<Long>()
        val ndkFloatShortConversionResults = mutableListOf<Long>()
        val ndkShortComplexConversionResults = mutableListOf<Long>()
        for (i in 0 until conversions) {
            println("Conversions progress $i / $conversions")

            val jvmTotalTime = conversionsBenchmark(conversionsToPerform)
            println("${jvmTotalTime[0]} ${jvmTotalTime[1]} ${jvmTotalTime[2]}")
            jvmShortFloatConversionResults.add(opsPerSecond(conversionsToPerform, jvmTotalTime[0]))
            jvmFloatShortConversionResults.add(opsPerSecond(conversionsToPerform, jvmTotalTime[1]))
            jvmShortComplexConversionResults.add(opsPerSecond(conversionsToPerform, jvmTotalTime[2]))

            ndkShortFloatConversionResults.add(
                opsPerSecond(conversionsToPerform, NativeUtils.ndkShortFloatConversionBenchmark(conversionsToPerform)))
            ndkFloatShortConversionResults.add(
                opsPerSecond(conversionsToPerform, NativeUtils.ndkShortFloatConversionBenchmark(conversionsToPerform)))
            ndkShortComplexConversionResults.add(
                opsPerSecond(conversionsToPerform, NativeUtils.ndkShortFloatConversionBenchmark(conversionsToPerform)))
        }

        // dump results to csv files
        val timestamp = System.currentTimeMillis()
        var path = "${context!!.getExternalFilesDir(null)}/convolution_benchmark_$timestamp.csv"
        println("Saving benchmark results to ${context!!.getExternalFilesDir(null)}")
        var file = File(path)
        file.createNewFile()
        file.printWriter().use { out ->
            out.print("env,")
            out.println(filterLengths.joinToString(","))
            out.print("jvm_float,")
            out.println(jvmFloatConvolutionResults.joinToString(","))
            out.print("jvm_short,")
            out.println(jvmShortConvolutionResults.joinToString(","))
            out.print("ndk_float,")
            out.println(ndkFloatConvolutionResults.joinToString(","))
            out.print("ndk_short,")
            out.println(ndkShortConvolutionResults.joinToString(","))
        }
        path = "${context!!.getExternalFilesDir(null)}/fft_benchmark_$timestamp.csv"
        file = File(path)
        file.createNewFile()
        file.printWriter().use { out ->
            out.print("env,")
            out.println(fftWidths.joinToString(","))
            out.print("jvm_complex,")
            out.println(jvmComplexFFTResults.joinToString(","))
            out.print("jvm_real,")
            out.println(jvmRealFFTResults.joinToString(","))
            out.print("ndk_complex,")
            out.println(ndkComplexFFTResults.joinToString(","))
            out.print("ndk_real,")
            out.println(ndkRealFFTResults.joinToString(","))
        }
        path = "${context!!.getExternalFilesDir(null)}/conversion_benchmark_$timestamp.csv"
        file = File(path)
        file.createNewFile()
        file.printWriter().use { out ->
            out.print("jvm_short_float,")
            out.println(jvmShortFloatConversionResults.joinToString(","))
            out.print("jvm_float_short,")
            out.println(jvmFloatShortConversionResults.joinToString(","))
            out.print("jvm_short_complex,")
            out.println(jvmShortComplexConversionResults.joinToString(","))

            out.print("ndk_short_float,")
            out.println(ndkShortComplexConversionResults.joinToString(","))
            out.print("ndk_float_short,")
            out.println(ndkShortFloatConversionResults.joinToString(","))
            out.print("ndk_short_complex,")
            out.println(ndkShortComplexConversionResults.joinToString(","))
        }
        setBatchResult()
    }

    private fun setBatchResult() {
        root.findViewById<TextView>(R.id.batchResultText).text =
            "Benchmark results saved to ${context!!.getExternalFilesDir(null)}"
    }

}