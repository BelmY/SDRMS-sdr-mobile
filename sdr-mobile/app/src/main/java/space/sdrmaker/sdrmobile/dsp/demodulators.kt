package space.sdrmaker.sdrmobile.dsp

import kotlin.math.atan2

const val AUDIO_RATE = 48000

enum class ModulationType {
    AM, NFM, WFM, LSB, USB
}

val QUADRATURE_RATE = mapOf(
    Pair(ModulationType.AM, 2 * AUDIO_RATE),
    Pair(ModulationType.NFM, 2 * AUDIO_RATE),
    Pair(ModulationType.WFM, 8 * AUDIO_RATE),
    Pair(ModulationType.LSB, 2 * AUDIO_RATE),
    Pair(ModulationType.USB, 2 * AUDIO_RATE)
)

class FMDemodulator(
    private val input: Iterator<Pair<Float, Float>>,
    maxDeviation: Int,
    modulation: ModulationType
) :
    Iterator<Float> {

    private val quadratureRate = (QUADRATURE_RATE[modulation] ?: 2 * AUDIO_RATE).toFloat()
    private val quadratureGain = (quadratureRate / (2 * Math.PI * maxDeviation)).toFloat()
    private var previousSample = input.next()

    override fun next(): Float {
        val sample = input.next()

        // Quadrature demodulation
        var reOut = sample.first * previousSample.first + sample.second * previousSample.second
        var imOut = sample.second * previousSample.first - sample.first * previousSample.second

        previousSample = sample
        return quadratureGain * atan2(imOut.toDouble(), reOut.toDouble()).toFloat()
    }

    override fun hasNext() = input.hasNext()

}