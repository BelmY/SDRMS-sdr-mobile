package space.sdrmaker.sdrmobile.dsp

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.media.AudioTrack
import android.media.AudioRecord.MetricsConstants.CHANNELS
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack.WRITE_BLOCKING
import kotlin.math.cos
import kotlin.math.sin


class FileReader(
    path: String,
    private val blockSize: Int = 16 * 1024
) : Iterator<FloatArray> {

    private var stream = File(path).inputStream().buffered()
    private var closed = false

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

class BufferedFileReader(
    path: String,
    blockSize: Int = 16 * 1024
) : Iterator<FloatArray> {

    private var stream = File(path).inputStream().buffered()
    private var data = ArrayList<FloatArray>()
    private var iterator: Iterator<FloatArray>

    init {
        var read = blockSize
        while(read == blockSize) {
            val bytes = ByteArray(blockSize)
            read = stream.read(bytes)
            if (read < blockSize) {
                stream.close()
            }
            val buffer = ByteBuffer.wrap(bytes)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            val floatBuffer = buffer.asFloatBuffer()
            data.add(FloatArray(floatBuffer.capacity()) { index -> floatBuffer[index] })
        }
        iterator = data.iterator()
        println("Buffered, len: ${data.size}")
    }

    override fun next() = iterator.next()

    override fun hasNext() = iterator.hasNext()

}

class FileWriter {

    fun write(input: Iterator<FloatArray>, path: String, limit: Int = 0) {
        val stream = File(path).outputStream().buffered()
        var counter = 0
        while (input.hasNext() && (counter < limit || limit == 0)) {
            val nextArray = input.next()
            val bytes = ByteBuffer.allocate(nextArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            for (value in nextArray) {
                bytes.putFloat(value)
            }
            stream.write(bytes.array())
            counter++
        }
        stream.flush()
        stream.close()
    }
}

const val AUDIO_SAMPLE_RATE = 44100

class AudioSink {

    private val audioTrack: AudioTrack

    init {
        var mBufferSize = AudioTrack.getMinBufferSize(
            AUDIO_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        if (mBufferSize == AudioTrack.ERROR || mBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            mBufferSize = AUDIO_SAMPLE_RATE * CHANNELS.toInt() * 2
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(AUDIO_SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
            )
            .setBufferSizeInBytes(mBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack.play()
    }

    fun write(input: FloatArray) {
        audioTrack.write(input, 0, input.size, WRITE_BLOCKING)
    }
}

class ComplexSineWaveSource(
    private val frequency: Int,
    private val rate: Int,
    private val blockSize: Int,
    private val gain: Int = 1
) : Iterator<FloatArray> {

    private var t = 0

    override fun hasNext() = true

    override fun next(): FloatArray {
        val result = FloatArray(blockSize)
        for (i in 0 until blockSize - 1 step 2) {
            result[i] = gain * cos(2 * Math.PI * frequency * t / rate).toFloat()
            result[i + 1] = gain * sin(2 * Math.PI * frequency * t / rate).toFloat()
            t++
        }
        return result
    }
}

class SineWaveSource(
    private val frequency: Int,
    private val rate: Int,
    private val blockSize: Int,
    private val gain: Int = 1
) : Iterator<FloatArray> {

    private var t = 0

    override fun hasNext() = true

    override fun next() =
        FloatArray(blockSize) {gain * cos(2 * Math.PI * frequency * t++ / rate).toFloat()}
}
