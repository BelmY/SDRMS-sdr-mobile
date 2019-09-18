package space.sdrmaker.sdrmobile.dsp

val NOAA_TAPS = floatArrayOf(
    -7.383784e-03f,
    -3.183046e-03f, 2.255039e-03f, 7.461166e-03f, 1.091908e-02f,
    1.149109e-02f, 8.769802e-03f, 3.252932e-03f, -3.720606e-03f,
    -1.027446e-02f, -1.447403e-02f, -1.486427e-02f, -1.092423e-02f,
    -3.307958e-03f, 6.212477e-03f, 1.511364e-02f, 2.072873e-02f,
    2.096037e-02f, 1.492345e-02f, 3.347624e-03f, -1.138407e-02f,
    -2.560252e-02f, -3.507114e-02f, -3.591225e-02f, -2.553830e-02f,
    -3.371569e-03f, 2.882645e-02f, 6.711368e-02f, 1.060042e-01f,
    1.394643e-01f, 1.620650e-01f, 1.700462e-01f, 1.620650e-01f,
    1.394643e-01f, 1.060042e-01f, 6.711368e-02f, 2.882645e-02f,
    -3.371569e-03f, -2.553830e-02f, -3.591225e-02f, -3.507114e-02f,
    -2.560252e-02f, -1.138407e-02f, 3.347624e-03f, 1.492345e-02f,
    2.096037e-02f, 2.072873e-02f, 1.511364e-02f, 6.212477e-03f,
    -3.307958e-03f, -1.092423e-02f, -1.486427e-02f, -1.447403e-02f,
    -1.027446e-02f, -3.720606e-03f, 3.252932e-03f, 8.769802e-03f,
    1.149109e-02f, 1.091908e-02f, 7.461166e-03f, 2.255039e-03f,
    -3.183046e-03f, -7.383784e-03f
)

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

    override fun hasNext() = input.hasNext()
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