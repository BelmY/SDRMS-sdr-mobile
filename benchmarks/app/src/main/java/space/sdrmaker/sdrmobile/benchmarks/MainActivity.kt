package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var filterLength = 10
    private var dataLength = 50000

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
                filterLength = progress + 1
                setFilterLengthLabel()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        filterLengthBar.setOnSeekBarChangeListener(filterLengthHandler)
        setFilterLengthLabel()
    }

    private fun onConvolutionButtonClick() {
        // perform java convolution benchmark
        val javaTotalTime = javaConvolutionBenchmark(filterLength = filterLength, dataLength = dataLength)
        val javaSamplesPerMS = dataLength / javaTotalTime
        val javaResultLabel = "Java total time: $javaTotalTime ms\nJava samples/ms: $javaSamplesPerMS"
        setConvolutionResult(javaResultLabel)

        // perform NDK convolution benchmark
        val ndkTotalTime = ndkConvolutionBenchmark(filterLength, dataLength)
        val ndkSamplesPerMS = dataLength / ndkTotalTime
        val ndkResultLabel = "NDK total time: $ndkTotalTime ms\nNDK samples/ms: $ndkSamplesPerMS"
        setConvolutionResult("$javaResultLabel\n\n$ndkResultLabel")
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
        resultText.text = result
    }

    private fun setFilterLengthLabel() {
        filterLengthLabel.text = "Filter length: $filterLength"
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun ndkConvolutionBenchmark(filterLength: Int, dataLength: Int): Long

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
