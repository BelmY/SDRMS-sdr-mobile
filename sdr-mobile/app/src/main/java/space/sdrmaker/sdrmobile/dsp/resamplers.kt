package space.sdrmaker.sdrmobile.dsp

val FIR_COEFFS = floatArrayOf(-7.383784e-03f,
-3.183046e-03f,  2.255039e-03f,   7.461166e-03f,   1.091908e-02f,
1.149109e-02f,   8.769802e-03f,   3.252932e-03f,   -3.720606e-03f,
-1.027446e-02f,  -1.447403e-02f,  -1.486427e-02f,  -1.092423e-02f,
-3.307958e-03f,  6.212477e-03f,   1.511364e-02f,   2.072873e-02f,
2.096037e-02f,   1.492345e-02f,   3.347624e-03f,   -1.138407e-02f,
-2.560252e-02f,  -3.507114e-02f,  -3.591225e-02f,  -2.553830e-02f,
-3.371569e-03f,  2.882645e-02f,   6.711368e-02f,   1.060042e-01f,
1.394643e-01f,   1.620650e-01f,   1.700462e-01f,   1.620650e-01f,
1.394643e-01f,   1.060042e-01f,   6.711368e-02f,   2.882645e-02f,
-3.371569e-03f,  -2.553830e-02f,  -3.591225e-02f,  -3.507114e-02f,
-2.560252e-02f,  -1.138407e-02f,  3.347624e-03f,   1.492345e-02f,
2.096037e-02f,   2.072873e-02f,   1.511364e-02f,   6.212477e-03f,
-3.307958e-03f,  -1.092423e-02f,  -1.486427e-02f,  -1.447403e-02f,
-1.027446e-02f,  -3.720606e-03f,  3.252932e-03f,   8.769802e-03f,
1.149109e-02f,   1.091908e-02f,   7.461166e-03f,   2.255039e-03f,
-3.183046e-03f,  -7.383784e-03f)

class Upsampler (private val input: Iterator<Float>, private val factor: Int) : Iterator<Float> {
    var state = -1

    override fun next(): Float {
        state++
        return if (state.rem(factor) == 0) {
            state = 0
            input.next()
        }
        else 0F
    }

    override fun hasNext(): Boolean = input.hasNext()
}

class Downsampler (private val input: Iterator<Float>, private val factor: Int) : Iterator<Float> {

    override fun next(): Float {
        val out = input.next()
        for (i in 1 until factor)
            input.next()

        return out
    }

    override fun hasNext(): Boolean = input.hasNext()
}

class Decimator(input: Iterator<Float>, factor: Int) : Iterator<Float> {

    private val filter = FIRFilter(input, calculateFilterCoeffs())
    private val downsampler = Downsampler(filter, factor)


    override fun hasNext() = downsampler.hasNext()

    override fun next(): Float {
        return downsampler.next()
    }

    private fun calculateFilterCoeffs() = FIR_COEFFS
}

class Interpolator(input: Iterator<Float>, factor: Int) : Iterator<Float> {

    private val upsampler = Upsampler(input, factor)
    private val filter = FIRFilter(upsampler, calculateFilterCoeffs())

    override fun hasNext() = filter.hasNext()

    override fun next(): Float {
        return filter.next()
    }

    private fun calculateFilterCoeffs() = FIR_COEFFS
}

class Resampler(input: Iterator<Float>, interpolation: Int, decimation: Int) : Iterator<Float> {
    private val interpolator = Interpolator(input, interpolation)
    private val decimator = Decimator(interpolator, decimation)

    override fun hasNext() = decimator.hasNext()

    override fun next() = decimator.next()

}