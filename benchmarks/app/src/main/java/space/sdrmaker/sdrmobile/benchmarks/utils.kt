package space.sdrmaker.sdrmobile.benchmarks.utils

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.floor
import kotlin.random.Random

internal class FIRFloat(private val coefs: FloatArray) {
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

internal class FIRShort(private val coefs: ShortArray) {
    private val length: Int = coefs.size
    private val delayLine: ShortArray
    private var count = 0

    init {
        delayLine = ShortArray(length)
    }

    fun getOutputSample(inputSample: Short): Int {
        delayLine[count] = inputSample
        var result = 0
        var index = count
        for (i in 0 until length) {
            result += coefs[i] * delayLine[index--]
            if (index < 0) index = length - 1
        }
        if (++count >= length) count = 0
        return result
    }
}

fun floatConvolutionBenchmark(filterLength: Int = 10, dataLength: Int = 50000): Long {
    val randomizer = Random(42)
    val filter = FIRFloat(FloatArray(filterLength) {randomizer.nextFloat()})
    val data = FloatArray(dataLength) {randomizer.nextFloat()}
    val start = System.currentTimeMillis()
    for(i in 0 until dataLength) {
        filter.getOutputSample(data[i])
    }
    val end = System.currentTimeMillis()
    return end - start
}

fun shortConvolutionBenchmark(filterLength: Int = 10, dataLength: Int = 50000): Long {
    val randomizer = Random(42)
    val filter = FIRShort(ShortArray(filterLength) {randomizer.nextInt().toShort()})
    val data = ShortArray(dataLength) {randomizer.nextInt().toShort()}
    val start = System.currentTimeMillis()
    for(i in 0 until dataLength) {
        filter.getOutputSample(data[i])
    }
    val end = System.currentTimeMillis()
    return end - start
}

fun fftComplexBenchmark(fftWidth: Int, dataLength: Int): Long {
    val randomizer = Random(42)
    val data = DoubleArray(dataLength * 2) {randomizer.nextDouble()}
    val fft = DoubleFFT_1D(fftWidth.toLong())

    val start = System.currentTimeMillis()
    for(i in 0 until floor(dataLength.toFloat() / fftWidth).toInt()) {
        fft.complexForward(data, i * 2 * fftWidth)
    }
    val end = System.currentTimeMillis()

    return end - start
}

fun fftRealBenchmark(fftWidth: Int, dataLength: Int): Long {
    val randomizer = Random(42)
    val data = DoubleArray(dataLength) {randomizer.nextDouble()}
    val fft = DoubleFFT_1D(fftWidth.toLong())

    val start = System.currentTimeMillis()
    for(i in 0 until floor(dataLength.toFloat() / fftWidth).toInt()) {
        fft.realForward(data, i * fftWidth)
    }
    val end = System.currentTimeMillis()

    return end - start
}
