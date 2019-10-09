package space.sdrmaker.sdrmobile.dsp

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
