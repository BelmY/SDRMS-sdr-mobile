package space.sdrmaker.sdrmobile.benchmarks

import org.junit.Test

import org.junit.Assert.*

class MainTest {
    @Test
    fun testKotlinFIR() {
        val filter = FIR(floatArrayOf(1f, 2f, 3f))
        val data = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)
        val expected = floatArrayOf(1f, 4f, 10f, 16f, 22f, 28f, 34f, 40f, 46f, 52f)
        val filtered: FloatArray = data.map { filter.getOutputSample(it) }.toFloatArray()
        assertArrayEquals(expected, filtered, 0.001f)
    }
}
