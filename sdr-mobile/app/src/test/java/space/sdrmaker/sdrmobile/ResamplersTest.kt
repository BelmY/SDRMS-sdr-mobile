package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import space.sdrmaker.sdrmobile.dsp.resamplers.ComplexInterpolator
import space.sdrmaker.sdrmobile.dsp.resamplers.ComplexUpsampler
import space.sdrmaker.sdrmobile.dsp.resamplers.Interpolator
import space.sdrmaker.sdrmobile.dsp.resamplers.Upsampler
import space.sdrmaker.sdrmobile.dsp.resamplers.Downsampler
import space.sdrmaker.sdrmobile.dsp.resamplers.ComplexDownsampler
import space.sdrmaker.sdrmobile.dsp.resamplers.Decimator
import space.sdrmaker.sdrmobile.dsp.resamplers.ComplexDecimator


class ResamplersTest {
    @Test
    fun test_Upsampler() {
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f),
            floatArrayOf(4f, 5f, 6f)
        )
        val upsampler = Upsampler(data.iterator(), 3)
        val out = Array(data.size) { upsampler.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 0f, 0f, 2f, 0f, 0f, 3f, 0f, 0f),
            floatArrayOf(4f, 0f, 0f, 5f, 0f, 0f, 6f, 0f, 0f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, upsampler.hasNext())
    }

    @Test
    fun test_ComplexUpsampler() {
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f),
            floatArrayOf(5f, 6f, 7f, 8f)
        )
        val upsampler = ComplexUpsampler(data.iterator(), 3)
        assertEquals(true, upsampler.hasNext())
        val out = Array(data.size) {upsampler.next()}
        val expected = arrayListOf(
            floatArrayOf(1f, 2f, 0f, 0f, 0f, 0f, 3f, 4f, 0f, 0f, 0f, 0f),
            floatArrayOf(5f, 6f, 0f, 0f, 0f, 0f, 7f, 8f, 0f, 0f, 0f, 0f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, upsampler.hasNext())
    }

    @Test
    fun test_Interpolator() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f),
            floatArrayOf(6f, 7f, 8f, 9f, 10f)
        )
        val interpolator = Interpolator(data.iterator(), 2, coeffs, gain = coeffs.size.toFloat())
        val out = Array(data.size) { interpolator.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 2f, 5f, 4f, 9f, 6f, 13f, 8f, 17f, 10f),
            floatArrayOf(21f, 12f, 25f, 14f, 29f, 16f, 33f, 18f, 37f, 20f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, interpolator.hasNext())
    }

    @Test
    fun test_ComplexInterpolator() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f, 5f, 5f),
            floatArrayOf(6f, 6f, 7f, 7f, 8f, 8f, 9f, 9f, 10f, 10f)
        )
        val intrpolator = ComplexInterpolator(data.iterator(), 2, coeffs, gain = coeffs.size.toFloat())
        val expected = arrayListOf(
            floatArrayOf(1f, 1f, 2f, 2f, 5f, 5f, 4f, 4f, 9f, 9f, 6f, 6f, 13f, 13f, 8f, 8f, 17f, 17f, 10f, 10f),
            floatArrayOf(21f, 21f, 12f, 12f, 25f, 25f, 14f, 14f, 29f, 29f, 16f, 16f, 33f, 33f, 18f, 18f, 37f, 37f, 20f, 20f)
        )
        val out = Array(data.size) { intrpolator.next() }
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, intrpolator.hasNext())
    }

    @Test
    fun test_Downsampler() {
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f),
            floatArrayOf(6f, 7f, 8f, 9f, 10f)
        )
        val downsampler = Downsampler(data.iterator(), 2)
        val out = Array(data.size) { downsampler.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 3f, 5f),
            floatArrayOf(7f, 9f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, downsampler.hasNext())
    }

    @Test
    fun test_ComplexDownsampler() {
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f),
            floatArrayOf(7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f)
        )
        val downsampler = ComplexDownsampler(data.iterator(), 2)
        val out = Array(data.size) { downsampler.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 2f, 5f, 6f),
            floatArrayOf(9f, 10f, 13f, 14f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, downsampler.hasNext())
    }

    @Test
    fun test_Decimator() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 2f, 3f, 4f, 5f),
            floatArrayOf(6f, 7f, 8f, 9f, 10f)
        )
        val decimator = Decimator(data.iterator(), 2, coeffs, gain = coeffs.size.toFloat())
        val out = Array(data.size) { decimator.next() }
        val expected = arrayListOf(
            floatArrayOf(1f, 10f, 22f),
            floatArrayOf(34f, 46f)
        )
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, decimator.hasNext())
    }

    @Test
    fun test_ComplexDecimator() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = arrayListOf(
            floatArrayOf(1f, 1f, 2f, 2f, 3f, 3f, 4f, 4f, 5f, 5f),
            floatArrayOf(6f, 6f, 7f, 7f, 8f, 8f, 9f, 9f, 10f, 10f)
        )
        val decimator = ComplexDecimator(data.iterator(), 2, coeffs, gain = coeffs.size.toFloat())
        val expected = arrayListOf(
            floatArrayOf(1f, 1f, 10f, 10f, 22f, 22f),
            floatArrayOf(34f, 34f, 46f, 46f)
        )

        val out = Array(data.size) { decimator.next() }
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, decimator.hasNext())
    }

}
