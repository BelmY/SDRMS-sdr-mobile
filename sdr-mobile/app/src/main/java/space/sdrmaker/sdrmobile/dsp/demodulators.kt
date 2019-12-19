package space.sdrmaker.sdrmobile.dsp.demodulators

import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Performs FM demodulation of interleaved complex signal contained in arrays provided by input iterator.
 *
 * @property input Iterator providing FloatArrays representing interleaved complex signal to be demodulated.
 * @property gain Demodulated signal gain.
 * @property quadratureRate samples / s rate of input signal.
 * @constructor Creates new FMDemodulator.
 *
 * @param maxDeviation Maximum difference between an FM modulated frequency and the nominal carrier frequency.
 */
class FMDemodulator(
    private val input: Iterator<FloatArray>,
    maxDeviation: Int,
    private val quadratureRate: Float,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val quadratureGain = (quadratureRate / (2 * Math.PI * maxDeviation)).toFloat()
    private var previousRe = 1f
    private var previousIm = 1f

    /**
     * Performs FM demodulation of next array provided by input Iterator.
     *
     * @return FloatArray of real-valued FM-demodulated signal based on input.
     */
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

/**
 * Performs AM demodulation of interleaved complex signal contained in arrays provided by input iterator.
 *
 * @property input Iterator providing FloatArrays representing interleaved complex signal to be demodulated.
 * @property gain Demodulated signal gain.
 */
class AMDemodulator(
    private val input: Iterator<FloatArray>,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    /**
     * Performs AM demodulation of next array provided by input Iterator.
     *
     * @return FloatArray of real-valued AM-demodulated signal based on input.
     */
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
