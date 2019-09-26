package space.sdrmaker.sdrmobile.dsp

class OldUpsampler(private val input: Iterator<Float>, private val factor: Int) : Iterator<Float> {
    private var state = -1

    override fun next(): Float {
        state++
        return if (state.rem(factor) == 0) {
            state = 0
            input.next()
        } else 0F
    }

    override fun hasNext() = input.hasNext()
}

class Upsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    override fun next(): FloatArray {
        val nextArray = input.next()
        return FloatArray(nextArray.size * factor) { index -> if (index.rem(factor) == 0) nextArray[index / factor] else 0f }
    }

    override fun hasNext() = input.hasNext()
}

class OldComplexUpsampler(
    private val input: Iterator<Pair<Float, Float>>,
    private val factor: Int
) :
    Iterator<Pair<Float, Float>> {
    private var state = -1

    override fun next(): Pair<Float, Float> {
        state++
        return if (state.rem(factor) == 0) {
            state = 0
            input.next()
        } else Pair(0F, 0F)
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
        while(iterator.hasNext()) {
            result[counter++] = iterator.next()
            result[counter++] = iterator.next()
            for (i in 0 until factor) {
                result[counter++] = 0f
                result[counter++] = 0f
            }
        }

        return result
    }

    override fun hasNext() = input.hasNext()
}

class Downsampler(private val input: Iterator<Float>, private val factor: Int) : Iterator<Float> {

    override fun next(): Float {
        val out = input.next()
        for (i in 1 until factor)
            input.next()

        return out
    }

    override fun hasNext() = input.hasNext()
}

class ComplexDownsampler(private val input: Iterator<Pair<Float, Float>>, private val factor: Int) :
    Iterator<Pair<Float, Float>> {

    override fun next(): Pair<Float, Float> {
        val out = input.next()
        for (i in 1 until factor)
            input.next()

        return out
    }

    override fun hasNext() = input.hasNext()
}

class Decimator(input: Iterator<Float>, factor: Int, taps: FloatArray) : Iterator<Float> {

    private val filter = FIRFilter(input, taps)
    private val downsampler = Downsampler(filter, factor)

    override fun hasNext() = downsampler.hasNext()

    override fun next() = downsampler.next()
}

class ComplexDecimator(input: Iterator<Pair<Float, Float>>, factor: Int, taps: FloatArray) :
    Iterator<Pair<Float, Float>> {

    private val filter = ComplexFIRFilter(input, taps)
    private val downsampler = ComplexDownsampler(filter, factor)

    override fun next(): Pair<Float, Float> {
        return downsampler.next()
    }

    override fun hasNext() = downsampler.hasNext()
}

class Interpolator(input: Iterator<Float>, factor: Int, taps: FloatArray) : Iterator<Float> {

    private val upsampler = Upsampler(input, factor)
    private val filter = FIRFilter(upsampler, taps)

    override fun hasNext() = filter.hasNext()

    override fun next(): Float {
        return filter.next()
    }
}

class ComplexInterpolator(input: Iterator<Pair<Float, Float>>, factor: Int, taps: FloatArray) :
    Iterator<Pair<Float, Float>> {

    private val upsampler = ComplexUpsampler(input, factor)
    private val filter = ComplexFIRFilter(upsampler, taps)

    override fun hasNext() = filter.hasNext()

    override fun next(): Pair<Float, Float> {
        return filter.next()
    }
}


class Resampler(
    input: Iterator<Float>,
    interpolation: Int,
    decimation: Int,
    interpolatorTaps: FloatArray,
    decimatorTaps: FloatArray
) : Iterator<Float> {

    private val interpolator = Interpolator(input, interpolation, interpolatorTaps)
    private val decimator = Decimator(interpolator, decimation, decimatorTaps)

    override fun hasNext() = decimator.hasNext()

    override fun next() = decimator.next()
}

class ComplexResampler(
    input: Iterator<Pair<Float, Float>>,
    interpolation: Int,
    decimation: Int,
    interpolatorTaps: FloatArray,
    decimatorTaps: FloatArray
) :
    Iterator<Pair<Float, Float>> {

    private val interpolator = ComplexInterpolator(input, interpolation, interpolatorTaps)
    private val decimator = ComplexDecimator(interpolator, decimation, decimatorTaps)

    override fun hasNext() = decimator.hasNext()

    override fun next() = decimator.next()
}