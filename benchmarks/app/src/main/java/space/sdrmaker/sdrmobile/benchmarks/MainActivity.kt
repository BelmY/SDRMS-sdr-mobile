package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.math.round


class MainActivity : AppCompatActivity() {

    private var convolutionFilterLength = 10
    private var convolutionDataLength = 50000
    private var fftWidth = 1024
    private var fftDataLength = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup convolution button listener
        convolutionButton.setOnClickListener {
            onConvolutionButtonClick()
        }

        // setup convolution filter length slider
        val filterLengthHandler = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                convolutionFilterLength = progress + 1
                setFilterLengthLabel()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        filterLengthBar.setOnSeekBarChangeListener(filterLengthHandler)
        setFilterLengthLabel()

        // setup fft button listener
        fftButton.setOnClickListener {
            onFFTButtonClick()
        }

        // setup fft width slider
        val fftWidthHandler = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fftWidth = (progress + 1) * 1024
                setFFTWidthLabel()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        fftWidthBar.setOnSeekBarChangeListener(fftWidthHandler)
        setFFTWidthLabel()

        // setup batch benchmark button listener
        batchBenchmarkButton.setOnClickListener {
            onBatchBenchmarkButtonClick()
        }
    }

    private fun onConvolutionButtonClick() {
        // perform JVM convolution benchmark
        val jvmFloatTotalTime = floatConvolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmFloatSamplesPerSecond = opsPerSecond(convolutionDataLength, jvmFloatTotalTime)
        val jvmFloatResultLabel = "JVM float total time: $jvmFloatTotalTime ms\nJVM float samples/s: $jvmFloatSamplesPerSecond"
        setConvolutionResult(jvmFloatResultLabel)

        val jvmShortTotalTime = shortConvolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmShortSamplesPerSecond = opsPerSecond(convolutionDataLength, jvmShortTotalTime)
        val jvmShortResultLabel = "JVM short total time: $jvmShortTotalTime ms\nJVM short samples/s: $jvmShortSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n$jvmShortResultLabel")

        // perform NDK convolution benchmark
        val ndkFloatTotalTime = ndkFloatConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkFloatSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkFloatTotalTime)
        val ndkFloatResultLabel = "NDK float total time: $ndkFloatTotalTime ms\nNDK float samples/s: $ndkFloatSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n$jvmShortResultLabel\n$ndkFloatResultLabel")

        val ndkShortTotalTime = ndkShortConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkShortSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkShortTotalTime)
        val ndkShortResultLabel = "NDK short total time: $ndkShortTotalTime ms\nNDK short samples/s: $ndkShortSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n$jvmShortResultLabel\n$ndkFloatResultLabel\n$ndkShortResultLabel")
    }

    private fun onFFTButtonClick() {
        // perform JVM FFT benchmark
        val jvmComplexTotalTime = fftComplexBenchmark(fftWidth, fftDataLength * fftWidth)
        val jvmComplexFFTsPerSecond = opsPerSecond(fftDataLength, jvmComplexTotalTime)
        val jvmComplexResultLabel = "JVM complex total time: $jvmComplexTotalTime ms\nJVM complex FFTs/s: $jvmComplexFFTsPerSecond"
        setFFTResult(jvmComplexResultLabel)

        val jvmRealTotalTime = fftRealBenchmark(fftWidth, fftDataLength * fftWidth)
        val jvmRealFFTsPerSecond = opsPerSecond(fftDataLength, jvmRealTotalTime)
        val jvmRealResultLabel = "JVM real total time: $jvmRealTotalTime ms\nJVM real FFTs/s: $jvmRealFFTsPerSecond"
        setFFTResult("$jvmComplexResultLabel\n$jvmRealResultLabel")

        // perform NDK FFT benchmark
        val ndkComplexTotalTime = ndkComplexFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkComplexFFTsPerSecond = opsPerSecond(fftDataLength, ndkComplexTotalTime)
        val ndkComplexResultLabel = "NDK complex total time: $ndkComplexTotalTime ms\nNDK complex FFTs/s: $ndkComplexFFTsPerSecond"
        setFFTResult("$jvmComplexResultLabel\n$jvmRealResultLabel\n$ndkComplexResultLabel")

        val ndkRealTotalTime = ndkRealFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkRealFFTsPerSecond = opsPerSecond(fftDataLength, ndkRealTotalTime)
        val ndkRealResultLabel = "NDK real total time: $ndkRealTotalTime ms\nNDK real FFTs/s: $ndkRealFFTsPerSecond"
        setFFTResult("$jvmComplexResultLabel\n$jvmRealResultLabel\n$ndkComplexResultLabel\n$ndkRealResultLabel")
    }

    private fun opsPerSecond(totalOps: Int, totalTimeMS: Long): Long {
        return round(if(totalTimeMS > 0) totalOps.toDouble() * 1000 / totalTimeMS else totalOps.toDouble() * 1000).toLong()
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

            val ndkFloatTotalTime = ndkFloatConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            ndkFloatConvolutionResults.add(opsPerSecond(convolutionDataLength, ndkFloatTotalTime))
            val ndkShortTotalTime = ndkShortConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            ndkShortConvolutionResults.add(opsPerSecond(convolutionDataLength, ndkShortTotalTime))
        }

        // perform FFT benchmarks
        val fftWidths = mutableListOf<Int>()
        val jvmComplexFFTResults = mutableListOf<Long>()
        val ndkComplexFFTResults = mutableListOf<Long>()
        val jvmRealFFTResults = mutableListOf<Long>()
        val ndkRealFFTResults = mutableListOf<Long>()
        val batchFFTWidthMax = 1024 * 50
        for (batchFFTWidth in 1024..batchFFTWidthMax step 1024) {
            println("FFT progress $batchFFTWidth / $batchFFTWidthMax")
            fftWidths.add(batchFFTWidth)

            val jvmComplexTotalTime = fftComplexBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            jvmComplexFFTResults.add(opsPerSecond(fftDataLength, jvmComplexTotalTime))
            val jvmRealTotalTime = fftRealBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            jvmRealFFTResults.add(opsPerSecond(fftDataLength, jvmRealTotalTime))
            val ndkComplexTotalTime = ndkComplexFFTBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            ndkComplexFFTResults.add(opsPerSecond(fftDataLength, ndkComplexTotalTime))
            val ndkRealTotalTime = ndkRealFFTBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            ndkRealFFTResults.add(opsPerSecond(fftDataLength, ndkRealTotalTime))
        }

        // dump results to csv files
        val timestamp = System.currentTimeMillis()
        var path = "${getExternalFilesDir(null)}/convolution_benchmark_$timestamp.csv"
        println("Saving benchmark results to ${getExternalFilesDir(null)}")
        var file = File(path)
        file.createNewFile()
        file.printWriter().use {out ->
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
        path = "${getExternalFilesDir(null)}/fft_benchmark_$timestamp.csv"
        file = File(path)
        file.createNewFile()
        file.printWriter().use {out ->
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
    }

    private fun setConvolutionResult(result: String) {
        convolutionResultText.text = result
    }

    private fun setFilterLengthLabel() {
        filterLengthLabel.text = "Filter length: $convolutionFilterLength"
    }

    private fun setFFTResult(result: String) {
        fftResultText.text = result
    }

    private fun setFFTWidthLabel() {
        fftWidthLabel.text = "FFT width: $fftWidth"
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun ndkFloatConvolutionBenchmark(filterLength: Int, dataLength: Int): Long
    external fun ndkShortConvolutionBenchmark(filterLength: Int, dataLength: Int): Long
    external fun ndkComplexFFTBenchmark(fftWidth: Int, dataLength: Int): Long
    external fun ndkRealFFTBenchmark(fftWidth: Int, dataLength: Int): Long

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
