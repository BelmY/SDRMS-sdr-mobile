package space.sdrmaker.sdrmobile.dsp

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
            for (i in 0 until factor) {
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

    override fun next(): FloatArray {
        val nextArray = input.next()
        return FloatArray(nextArray.size / factor) { index -> nextArray[index * factor] }
    }

    override fun hasNext() = input.hasNext()
}

class ComplexDownsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    override fun next(): FloatArray {
        val nextArray = input.next()
        val result = FloatArray(nextArray.size / factor)
        for (i in 0 until result.size - 1 step 2) {
            result[i] = nextArray[i * factor]
            result[i + 1] = nextArray[i * factor + 1]
        }
        return result
    }

    override fun hasNext() = input.hasNext()
}

class Decimator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray) : Iterator<FloatArray> {

    private val filter = FIRFilter(input, taps)
    private val downsampler = Downsampler(filter, factor)

    override fun hasNext() = downsampler.hasNext()

    override fun next() = downsampler.next()
}

class ComplexDecimator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray) :
    Iterator<FloatArray> {

    private val filter = ComplexFIRFilter(input, taps)
    private val downsampler = ComplexDownsampler(filter, factor)

    override fun next(): FloatArray {
        return downsampler.next()
    }

    override fun hasNext() = downsampler.hasNext()
}

class Interpolator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray) :
    Iterator<FloatArray> {

    private val upsampler = Upsampler(input, factor)
    private val filter = FIRFilter(upsampler, taps)

    override fun hasNext() = filter.hasNext()

    override fun next(): FloatArray {
        return filter.next()
    }
}

class ComplexInterpolator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray) :
    Iterator<FloatArray> {

    private val upsampler = ComplexUpsampler(input, factor)
    private val filter = ComplexFIRFilter(upsampler, taps)

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