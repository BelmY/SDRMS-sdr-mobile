package space.sdrmaker.sdrmobile.dsp


class FIRFilter(private val input: Iterator<Float>, private val coefs: FloatArray) : Iterator<Float> {
    private val length: Int = coefs.size
    private val delayLine: FloatArray
    private var count = 0

    init {
        delayLine = FloatArray(length)
    }

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