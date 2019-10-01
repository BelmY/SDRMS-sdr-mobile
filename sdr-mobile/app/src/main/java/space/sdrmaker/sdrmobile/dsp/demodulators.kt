package space.sdrmaker.sdrmobile.dsp

import kotlin.math.atan2
import kotlin.math.floor

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
        val result = FloatArray(floor(nextArray.size.toFloat() / 2).toInt())
        var resultCounter = 0
        while(iterator.hasNext()) {
            if (!this::previousSample.isInitialized) previousSample =
                Pair(iterator.next(), iterator.next())
            val re = iterator.next()
            val im = if(iterator.hasNext()) iterator.next() else break
            val sample = Pair(re, im)

            // Quadrature demodulation
            var reOut = sample.first * previousSample.first + sample.second * previousSample.second
            var imOut = sample.second * previousSample.first - sample.first * previousSample.second

            previousSample = sample
            result[resultCounter++] = quadratureGain * atan2(imOut.toDouble(), reOut.toDouble()).toFloat()
        }
        return result
    }

    override fun hasNext() = input.hasNext()

}