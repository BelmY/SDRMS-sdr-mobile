package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.*
import space.sdrmaker.sdrmobile.dsp.Downsampler
import space.sdrmaker.sdrmobile.dsp.FIRFilter
import space.sdrmaker.sdrmobile.dsp.IQFileReader
import space.sdrmaker.sdrmobile.dsp.Upsampler

class UnitTest {
    @Test
    fun test_Upsampler() {
        val data = floatArrayOf(1f, 2f, 3f)
        val upsampler = Upsampler(data.iterator(), 3)
        val out = FloatArray(9) {upsampler.next()}

        assertArrayEquals(
            floatArrayOf(1f, 0f, 0f, 2f, 0f, 0f, 3f, 0f, 0f),
            out,
            0.1f
        )
        assertEquals(false, upsampler.hasNext())
    }

    @Test
    fun test_Downsampler() {
        val data = FloatArray(9) {i -> i.toFloat()}
        val downsampler = Downsampler(data.iterator(), 3)
        val out = FloatArray(3) {downsampler.next()}

        assertArrayEquals(
            floatArrayOf(0f, 3f, 6f),
            out,
            0.1f
        )
        assertEquals(false, downsampler.hasNext())
    }

    @Test
    fun test_FIRFilter() {
        val coeffs = floatArrayOf(1f, 2f, 3f)
        val data = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)
        val filter = FIRFilter(data.iterator(), coeffs)
        val expected = floatArrayOf(1f, 4f, 10f, 16f, 22f, 28f, 34f, 40f, 46f, 52f)
        val out = FloatArray(10) {filter.next()}

        assertArrayEquals(expected, out, 0.1F)
        assertEquals(false, filter.hasNext())
    }

    @Test
    fun test_IQFileReader() {
        val path = this::class.java.classLoader!!.getResource("iqsample.iq").path
        val reader = IQFileReader(path)
        assertEquals(true, reader.hasNext())
        val iq = reader.next()
        assertArrayEquals(
            floatArrayOf(iq.first, iq.second),
            floatArrayOf(-2.21362957e-04f, -6.103702e-05f),
            0.0001f
        )
        assertEquals(false, reader.hasNext())

    }
}
