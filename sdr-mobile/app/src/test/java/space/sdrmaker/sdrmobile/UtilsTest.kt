package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.*
import space.sdrmaker.sdrmobile.dsp.file.FileReader

class UtilsTest {
    @Test
    fun test_IQFileReader() {
        val path = this::class.java.classLoader!!.getResource("iqsample.iq").path
        val reader = FileReader(path, blockSize = 4 * 2)
        assertEquals(true, reader.hasNext())
        val iq = reader.next()
        assertArrayEquals(
            iq,
            floatArrayOf(-2.21362957e-04f, -6.103702e-05f),
            0.0001f
        )
        reader.next()
        assertEquals(false, reader.hasNext())
    }
}