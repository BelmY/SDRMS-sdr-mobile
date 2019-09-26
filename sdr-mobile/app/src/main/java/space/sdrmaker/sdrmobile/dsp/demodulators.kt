package space.sdrmaker.sdrmobile.dsp

import kotlin.math.atan2

const val AUDIO_RATE = 48000

enum class ModulationType {
    AM, NFM, WFM, LSB, USB
}

val QUADRATURE_RATE = mapOf(
    Pair(ModulationType.AM, 2 * AUDIO_RATE),
    Pair(ModulationType.NFM, 2 * AUDIO_RATE),
    Pair(ModulationType.WFM, 2 * AUDIO_RATE),
    Pair(ModulationType.LSB, 2 * AUDIO_RATE),
    Pair(ModulationType.USB, 2 * AUDIO_RATE)
)

class OldFMDemodulator(
    private val input: Iterator<Pair<Float, Float>>,
    maxDeviation: Int,
    modulation: ModulationType
) :
    Iterator<Float> {

    private val quadratureRate = (QUADRATURE_RATE[modulation] ?: 2 * AUDIO_RATE).toFloat()
    private val quadratureGain = (quadratureRate / (2 * Math.PI * maxDeviation)).toFloat()
    private lateinit var previousSample: Pair<Float, Float>

    override fun next(): Float {
        if (!this::previousSample.isInitialized) previousSample = input.next()
        val sample = input.next()

        // Quadrature demodulation
        var reOut = sample.first * previousSample.first + sample.second * previousSample.second
        var imOut = sample.second * previousSample.first - sample.first * previousSample.second

        previousSample = sample
        return quadratureGain * atan2(imOut.toDouble(), reOut.toDouble()).toFloat()
    }

    override fun hasNext() = input.hasNext()

}

class FMDemodulator(
    private val input: Iterator<FloatArray>,
    maxDeviation: Int,
    modulation: ModulationType
) :
    Iterator<FloatArray> {

    private val quadratureRate = (QUADRATURE_RATE[modulation] ?: 2 * AUDIO_RATE).toFloat()
    private val quadratureGain = (quadratureRate / (2 * Math.PI * maxDeviation)).toFloat()
    private lateinit var previousSample: Pair<Float, Float>

    override fun next(): FloatArray {
        val nextArray = input.next()
        val iterator = nextArray.iterator()
        val result = FloatArray(nextArray.size / 2)
        var counter = 0
        while(iterator.hasNext()) {
            if (!this::previousSample.isInitialized) previousSample =
                Pair(iterator.next(), iterator.next())
            val sample = Pair(iterator.next(), iterator.next())

            // Quadrature demodulation
            var reOut = sample.first * previousSample.first + sample.second * previousSample.second
            var imOut = sample.second * previousSample.first - sample.first * previousSample.second

            previousSample = sample
            result[counter++] = quadratureGain * atan2(imOut.toDouble(), reOut.toDouble()).toFloat()
        }
        return result
    }

    override fun hasNext() = input.hasNext()

}