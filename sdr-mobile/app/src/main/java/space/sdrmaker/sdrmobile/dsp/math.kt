package space.sdrmaker.sdrmobile.dsp

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.ceil
import kotlin.math.floor

class Multiply(
    private val input1: Iterator<FloatArray>,
    private val input2: Iterator<FloatArray>,
    private val gain: Float = 1f
) :
    Iterator<FloatArray> {

    override fun hasNext() = input1.hasNext() && input2.hasNext()

    override fun next(): FloatArray {
        val array1 = input1.next()
        val array2 = input2.next()
        assert(array1.size == array2.size)
        val size = array1.size
        val result = FloatArray(size)
        for (i in 0 until size - 1 step 2) {
            // real component
            result[i] = gain * (array1[i] * array2[i] - array1[i + 1] * array2[i + 1])
            // imaginary component
            result[i + 1] = gain * (array1[i] * array2[i + 1] + array1[i + 1] * array2[i])
        }

        return result
    }

}

class HilbertTransform(
    private val input: Iterator<FloatArray>
) : Iterator<FloatArray> {

    private var length = -1
    private lateinit var fft: FloatFFT_1D

    override fun next(): FloatArray {
        val nextArray = input.next()
        if(length < 0) {
            length = nextArray.size
            fft = FloatFFT_1D(length.toLong())
        }
        val dataFFT = FloatArray(length * 2)
        nextArray.copyInto(dataFFT)
        fft.realForwardFull(dataFFT)
        val nyquist = floor(length / 2 + .5).toInt()
        val hilbert = FloatArray(ceil(nextArray.size.toFloat() / 2).toInt()) { index -> if(index < nyquist) 2f else 0f}
        hilbert[0] = 1f
        hilbert[nyquist] = 1f

        for(i in 0 until length) {
            dataFFT[2 * i] *= hilbert[i]
            dataFFT[2 * i + 1] *= hilbert[i]
        }

        fft.complexInverse(dataFFT, false)
        return dataFFT
    }

    override fun hasNext() = input.hasNext()
}
