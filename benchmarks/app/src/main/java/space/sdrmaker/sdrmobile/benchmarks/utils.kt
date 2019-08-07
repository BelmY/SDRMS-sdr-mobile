package space.sdrmaker.sdrmobile.benchmarks

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.random.Random

internal class FIR(private val coefs: FloatArray) {
    private val length: Int = coefs.size
    private val delayLine: FloatArray
    private var count = 0

    init {
        delayLine = FloatArray(length)
    }

    fun getOutputSample(inputSample: Float): Float {
        delayLine[count] = inputSample
        var result = 0.0F
        var index = count
        for (i in 0 until length) {
            result += coefs[i] * delayLine[index--]
            if (index < 0) index = length - 1
        }
        if (++count >= length) count = 0
        return result
    }
}

fun convolutionBenchmark(filterLength: Int = 10, dataLength: Int = 50000): Long {
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

fun fftBenchmark(fftWidth: Int, dataLength: Int): Long {
    val randomizer = Random(42)
    val data = DoubleArray(dataLength) {randomizer.nextDouble()}
    val fft = DoubleFFT_1D(fftWidth.toLong())

    val start = System.currentTimeMillis()
    for(i in 0 until (dataLength - 1 )/ fftWidth) {
        fft.complexForward(data, i * fftWidth)
    }
    val end = System.currentTimeMillis()

    return end - start
}
