package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import space.sdrmaker.sdrmobile.dsp.filters.ComplexFIRFilter
import space.sdrmaker.sdrmobile.dsp.filters.FIRFilter


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
    fun test_FIRFilter_decimation() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f),
            floatArrayOf(6f, 7f, 8f, 9f, 10f)
        )
        val filter = FIRFilter(data.iterator(), coeffs, decimation = 3, gain = coeffs.size.toFloat())
        val out = Array(data.size) { filter.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 16f),
            floatArrayOf(34f, 52f)
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
    fun test_FIRFilter_decimate_twice() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f),
            floatArrayOf(6f, 7f, 8f, 9f, 10f),
            floatArrayOf(11f, 12f, 13f, 14f, 15f),
            floatArrayOf(16f, 17f, 18f, 19f, 20f)
        )
        val filter1 = FIRFilter(data.iterator(), coeffs, decimation = 3, gain = coeffs.size.toFloat())
        val out1 = Array(data.size) { filter1.next() }
        val expected1 = arrayListOf(
            floatArrayOf(1f, 16f),
            floatArrayOf(34f, 52f),
            floatArrayOf(70f),
            floatArrayOf(88f, 106f)
        )
        assertEquals(expected1.size, out1.size)
        for(i in 0 until expected1.size) {
            assertArrayEquals(
                expected1[i],
                out1[i],
                0.1f
            )
        }
        assertEquals(false, filter1.hasNext())

        val filter2 = FIRFilter(out1.iterator(), coeffs, decimation = 2, gain = coeffs.size.toFloat())
        val out2 = Array(data.size) { filter2.next() }
        val expected2 = arrayListOf(
            floatArrayOf(1f),
            floatArrayOf(69f),
            floatArrayOf(276f),
            floatArrayOf(492f)
        )
        assertEquals(expected2.size, out2.size)
        for(i in 0 until expected2.size) {
            assertArrayEquals(
                expected2[i],
                out2[i],
                0.1f
            )
        }
        assertEquals(false, filter2.hasNext())
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
