package space.sdrmaker.sdrmobile

import org.junit.Test

import org.junit.Assert.*
import space.sdrmaker.sdrmobile.dsp.FileReader
import space.sdrmaker.sdrmobile.dsp.Multiply

class MathTest {

    @Test
    fun test_Multiply() {
        val data = arrayListOf(
            floatArrayOf(1f, 1f, 2f, 2f),
            floatArrayOf(3f, 3f, 4f, 4f)
        )
        val multiply = Multiply(data.iterator(), data.iterator())
        val expected = arrayListOf(
            floatArrayOf(0f, 2f, 0f, 8f),
            floatArrayOf(0f, 18f, 0f, 32f)
        )
        val out = Array(data.size) { multiply.next() }
        assertEquals(expected.size, out.size)
        for(i in 0 until expected.size) {
            assertArrayEquals(
                expected[i],
                out[i],
                0.1f
            )
        }
        assertEquals(false, multiply.hasNext())
    }

}