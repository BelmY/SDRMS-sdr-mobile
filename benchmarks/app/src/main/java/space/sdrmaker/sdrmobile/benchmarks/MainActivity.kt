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
        val jvmFloatResultLabel = "JVM Float total time: $jvmFloatTotalTime ms\nJVM Float samples/s: $jvmFloatSamplesPerSecond"
        setConvolutionResult(jvmFloatResultLabel)

        val jvmShortTotalTime = shortConvolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmShortSamplesPerSecond = opsPerSecond(convolutionDataLength, jvmShortTotalTime)
        val jvmShortResultLabel = "JVM Short total time: $jvmShortTotalTime ms\nJVM Short samples/s: $jvmShortSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n$jvmShortResultLabel")

        // perform NDK convolution benchmark
        val ndkFloatTotalTime = ndkFloatConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkFloatSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkFloatTotalTime)
        val ndkFloatResultLabel = "NDK Float total time: $ndkFloatTotalTime ms\nNDK Float samples/s: $ndkFloatSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n$jvmShortResultLabel\n$ndkFloatResultLabel")

        val ndkShortTotalTime = ndkShortConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkShortSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkShortTotalTime)
        val ndkShortResultLabel = "NDK Short total time: $ndkShortTotalTime ms\nNDK Short samples/s: $ndkShortSamplesPerSecond"
        setConvolutionResult("$jvmFloatResultLabel\n$jvmShortResultLabel\n$ndkFloatResultLabel\n$ndkShortResultLabel")
    }

    private fun onFFTButtonClick() {
        // perform JVM FFT benchmark
        val jvmTotalTime = fftBenchmark(fftWidth, fftDataLength * fftWidth)
        val jvmFFTsPerSecond = opsPerSecond(fftDataLength, jvmTotalTime)
        val jvmResultLabel = "JVM total time: $jvmTotalTime ms\nJVM FFTs/s: $jvmFFTsPerSecond"
        setFFTResult(jvmResultLabel)

        // perform NDK FFT benchmark
        val ndkTotalTime = ndkFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkFFTsPerSecond = opsPerSecond(fftDataLength, ndkTotalTime)
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK FFTs/s: $ndkFFTsPerSecond"
        setFFTResult("$jvmResultLabel\n\n$ndkResultLabel")
    }

    private fun opsPerSecond(totalOps: Int, totalTimeMS: Long): Long {
        return round(if(totalTimeMS > 0) totalOps.toDouble() * 1000 / totalTimeMS else totalOps.toDouble() * 1000).toLong()
    }

    private fun onBatchBenchmarkButtonClick() {
        // perform convolution benchmarks
        val filterLengths = mutableListOf<Int>()
        val jvmConvolutionResults = mutableListOf<Long>()
        val ndkConvolutionResults = mutableListOf<Long>()
        val batchConvolutionFilterLengthMax = 1000
        for (batchConvolutionFilterLength in 10..batchConvolutionFilterLengthMax step 10) {
            if (batchConvolutionFilterLength.rem(100) == 0)
                println("Convolution progress $batchConvolutionFilterLength / $batchConvolutionFilterLengthMax")
            filterLengths.add(batchConvolutionFilterLength)
            val jvmTotalTime = floatConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            jvmConvolutionResults.add(opsPerSecond(convolutionDataLength, jvmTotalTime))
            val ndkTotalTime = ndkFloatConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            ndkConvolutionResults.add(opsPerSecond(convolutionDataLength, ndkTotalTime))
        }

        // perform FFT benchmarks
        val fftWidths = mutableListOf<Int>()
        val jvmFFTResults = mutableListOf<Long>()
        val ndkFFTResults = mutableListOf<Long>()
        val batchFFTWidthMax = 1024 * 50
        for (batchFFTWidth in 1024..batchFFTWidthMax step 1024) {
            println("FFT progress $batchFFTWidth / $batchFFTWidthMax")
            fftWidths.add(batchFFTWidth)
            val jvmTotalTime = fftBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            jvmFFTResults.add(opsPerSecond(fftDataLength, jvmTotalTime))
            val ndkTotalTime = ndkFFTBenchmark(batchFFTWidth, fftDataLength * batchFFTWidth)
            ndkFFTResults.add(opsPerSecond(fftDataLength, ndkTotalTime))
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
            out.print("jvm,")
            out.println(jvmConvolutionResults.joinToString(","))
            out.print("ndk,")
            out.println(ndkConvolutionResults.joinToString(","))
        }
        path = "${getExternalFilesDir(null)}/fft_benchmark_$timestamp.csv"
        file = File(path)
        file.createNewFile()
        file.printWriter().use {out ->
            out.print("env,")
            out.println(fftWidths.joinToString(","))
            out.print("jvm,")
            out.println(jvmFFTResults.joinToString(","))
            out.print("ndk,")
            out.println(ndkFFTResults.joinToString(","))
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
    external fun ndkFFTBenchmark(fftWidth: Int, dataLength: Int): Long

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
