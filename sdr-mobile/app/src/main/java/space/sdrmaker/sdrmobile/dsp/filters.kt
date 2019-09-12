package space.sdrmaker.sdrmobile.dsp


class FIRFilter(private val input: Iterator<Float>, private val coefs: FloatArray) :
    Iterator<Float> {
    private val length: Int = coefs.size
    private val delayLine = FloatArray(length)
    private var count = 0

    override fun next(): Float {
        val inputSample = input.next()
        delayLine[count] = inputSample
        var result = 0.0F
        var index = count
        for (i in 0 until length) {
            result += coefs[i] * delayLine[index--]
            if (index < 0) index = length - 1
        }
        if (++count >= length) count = 0
        return result
    }

    override fun hasNext(): Boolean = input.hasNext()
}

class ComplexFIRFilter(
    private val input: Iterator<Pair<Float, Float>>,
    private val coefs: FloatArray
) : Iterator<Pair<Float, Float>> {
    private val length: Int = coefs.size
    private val reDelayLine = FloatArray(length)
    private val imDelayLine = FloatArray(length)
    private var count = 0

    override fun next(): Pair<Float, Float> {
        val inputSample = input.next()
        reDelayLine[count] = inputSample.first
        imDelayLine[count] = inputSample.second
        var reResult = 0f
        var imResult = 0f
        var index = count
        for (i in 0 until length) {
            reResult += coefs[i] * reDelayLine[index]
            imResult += coefs[i] * imDelayLine[index]
            index--
            if (index < 0) index = length - 1
        }
        if (++count >= length) count = 0
        return Pair(reResult, imResult)
    }

    override fun hasNext() = input.hasNext()
}