package space.sdrmaker.sdrmobile.dsp

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.media.AudioTrack
import android.media.AudioRecord.MetricsConstants.CHANNELS
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack.WRITE_BLOCKING
import android.media.AudioTrack.WRITE_NON_BLOCKING
import kotlin.math.cos
import kotlin.math.sin


class FileReader(
    path: String,
    private val blockSize: Int = 16 * 1024
) : Iterator<FloatArray> {

    private var stream = File(path).inputStream().buffered()
    private var closed = false
//    private var timestamp = System.currentTimeMillis()
//    private var counter = 0

    override fun next(): FloatArray {
//        if(counter++ == 1000) {
//            println("Data rate: ${blockSize.toFloat() * 1000 * 1000 / (System.currentTimeMillis() - timestamp)}Sps")
//            timestamp = System.currentTimeMillis()
//            counter = 0
//        }
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

//class IQFileWriter {
//    fun write(input: Iterator<Pair<Float, Float>>, path: String) {
//        val stream = File(path).outputStream().buffered()
//        while (input.hasNext()) {
//            val next = input.next()
//            var bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(next.first)
//                .putFloat(4, next.second).array()
//            stream.write(bytes)
//        }
//        stream.flush()
//        stream.close()
//    }
//}

class FileWriter {

    private var counter = 0

    fun write(input: Iterator<FloatArray>, path: String) {
        val stream = File(path).outputStream().buffered()
        while (input.hasNext() && counter < 500) {
            val nextArray = input.next()
            val bytes = ByteBuffer.allocate(nextArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            for (value in nextArray) {
                bytes.putFloat(value)
            }
            stream.write(bytes.array())
            counter++
            println(counter)
        }
        stream.flush()
        stream.close()
    }
}

//class RawFileWriter {
//    fun write(input: Iterator<Float>, path: String) {
//        val stream = File(path).outputStream().buffered()
//        while (input.hasNext()) {
//            val next = input.next()
//            var bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
//                .putFloat(next).array()
//            stream.write(bytes)
//        }
//        stream.flush()
//        stream.close()
//    }
//}

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
        audioTrack.write(input, 0, input.size, WRITE_NON_BLOCKING)
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
        }
        t++
        return result
    }

}
