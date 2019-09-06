package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.*
import space.sdrmaker.sdrmobile.dsp.Upsampler

class UnitTest {
    @Test
    fun test_upsampler() {
        val data = floatArrayOf(1F, 2F, 3F)
        val upsampler = Upsampler(data.iterator(), 3)
        val out = FloatArray(9) {upsampler.next()}

        assertArrayEquals(
            floatArrayOf(1F, 0F, 0F, 2F, 0F, 0F, 3F, 0F, 0F),
            out,
            0.1F
        )

        assertEquals(false, upsampler.hasNext())
    }
}
