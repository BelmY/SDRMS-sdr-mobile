package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.*
import space.sdrmaker.sdrmobile.dsp.*

class FiltersTest {
    @Test
    fun test_FIRFilter() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f),
            floatArrayOf(6f, 7f, 8f, 9f, 10f)
        )
        val filter = FIRFilter(data.iterator(), coeffs, gain = coeffs.size.toFloat())
        val out = Array(data.size) { filter.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 4f, 10f, 16f, 22f),
            floatArrayOf(28f, 34f, 40f, 46f, 52f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, filter.hasNext())
    }

    @Test
    fun test_ComplexFIRFilter() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f, 5f, 5f),
            floatArrayOf(6f, 6f, 7f, 7f, 8f, 8f, 9f, 9f, 10f, 10f)
        )
        val filter = ComplexFIRFilter(data.iterator(), coeffs, gain = coeffs.size.toFloat())
        val expected = arrayListOf(
            floatArrayOf(1f, 1f, 4f, 4f, 10f, 10f, 16f, 16f, 22f, 22f),
            floatArrayOf(28f, 28f, 34f, 34f, 40f, 40f, 46f, 46f, 52f, 52f)
        )
        val out = Array(data.size) { filter.next() }
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, filter.hasNext())
    }
}
