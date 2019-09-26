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

val FM_TAPS = floatArrayOf(
    0.04569721334034695f,
    -0.0014984279723340673f,
    0.11503570009461829f,
    -0.10199147438866914f,
    0.2008203889520777f,
    -0.3258053682622002f,
    0.44703675904735474f,
    -0.6630057483154398f,
    0.9487460954814383f,
    -1.2845363633197984f,
    1.7465931430974069f,
    -2.3411039573838255f,
    3.0510179136086815f,
    -3.9442181405341294f,
    5.051015302163319f,
    -6.356317517103702f,
    7.920049984543817f,
    -9.791839751011734f,
    11.957929834381758f,
    -14.46549679808007f,
    17.377101419515782f,
    -20.67894757431064f,
    24.397659439879813f,
    -28.600253940135733f,
    33.27073294311891f,
    -38.40633471755184f,
    44.06839736787468f,
    -50.236469774059174f,
    56.87104242036442f,
    -64.01644485597906f,
    71.64755432912143f,
    -79.68363099230835f,
    88.14142908472839f,
    -96.99429889882619f,
    106.12008345947294f,
    -115.5005827849022f,
    125.1133916142939f,
    -134.80259220473252f,
    144.50893315607334f,
    -154.22535078380292f,
    163.7762584615264f,
    -173.0573851377706f,
    182.09135675666698f,
    -190.7054475823649f,
    198.74360100815912f,
    -206.27570685927355f,
    213.16556359748f,
    -219.1847023081026f,
    224.4707616905682f,
    -228.98862775485046f,
    232.35830435878492f,
    -234.79499459394734f,
    236.7263281942179f,
    -236.5752462675513f,
    236.7263281942179f,
    -234.79499459394734f,
    232.35830435878492f,
    -228.98862775485046f,
    224.4707616905682f,
    -219.1847023081026f,
    213.16556359748f,
    -206.27570685927355f,
    198.74360100815912f,
    -190.7054475823649f,
    182.09135675666698f,
    -173.0573851377706f,
    163.7762584615264f,
    -154.22535078380292f,
    144.50893315607334f,
    -134.80259220473252f,
    125.1133916142939f,
    -115.5005827849022f,
    106.12008345947294f,
    -96.99429889882619f,
    88.14142908472839f,
    -79.68363099230835f,
    71.64755432912143f,
    -64.01644485597906f,
    56.87104242036442f,
    -50.236469774059174f,
    44.06839736787468f,
    -38.40633471755184f,
    33.27073294311891f,
    -28.600253940135733f,
    24.397659439879813f,
    -20.67894757431064f,
    17.377101419515782f,
    -14.46549679808007f,
    11.957929834381758f,
    -9.791839751011734f,
    7.920049984543817f,
    -6.356317517103702f,
    5.051015302163319f,
    -3.9442181405341294f,
    3.0510179136086815f,
    -2.3411039573838255f,
    1.7465931430974069f,
    -1.2845363633197984f,
    0.9487460954814383f,
    -0.6630057483154398f,
    0.44703675904735474f,
    -0.3258053682622002f,
    0.2008203889520777f,
    -0.10199147438866914f,
    0.11503570009461829f,
    -0.0014984279723340673f,
    0.04569721334034695f
)

class OldFIRFilter(private val input: Iterator<Float>, private val coefs: FloatArray) :
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

class FIRFilter(private val input: Iterator<FloatArray>, private val coefs: FloatArray) :
    Iterator<FloatArray> {
    private val length: Int = coefs.size
    private val delayLine = FloatArray(length)
    private var count = 0

    override fun next(): FloatArray {
        val nextArray = input.next()
        var result = FloatArray(nextArray.size) { 0f }
        nextArray.forEachIndexed { arrayIndex, value ->
            run {
                delayLine[count] = value
                var index = count
                for (i in 0 until length) {
                    result[arrayIndex] += coefs[i] * delayLine[index--]
                    if (index < 0) index = length - 1
                }
                if (++count >= length) count = 0
            }
        }
        return result
    }

    override fun hasNext() = input.hasNext()
}

class OldComplexFIRFilter(
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

class ComplexFIRFilter(
    private val input: Iterator<FloatArray>,
    private val coefs: FloatArray
) : Iterator<FloatArray> {
    private val length: Int = coefs.size
    private val reDelayLine = FloatArray(length)
    private val imDelayLine = FloatArray(length)
    private var count = 0

    override fun next(): FloatArray {
        val nextArray = input.next()
        val result = FloatArray(nextArray.size) { 0f }
        val iterator = nextArray.iterator()
        var resultCounter = 0
        while (iterator.hasNext()) {
            reDelayLine[count] = iterator.next()
            imDelayLine[count] = iterator.next()
            var index = count
            for (i in 0 until length) {
                result[resultCounter] += coefs[i] * reDelayLine[index]
                result[resultCounter + 1] += coefs[i] * imDelayLine[index]
                index--
                if (index < 0) index = length - 1
            }
            if (++count >= length) count = 0
            resultCounter += 2

        }
        return result
    }

    override fun hasNext() = input.hasNext()
}