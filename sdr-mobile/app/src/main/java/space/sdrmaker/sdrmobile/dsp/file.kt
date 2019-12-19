package space.sdrmaker.sdrmobile.dsp.file

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import space.sdrmaker.sdrmobile.dsp.utils.Sink


/**
 * Provides data stream from provided file path as Iterator<FloatArray>.
 * File should be a binary representation of float samples stored in little endian format.
 *
 * @property blockSize Defines the size of returned FloatArrays.
 * @constructor
 *
 * @param path Path to a file from which the data should be taken.
 */
class FileReader(
    path: String,
    private val blockSize: Int = 16 * 1024
) : Iterator<FloatArray> {

    private var stream = File(path).inputStream().buffered()
    private var closed = false

    /**
     * Provides next batch of data from specified file.
     *
     * @return FloatArray of samples.
     */
    override fun next(): FloatArray {
        val bytes = ByteArray(blockSize)
        val read = stream.read(bytes)
        if (read < blockSize) {
            stream.close()
            closed = true
        }
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val floatBuffer = buffer.asFloatBuffer()
        return FloatArray(floatBuffer.capacity()) { index -> floatBuffer[index] }
    }

    override fun hasNext() = !closed

}

/**
 * Writes stream of data to provided file path.
 *
 * @constructor
 *
 * @param path Output file path.
 */
class FileWriter(path: String):
    Sink<FloatArray> {

    private val stream = File(path).outputStream().buffered()

    /**
     * Writes next batch of data to specified file.
     *
     * @param input FloatArray of samples to be stored.
     */
    override fun write(input: FloatArray) {
        val bytes = ByteBuffer.allocate(input.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (value in input) {
            bytes.putFloat(value)
        }
        stream.write(bytes.array())
    }

    /**
     * Closes output file stream. Should be called once all the data was written.
     */
    fun close() {
        stream.flush()
        stream.close()
    }
}

