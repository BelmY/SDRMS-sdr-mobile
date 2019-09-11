package space.sdrmaker.sdrmobile.dsp

import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IQFileReader (path: String) : Iterator<Pair<Float, Float>> {

    private var stream: FileInputStream = File(path).inputStream()
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
        if(read < 8) {
            stream.close()
            closed = true
        }
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        iq = Pair(buffer.float, buffer.float)
    }

    override fun hasNext(): Boolean = !closed

}
