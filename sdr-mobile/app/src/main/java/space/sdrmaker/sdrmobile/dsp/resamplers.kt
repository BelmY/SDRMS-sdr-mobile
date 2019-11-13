package space.sdrmaker.sdrmobile.dsp

import kotlin.math.floor

class Upsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    override fun next(): FloatArray {
        val nextArray = input.next()
        return FloatArray(nextArray.size * factor) { index -> if (index.rem(factor) == 0) nextArray[index / factor] else 0f }
    }

    override fun hasNext() = input.hasNext()
}

class ComplexUpsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    override fun next(): FloatArray {
        val nextArray = input.next()
        val iterator = nextArray.iterator()
        val result = FloatArray(nextArray.size * factor)
        var counter = 0
        while (iterator.hasNext()) {
            result[counter++] = iterator.next()
            result[counter++] = iterator.next()
            for (i in 0 until factor - 1) {
                result[counter++] = 0f
                result[counter++] = 0f
            }
        }

        return result
    }

    override fun hasNext() = input.hasNext()
}

class Downsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    private var rem = 0

    override fun next(): FloatArray {
        val nextArray = input.next()
        val offset = (factor - rem).rem(factor)
        val result =
            nextArray.filterIndexed { index, _ -> index.rem(factor) == offset }.toFloatArray()
        rem = (rem + nextArray.size).rem(factor)

        return result
    }

    override fun hasNext() = input.hasNext()
}

class ComplexDownsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    private var rem = 0

    override fun next(): FloatArray {
        val nextArray = input.next()
        val offset = (factor - rem).rem(factor)
        val result =
            nextArray.filterIndexed {
                    index, _ ->
                (floor(index.toDouble() / 2) - offset).rem(factor) == 0.0 }.toFloatArray()
        rem = (rem + nextArray.size / 2).rem(factor)
        return result
    }

    override fun hasNext() = input.hasNext()
}

class Decimator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray, gain: Float = 1f) :
    Iterator<FloatArray> {

    private val filter = FIRFilter(input, taps, gain = gain)
    private val downsampler = Downsampler(filter, factor)

    override fun hasNext() = downsampler.hasNext()

    override fun next() = downsampler.next()
}

class ComplexDecimator(
    input: Iterator<FloatArray>,
    factor: Int,
    taps: FloatArray,
    gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val filter = ComplexFIRFilter(input, taps, gain = gain)
    private val downsampler = ComplexDownsampler(filter, factor)

    override fun next(): FloatArray {
        return downsampler.next()
    }

    override fun hasNext() = downsampler.hasNext()
}

class Interpolator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray, gain: Float = 1f) :
    Iterator<FloatArray> {

    private val upsampler = Upsampler(input, factor)
    private val filter = FIRFilter(upsampler, taps, gain = gain)

    override fun hasNext() = filter.hasNext()

    override fun next(): FloatArray {
        return filter.next()
    }
}

class ComplexInterpolator(
    input: Iterator<FloatArray>,
    factor: Int,
    taps: FloatArray,
    gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val upsampler = ComplexUpsampler(input, factor)
    private val filter = ComplexFIRFilter(upsampler, taps, gain = gain)

    override fun hasNext() = filter.hasNext()

    override fun next(): FloatArray {
        return filter.next()
    }
}

class Resampler(
    input: Iterator<FloatArray>,
    interpolation: Int,
    decimation: Int,
    interpolatorTaps: FloatArray,
    decimatorTaps: FloatArray
) : Iterator<FloatArray> {

    private val interpolator = Interpolator(input, interpolation, interpolatorTaps)
    private val decimator = Decimator(interpolator, decimation, decimatorTaps)

    override fun hasNext() = decimator.hasNext()

    override fun next() = decimator.next()
}

class ComplexResampler(
    input: Iterator<FloatArray>,
    interpolation: Int,
    decimation: Int,
    interpolatorTaps: FloatArray,
    decimatorTaps: FloatArray
) :
    Iterator<FloatArray> {

    private val interpolator = ComplexInterpolator(input, interpolation, interpolatorTaps)
    private val decimator = ComplexDecimator(interpolator, decimation, decimatorTaps)

    override fun hasNext() = decimator.hasNext()

    override fun next() = decimator.next()
}