package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


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
        val jvmTotalTime = convolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val jvmSamplesPerSecond = opsPerSecond(convolutionDataLength, jvmTotalTime)
        val jvmResultLabel = "JVM total time: $jvmTotalTime ms\nJVM samples/s: $jvmSamplesPerSecond"
        setConvolutionResult(jvmResultLabel)

        // perform NDK convolution benchmark
        val ndkTotalTime = ndkConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkSamplesPerSecond = opsPerSecond(convolutionDataLength, ndkTotalTime)
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK samples/s: $ndkSamplesPerSecond"
        setConvolutionResult("$jvmResultLabel\n\n$ndkResultLabel")
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

    private fun opsPerSecond(totalOps: Int, totalTimeMS: Long): Double {
        return if(totalTimeMS > 0) totalOps.toDouble() * 1000 / totalTimeMS else totalOps.toDouble() * 1000
    }

    private fun onBatchBenchmarkButtonClick() {
        // perform convolution benchmarks
        val filterLengths = mutableListOf<Int>()
        val jvmConvolutionResults = mutableListOf<Double>()
        val ndkConvolutionResults = mutableListOf<Double>()
        for (batchConvolutionFilterLength in 1 until 100 step 10) {
            if (batchConvolutionFilterLength.rem(100) == 0)
                println("Convolution progress $batchConvolutionFilterLength / 10000")
            filterLengths.add(batchConvolutionFilterLength)
            val jvmTotalTime = convolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            jvmConvolutionResults.add(opsPerSecond(convolutionDataLength, jvmTotalTime))
            val ndkTotalTime = ndkConvolutionBenchmark(batchConvolutionFilterLength, convolutionDataLength)
            ndkConvolutionResults.add(opsPerSecond(convolutionDataLength, ndkTotalTime))
        }

        // perform FFT benchmarks
        val fftWidths = mutableListOf<Int>()
        val jvmFFTResults = mutableListOf<Double>()
        val ndkFFTResults = mutableListOf<Double>()
        for (batchFFTWidth in 1024 until 1024 * 100 step 1024) {
            println("FFT progress $batchFFTWidth / ${1024 * 100}")
            fftWidths.add(batchFFTWidth)
            val jvmTotalTime = fftBenchmark(batchFFTWidth, fftDataLength)
            jvmFFTResults.add(opsPerSecond(fftDataLength, jvmTotalTime))
            val ndkTotalTime = ndkConvolutionBenchmark(batchFFTWidth, fftDataLength)
            ndkFFTResults.add(opsPerSecond(fftDataLength, ndkTotalTime))
        }

        // dump results to csv file
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
    external fun ndkConvolutionBenchmark(filterLength: Int, dataLength: Int): Long
    external fun ndkFFTBenchmark(fftWidth: Int, dataLength: Int): Long

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
