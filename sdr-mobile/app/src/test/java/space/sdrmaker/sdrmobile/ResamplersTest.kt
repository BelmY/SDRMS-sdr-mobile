package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.*
import space.sdrmaker.sdrmobile.dsp.*

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
        assertArrayEquals(
            expected[0],
            out[0],
            0.1f
        )
        assertArrayEquals(
            expected[1],
            out[1],
            0.1f
        )
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
        assertArrayEquals(
            expected[0],
            out[0],
            0.1f
        )
        assertArrayEquals(
            expected[1],
            out[1],
            0.1f
        )
        assertEquals(false, upsampler.hasNext())
    }
//
//    @Test
//    fun test_Downsampler() {
//        val data = FloatArray(9) { i -> i.toFloat() }
//        val downsampler = Downsampler(data.iterator(), 3)
//        val out = FloatArray(3) { downsampler.next() }
//
//        assertArrayEquals(
//            floatArrayOf(0f, 3f, 6f),
//            out,
//            0.1f
//        )
//        assertEquals(false, downsampler.hasNext())
//    }
//
//    @Test
//    fun test_ComplexDownsampler() {
//        val data = Array(9) { i -> Pair(i.toFloat(), i.toFloat()) }
//        val downsampler = ComplexDownsampler(data.iterator(), 3)
//        val out = Array(3) { downsampler.next() }
//        val expected = floatArrayOf(0f, 3f, 6f)
//
//        assertArrayEquals(
//            expected,
//            out.map { p -> p.first }.toFloatArray(),
//            0.1f
//        )
//        assertArrayEquals(
//            expected,
//            out.map { p -> p.second }.toFloatArray(),
//            0.1f
//        )
//        assertEquals(false, downsampler.hasNext())
//    }
//
//    @Test
//    fun test_FIRFilter() {
//        val coeffs = floatArrayOf(1f, 2f, 3f)
//        val data = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f)
//        val filter = FIRFilter(data.iterator(), coeffs)
//        val expected = floatArrayOf(1f, 4f, 10f, 16f, 22f, 28f, 34f, 40f, 46f, 52f)
//        val out = FloatArray(10) { filter.next() }
//
//        assertArrayEquals(expected, out, 0.1F)
//        assertEquals(false, filter.hasNext())
//    }
//
//    @Test
//    fun test_ComplexFIRFilter() {
//        val coeffs = floatArrayOf(1f, 2f, 3f)
//        val data = floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f).map {i -> Pair(i, i)}
//        val filter = ComplexFIRFilter(data.iterator(), coeffs)
//        val expected = floatArrayOf(1f, 4f, 10f, 16f, 22f, 28f, 34f, 40f, 46f, 52f)
//        val out = Array(10) { filter.next() }
//
//        assertArrayEquals(expected, out.map { p -> p.first}.toFloatArray(), 0.1F)
//        assertArrayEquals(expected, out.map { p -> p.second}.toFloatArray(), 0.1F)
//        assertEquals(false, filter.hasNext())
//    }
//
//    @Test
//    fun test_IQFileReader() {
//        val path = this::class.java.classLoader!!.getResource("iqsample.iq").path
//        val reader = FileReader(path)
//        assertEquals(true, reader.hasNext())
//        val iq = reader.next()
//        assertArrayEquals(
//            floatArrayOf(iq.first, iq.second),
//            floatArrayOf(-2.21362957e-04f, -6.103702e-05f),
//            0.0001f
//        )
//        assertEquals(false, reader.hasNext())
//
//    }
}
