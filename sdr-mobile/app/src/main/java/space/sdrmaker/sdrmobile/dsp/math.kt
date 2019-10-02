package space.sdrmaker.sdrmobile.dsp

class Multiply(private val input1: Iterator<FloatArray>, private val input2: Iterator<FloatArray>) :
    Iterator<FloatArray> {

    override fun hasNext() = input1.hasNext() && input2.hasNext()

    override fun next(): FloatArray {
        val array1 = input1.next()
        val array2 = input2.next()
        assert(array1.size == array2.size)
        val size = array1.size
        val result = FloatArray(size)
        for(i in 0 until size - 1 step 2) {
            val (re1, re2, im1, im2) = listOf(array1[i], array2[i], array1[i+1], array2[i+1])
            // real component
            result[i] = re1 * re2 - im1 * im2
            // imaginary component
            result[i+1] = re1 * im2 + im1 * re2
        }

        return result
    }

}
