package space.sdrmaker.sdrmobile.benchmarks

internal class FIR(private val coefs: DoubleArray) {
    private val length: Int = coefs.size
    private val delayLine: DoubleArray
    private var count = 0

    init {
        delayLine = DoubleArray(length)
    }

    fun getOutputSample(inputSample: Double): Double {
        delayLine[count] = inputSample
        var result = 0.0
        var index = count
        for (i in 0 until length) {
            result += coefs[i] * delayLine[index--]
            if (index < 0) index = length - 1
        }
        if (++count >= length) count = 0
        return result
    }
}
