package space.sdrmaker.sdrmobile.dsp

import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

class FMDemodulator(
    private val input: Iterator<FloatArray>,
    maxDeviation: Int,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val quadratureRate = 2.toFloat() * AUDIO_SAMPLE_RATE
    private val quadratureGain = (quadratureRate / (2 * Math.PI * maxDeviation)).toFloat()
    private var previousRe = 1f
    private var previousIm = 1f

    override fun next(): FloatArray {
        val nextArray = input.next()
        val iterator = nextArray.iterator()
        val result = FloatArray(floor(nextArray.size.toFloat() / 2).toInt())
        var resultCounter = 0
        while (iterator.hasNext()) {
            val re = iterator.next()
            val im = if (iterator.hasNext()) iterator.next() else break

            // Quadrature demodulation
            var reOut = re * previousRe + im * previousIm
            var imOut = im * previousRe - re * previousIm

            previousRe = re
            previousIm = im
            result[resultCounter++] =
                gain * quadratureGain * atan2(imOut.toDouble(), reOut.toDouble()).toFloat()
        }
        return result
    }

    override fun hasNext() = input.hasNext()

}

class AMDemodulator(
    private val input: Iterator<FloatArray>,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    override fun next(): FloatArray {
        val nextArray = input.next()
        val iterator = nextArray.iterator()
        val result = FloatArray(floor(nextArray.size.toFloat() / 2).toInt())
        var resultCounter = 0
        while (iterator.hasNext()) {
            val re = iterator.next()
            val im = if (iterator.hasNext()) iterator.next() else break
            result[resultCounter++] =
                gain * sqrt(re.pow(2) + im.pow(2))
        }
        return result
    }

    override fun hasNext() = input.hasNext()

}
