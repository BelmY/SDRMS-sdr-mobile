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
import space.sdrmaker.sdrmobile.benchmarks.utils.fftComplexBenchmark
import space.sdrmaker.sdrmobile.benchmarks.utils.fftRealBenchmark
import kotlin.math.round

class FFTFragment : Fragment() {

    private lateinit var root: View

    private var fftWidth = 1024
    private var fftDataLength = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_fft, container, false)

        // setup fft button listener
        root.findViewById<Button>(R.id.fftButton).setOnClickListener {
            onFFTButtonClick()
        }

        // setup fft width slider
        val fftWidthHandler = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fftWidth = (progress + 1) * 1024
                setFFTWidthLabel()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        }
        root.findViewById<SeekBar>(R.id.fftWidthBar).setOnSeekBarChangeListener(fftWidthHandler)
        setFFTWidthLabel()

        return root
    }

    private fun onFFTButtonClick() {
        // perform JVM FFT benchmark
        val jvmComplexTotalTime = fftComplexBenchmark(fftWidth, fftDataLength * fftWidth)
        val jvmComplexFFTsPerSecond = opsPerSecond(fftDataLength, jvmComplexTotalTime)
        val jvmComplexResultLabel =
            "JVM complex total time: $jvmComplexTotalTime ms\nJVM complex FFTs/s: $jvmComplexFFTsPerSecond"
        setFFTResult(jvmComplexResultLabel)

        val jvmRealTotalTime = fftRealBenchmark(fftWidth, fftDataLength * fftWidth)
        val jvmRealFFTsPerSecond = opsPerSecond(fftDataLength, jvmRealTotalTime)
        val jvmRealResultLabel = "JVM real total time: $jvmRealTotalTime ms\nJVM real FFTs/s: $jvmRealFFTsPerSecond"
        setFFTResult("$jvmComplexResultLabel\n$jvmRealResultLabel")

        // perform NDK FFT benchmark
        val ndkComplexTotalTime = NativeUtils.ndkComplexFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkComplexFFTsPerSecond = opsPerSecond(fftDataLength, ndkComplexTotalTime)
        val ndkComplexResultLabel =
            "NDK complex total time: $ndkComplexTotalTime ms\nNDK complex FFTs/s: $ndkComplexFFTsPerSecond"
        setFFTResult("$jvmComplexResultLabel\n$jvmRealResultLabel\n$ndkComplexResultLabel")

        val ndkRealTotalTime = NativeUtils.ndkRealFFTBenchmark(fftWidth, fftDataLength * fftWidth)
        val ndkRealFFTsPerSecond = opsPerSecond(fftDataLength, ndkRealTotalTime)
        val ndkRealResultLabel = "NDK real total time: $ndkRealTotalTime ms\nNDK real FFTs/s: $ndkRealFFTsPerSecond"
        setFFTResult("$jvmComplexResultLabel\n$jvmRealResultLabel\n$ndkComplexResultLabel\n$ndkRealResultLabel")
    }

    private fun opsPerSecond(totalOps: Int, totalTimeMS: Long): Long {
        return round(if (totalTimeMS > 0) totalOps.toDouble() * 1000 / totalTimeMS else totalOps.toDouble() * 1000).toLong()
    }

    private fun setFFTResult(result: String) {
        root.findViewById<TextView>(R.id.fftResultText).text = result
    }

    private fun setFFTWidthLabel() {
        root.findViewById<TextView>(R.id.fftWidthLabel).text = "FFT width: $fftWidth"
    }

}