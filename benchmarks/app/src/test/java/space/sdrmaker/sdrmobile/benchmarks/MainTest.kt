package space.sdrmaker.sdrmobile.benchmarks

import org.junit.Test

import org.junit.Assert.*

class MainTest {
    @Test
    fun testFIRFloat() {
        val filter = FIRFloat(floatArrayOf(1f, 2f, 3f))
        val data = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)
        val expected = floatArrayOf(1f, 4f, 10f, 16f, 22f, 28f, 34f, 40f, 46f, 52f)
        val filtered: FloatArray = data.map { filter.getOutputSample(it) }.toFloatArray()
        assertArrayEquals(expected, filtered, 0.001f)
    }
    @Test
    fun testFIRShort() {
        val filter = FIRShort(shortArrayOf(1, 2, 3))
        val data = shortArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val expected = intArrayOf(1, 4, 10, 16, 22, 28, 34, 40, 46, 52)
        val filtered: IntArray = data.map { filter.getOutputSample(it) }.toIntArray()
        assertArrayEquals(expected, filtered)
    }

}
