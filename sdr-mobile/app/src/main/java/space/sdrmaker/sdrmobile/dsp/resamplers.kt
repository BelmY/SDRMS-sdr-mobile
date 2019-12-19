package space.sdrmaker.sdrmobile.dsp.resamplers

import space.sdrmaker.sdrmobile.dsp.filters.ComplexFIRFilter
import space.sdrmaker.sdrmobile.dsp.filters.FIRFilter
import kotlin.math.floor

/**
 * Performs upsampling of real-valued input signal by specified factor.
 *
 * @property input Iterator providing FloatArrays of real-valued input signal.
 * @property factor Upsampling factor.
 */
class Upsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    /**
     * Performs upsampling on next array provided by input Iterator.
     *
     * @return FloatArray of upsampled real-valued signal based on input.
     */
    override fun next(): FloatArray {
        val nextArray = input.next()
        return FloatArray(nextArray.size * factor) { index -> if (index.rem(factor) == 0) nextArray[index / factor] else 0f }
    }

    override fun hasNext() = input.hasNext()
}

/**
 * Performs upsampling of complex (interleaved) input signal by specified factor.
 *
 * @property input Iterator providing FloatArrays of complex (interleaved) input signal.
 * @property factor Upsampling factor.
 */
class ComplexUpsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    /**
     * Performs upsampling on next array provided by input Iterator.
     *
     * @return FloatArray of upsampled complex (interleaved) signal based on input.
     */
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

/**
 * Performs downsampling of real-valued input signal by specified factor.
 *
 * @property input Iterator providing FloatArrays of real-valued input signal.
 * @property factor Downsampling factor.
 */
class Downsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    private var rem = 0

    /**
     * Performs downsampling on next array provided by input Iterator.
     *
     * @return FloatArray of downsampled real-valued signal based on input.
     */
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

/**
 * Performs downsampling of complex (interleaved) input signal by specified factor.
 *
 * @property input Iterator providing FloatArrays of complex (interleaved) input signal.
 * @property factor Downsampling factor.
 */
class ComplexDownsampler(private val input: Iterator<FloatArray>, private val factor: Int) :
    Iterator<FloatArray> {

    private var rem = 0

    /**
     * Performs downsampling on next array provided by input Iterator.
     *
     * @return FloatArray of downsampled complex (interleaved) signal based on input.
     */
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

/**
 * Decimates real-valued input signal by specified factor.
 *
 * @param input Iterator providing FloatArrays of real-valued input signal.
 * @param factor Decimation factor.
 * @param taps FIR filter taps.
 * @param gain Decimated signal gain.
 */
class Decimator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray, gain: Float = 1f) :
    Iterator<FloatArray> {

    private val filter = FIRFilter(input, taps, gain = gain)
    private val downsampler = Downsampler(filter, factor)

    override fun hasNext() = downsampler.hasNext()

    /**
     * Decimates next array provided by input Iterator.
     *
     * @return FloatArray of decimated real-valued signal based on input.
     */
    override fun next() = downsampler.next()
}

/**
 * Decimates complex (interleaved) input signal by specified factor.
 *
 * @param input Iterator providing FloatArrays of complex (interleaved) input signal.
 * @param factor Decimation factor.
 * @param taps FIR filter taps.
 * @param gain Decimated signal gain.
 */
class ComplexDecimator(
    input: Iterator<FloatArray>,
    factor: Int,
    taps: FloatArray,
    gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val filter = ComplexFIRFilter(input, taps, gain = gain)
    private val downsampler = ComplexDownsampler(filter, factor)

    /**
     * Decimates next array provided by input Iterator.
     *
     * @return FloatArray of decimated complex (interleaved) signal based on input.
     */
    override fun next(): FloatArray {
        return downsampler.next()
    }

    override fun hasNext() = downsampler.hasNext()
}

/**
 * Interpolates real-valued input signal by specified factor.
 *
 * @param input Iterator providing FloatArrays of real-valued input signal.
 * @param factor Interpolation factor.
 * @param taps FIR filter taps.
 * @param gain Interpolated signal gain.
 */
class Interpolator(input: Iterator<FloatArray>, factor: Int, taps: FloatArray, gain: Float = 1f) :
    Iterator<FloatArray> {

    private val upsampler = Upsampler(input, factor)
    private val filter = FIRFilter(upsampler, taps, gain = gain)

    override fun hasNext() = filter.hasNext()

    /**
     * Performs interpolation on next array provided by input Iterator.
     *
     * @return FloatArray of interpolated real-valued signal based on input.
     */
    override fun next(): FloatArray {
        return filter.next()
    }
}

/**
 * Performs interpolation of complex (interleaved) input signal by specified factor.
 *
 * @param input Iterator providing FloatArrays of complex (interleaved) input signal.
 * @param factor Interpolation factor.
 * @param taps FIR filter taps.
 * @param gain Interpolated signal gain.
 */
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

    /**
     * Performs interpolation on next array provided by input Iterator.
     *
     * @return FloatArray of interpolated complex (interleaved) signal based on input.
     */
    override fun next(): FloatArray {
        return filter.next()
    }
}

/**
 * Resamples real-valued input signal by performing interpolation and decimation.
 *
 * @param input Iterator providing FloatArrays of real-valued input signal.
 * @param interpolation Interpolation factor.
 * @param decimation Decimation factor.
 * @param interpolatorTaps FIR taps used by interpolator.
 * @param decimatorTaps FIR taps used by decimator.
 */
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

    /**
     * Performs resampling on next array provided by input Iterator.
     *
     * @return FloatArray of resampled real-valued signal based on input.
     */
    override fun next() = decimator.next()
}

/**
 * Resamples complex (interleaved) input signal by performing interpolation and decimation.
 *
 * @param input Iterator providing FloatArrays of real-valued input signal.
 * @param interpolation Interpolation factor.
 * @param decimation Decimation factor.
 * @param interpolatorTaps FIR taps used by interpolator.
 * @param decimatorTaps FIR taps used by decimator.
 */
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

    /**
     * Performs resampling on next array provided by input Iterator.
     *
     * @return FloatArray of resampled complex (interleaved) signal based on input.
     */
    override fun next() = decimator.next()
}