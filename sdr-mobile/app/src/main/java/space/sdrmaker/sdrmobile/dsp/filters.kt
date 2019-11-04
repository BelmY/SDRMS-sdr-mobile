package space.sdrmaker.sdrmobile.dsp

import java.util.ArrayDeque
import kotlin.math.ceil

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

    override fun next(): FloatArray {
        val nextArray = input.next()
        var result = FloatArray(ceil(nextArray.size.toFloat() / decimation).toInt()) { 0f }
        nextArray.forEachIndexed { arrayIndex, value ->
            run {
                delayLine[count] = value
                var index = count
                if (arrayIndex.rem(decimation) == 0) {
                    for (i in 0 until length) {
                        result[arrayIndex / decimation] += coefs[i] * delayLine[index--]
                        if (index < 0) index = length - 1
                    }
                    result[arrayIndex / decimation] = result[arrayIndex / decimation] * gain / length
                }
                if (++count >= length) count = 0
            }
        }
        return result
    }

    override fun hasNext() = input.hasNext()
}

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

class DCFilterShort(
    private val input: Iterator<FloatArray>,
    length: Int,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val ma0 = MovingAverager(length)
    private val ma1 = MovingAverager(length)

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