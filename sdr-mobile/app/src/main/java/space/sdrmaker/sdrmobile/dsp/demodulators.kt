package space.sdrmaker.sdrmobile.dsp

import kotlin.math.atan2
import kotlin.math.floor

enum class ModulationType {
    AM, NFM, WFM, LSB, USB
}

val QUADRATURE_RATE = mapOf(
    Pair(ModulationType.AM, 2 * AUDIO_SAMPLE_RATE),
    Pair(ModulationType.NFM, 2 * AUDIO_SAMPLE_RATE),
    Pair(ModulationType.WFM, 2 * AUDIO_SAMPLE_RATE),
    Pair(ModulationType.LSB, 2 * AUDIO_SAMPLE_RATE),
    Pair(ModulationType.USB, 2 * AUDIO_SAMPLE_RATE)
)

class FMDemodulator(
    private val input: Iterator<FloatArray>,
    maxDeviation: Int,
    modulation: ModulationType,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    private val quadratureRate = (QUADRATURE_RATE[modulation] ?: 2 * AUDIO_SAMPLE_RATE).toFloat()
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
