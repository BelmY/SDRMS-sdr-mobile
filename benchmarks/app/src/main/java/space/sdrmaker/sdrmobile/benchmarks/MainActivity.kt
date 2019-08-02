package space.sdrmaker.sdrmobile.benchmarks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var filterLength = 10
    private var dataLength = 1024

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

        // setup data length slider
        val dataLengthHandler = object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dataLength = (progress + 1) * 1000
                setDataLengthLabel()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        dataLengthBar.setOnSeekBarChangeListener(dataLengthHandler)
        setDataLengthLabel()
    }

    private fun onConvolutionButtonClick() {
        // perform java convolution benchmark
        val javaConvolutionResult = javaConvolutionBenchmark(filterLength = filterLength, dataLength = dataLength)
        setConvolutionResult(javaConvolutionResult)

        // TODO: perform NDK convolution benchmark
        val ndkConvolutionResult = ndkConvolution()
        println(ndkConvolutionResult)
    }

    private fun javaConvolutionBenchmark(filterLength: Int = 10, dataLength: Int = 2048): String {
        val randomizer = Random(42)
        val filter = FIR(DoubleArray(filterLength) {randomizer.nextDouble()})
        val data = DoubleArray(dataLength) {randomizer.nextDouble()}
        val start = System.currentTimeMillis()
        for(i in 0 until dataLength) {
            filter.getOutputSample(data[i])
        }
        val end = System.currentTimeMillis()
        val totalTime = end - start
        val samplesPerMilisecond = if (totalTime != 0L) dataLength / totalTime else dataLength

        return "$dataLength samples processed in $totalTime ms\n" +
                "samples/ms: $samplesPerMilisecond"
    }

    private fun setConvolutionResult(result: String) {
        resultText.text = result
    }

    private fun setFilterLengthLabel() {
        filterLengthLabel.text = "Filter length: $filterLength"
    }

    private fun setDataLengthLabel() {
        dataLengthLabel.text = "Data length: $dataLength"
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun ndkConvolution(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
