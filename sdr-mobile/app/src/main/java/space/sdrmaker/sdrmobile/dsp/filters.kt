package space.sdrmaker.sdrmobile.dsp.filters

import java.util.ArrayDeque
import kotlin.math.ceil

/**
 * Implementation of real input low-pass FIR filter.
 *
 * @property input Iterator providing FloatArrays representing real-valued signal to be filtered.
 * @property coefs FloatArray of FIR coefficients.
 * @property decimation Decimation ratio, default = 1 (no decimation).
 * @property gain Filtered signal gain.
 */
class FIRFilter(
    private val input: Iterator<FloatArray>,
    private val coefs: FloatArray,
    private val decimation: Int = 1,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {
    private val length: Int = coefs.size
    private val delayLine = FloatArray(length)
    private var count = 0
    private var rem = 0

    /**
     * Performs filtering on next array provided by input Iterator.
     *
     * @return FloatArray of filtered real-valued signal based on input.
     */
    override fun next(): FloatArray {
        val nextArray = input.next()
        val offset = (decimation - rem).rem(decimation)
        val resultSize = ceil((nextArray.size.toFloat() - offset) / decimation).toInt()
        val result = FloatArray(resultSize) { 0f }
        nextArray.forEachIndexed { arrayIndex, value ->
            run {
                delayLine[count] = value
                var index = count
                if ((arrayIndex + rem).rem(decimation) == 0) {
                    for (i in 0 until length) {
                        result[arrayIndex / decimation] += coefs[i] * delayLine[index--]
                        if (index < 0) index = length - 1
                    }
                    result[arrayIndex / decimation] = result[arrayIndex / decimation] * gain / length
                }
                if (++count >= length) count = 0
            }
        }
        rem = (rem + nextArray.size).rem(decimation)
        return result
    }

    override fun hasNext() = input.hasNext()
}

/**
 * Implementation of interleaved complex input low-pass FIR filter.
 *
 * @property input Iterator providing FloatArrays representing interleaved complex signal to be filtered.
 * @property coefs FloatArray of FIR coefficients.
 * @property decimation Decimation ratio, default = 1 (no decimation).
 * @property gain Filtered signal gain.
 */
class ComplexFIRFilter(
    private val input: Iterator<FloatArray>,
    private val coefs: FloatArray,
    private val decimation: Int = 1,
    private val gain: Float = 1f
) : Iterator<FloatArray> {
    private val length: Int = coefs.size
    private val reDelayLine = FloatArray(length)
    private val imDelayLine = FloatArray(length)
    private var count = 0

    /**
     * Performs filtering on next array provided by input Iterator.
     *
     * @return FloatArray of filtered interleaved complex signal based on input.
     */
    override fun next(): FloatArray {
        val nextArray = input.next()
        val result = FloatArray(ceil(nextArray.size.toFloat() / decimation).toInt()) { 0f }
        for (arrayIndex in 0 until nextArray.size - 1 step 2) {
            reDelayLine[count] = nextArray[arrayIndex]
            imDelayLine[count] = nextArray[arrayIndex + 1]
            var index = count
            if ((arrayIndex / 2).rem(decimation) == 0) {
                for (i in 0 until length) {
                    result[arrayIndex / decimation] += coefs[i] * reDelayLine[index]
                    result[arrayIndex / decimation + 1] += coefs[i] * imDelayLine[index]
                    index--
                    if (index < 0) index = length - 1
                }
                result[arrayIndex / decimation] = result[arrayIndex / decimation] * gain / length
                result[arrayIndex / decimation + 1] = result[arrayIndex / decimation + 1] * gain / length
            }
            if (++count >= length) count = 0

        }
        return result
    }

    override fun hasNext() = input.hasNext()
}

class MovingAverager(
    private val length: Int
) {

    private var out = 0f
    private var out1 = 0f
    private var out2 = 0f
    private val delayLine = ArrayDeque<Float>(length - 1)

    fun filter(x: Float): Float {
        out1 = out
        delayLine.addLast(x)
        out = delayLine.pop()

        val y = x - out1 + out2
        out2 = y
        return y / length
    }

    fun delayedSig() = out
}

/**
 * Implementation of real input moving average DC filter (2 moving averagers).
 *
 * @property input Iterator providing FloatArrays representing real-valued signal to be filtered.
 * @param length FloatArray of FIR coefficients.
 * @property gain Filtered signal gain.
 */
class DCFilterShort(
    private val input: Iterator<FloatArray>,
    length: Int,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val ma0 = MovingAverager(length)
    private val ma1 = MovingAverager(length)

    /**
     * Performs filtering on next array provided by input Iterator.
     *
     * @return FloatArray of filtered real-valued signal based on input.
     */
    override fun next(): FloatArray {
        val nextArray = input.next()
        val result = FloatArray(nextArray.size)
        for (i in nextArray.indices) {
            val y1 = ma0.filter(nextArray[i])
            val y2 = ma1.filter(y1)
            result[i] = gain * (ma0.delayedSig() - y2)
        }

        return result
    }

    override fun hasNext() = input.hasNext()
}

/**
 * Implementation of real input moving average DC filter (4 moving averagers).
 *
 * @property input Iterator providing FloatArrays representing real-valued signal to be filtered.
 * @param length FloatArray of FIR coefficients.
 * @property gain Filtered signal gain.
 */
class DCFilterLong(
    private val input: Iterator<FloatArray>,
    length: Int,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val ma0 = MovingAverager(length)
    private val ma1 = MovingAverager(length)
    private val ma2 = MovingAverager(length)
    private val ma3 = MovingAverager(length)
    private val delayLine = ArrayDeque<Float>(length - 1)

    /**
     * Performs filtering on next array provided by input Iterator.
     *
     * @return FloatArray of filtered real-valued signal based on input.
     */
    override fun next(): FloatArray {
        val nextArray = input.next()
        val result = FloatArray(nextArray.size)
        for (i in nextArray.indices) {
            val y1 = ma0.filter(nextArray[i])
            val y2 = ma1.filter(y1)
            val y3 = ma2.filter(y2)
            val y4 = ma3.filter(y3)

            delayLine.addLast(ma0.delayedSig())
            val d = delayLine.pop()
            result[i] = gain * (d - y4)
        }

        return result
    }

    override fun hasNext() = input.hasNext()
}