package space.sdrmaker.sdrmobile.dsp

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IQFileReader(path: String) : Iterator<Pair<Float, Float>> {

    private var stream = File(path).inputStream().buffered()
    private lateinit var iq: Pair<Float, Float>
    private var closed = false

    init {
        readIQ()
    }

    override fun next(): Pair<Float, Float> {
        val result = iq
        readIQ()
        return result
    }

    private fun readIQ() {
        val bytes = ByteArray(8)
        val read = stream.read(bytes)
        if (read < 8) {
            stream.close()
            closed = true
        }
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        iq = Pair(buffer.float, buffer.float)
    }

    override fun hasNext(): Boolean = !closed

}

class IQFileWriter {
    fun write(input: Iterator<Pair<Float, Float>>, path: String) {
        val stream = File(path).outputStream().buffered()
        var count = 0
        while (input.hasNext()) {
            if (count++.rem(100) == 0) {
                println(count - 1)
            }
            val next = input.next()
            var bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(next.first)
                .putFloat(4, next.second).array()
            stream.write(bytes)
        }
        stream.flush()
        stream.close()
    }
}

class RawFileWriter {
    fun write(input: Iterator<Float>, path: String) {
        val stream = File(path).outputStream().buffered()
        var count = 0
        while (input.hasNext()) {
            if (count++.rem(100) == 0) {
                println(count - 1)
            }
            val next = input.next()
            var bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(next).array()
            stream.write(bytes)
        }
        stream.flush()
        stream.close()
    }
}
