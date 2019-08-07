package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

import kotlin.random.Random

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

    }

    private fun onConvolutionButtonClick() {
        // perform java convolution benchmark
        val javaTotalTime = javaConvolutionBenchmark(filterLength = convolutionFilterLength, dataLength = convolutionDataLength)
        val javaSamplesPerMS = if (javaTotalTime != 0L) convolutionDataLength / javaTotalTime else convolutionDataLength
        val javaResultLabel = "Java total time: $javaTotalTime ms\nJava samples/ms: $javaSamplesPerMS"
        setConvolutionResult(javaResultLabel)

        // perform NDK convolution benchmark
        val ndkTotalTime = ndkConvolutionBenchmark(convolutionFilterLength, convolutionDataLength)
        val ndkSamplesPerMS = if (ndkTotalTime != 0L) convolutionDataLength / ndkTotalTime else convolutionDataLength
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK samples/ms: $ndkSamplesPerMS"
        setConvolutionResult("$javaResultLabel\n\n$ndkResultLabel")
    }

    private fun onFFTButtonClick() {
        // perform java FFT benchmark
        val javaResultLabel = "TODO"

        // perform NDK FFT benchmark
        val ndkTotalTime = ndkFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkFFTsPerMS = if (ndkTotalTime != 0L) fftDataLength / ndkTotalTime else fftDataLength
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK FFTs/ms: $ndkFFTsPerMS"
        setFFTResult("$javaResultLabel\n\n$ndkResultLabel")
    }

    private fun javaConvolutionBenchmark(filterLength: Int = 10, dataLength: Int = 50000): Long {
        val randomizer = Random(42)
        val filter = FIR(FloatArray(filterLength) {randomizer.nextFloat()})
        val data = FloatArray(dataLength) {randomizer.nextFloat()}
        val start = System.currentTimeMillis()
        for(i in 0 until dataLength) {
            filter.getOutputSample(data[i])
        }
        val end = System.currentTimeMillis()
        return end - start
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
