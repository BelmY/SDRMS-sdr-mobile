package space.sdrmaker.sdrmobile.dsp

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.media.AudioTrack
import android.media.AudioRecord.MetricsConstants.CHANNELS
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack.WRITE_BLOCKING
import kotlin.math.sin


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

    override fun hasNext() = !closed

}

class IQFileWriter {
    fun write(input: Iterator<Pair<Float, Float>>, path: String) {
        val stream = File(path).outputStream().buffered()
        while (input.hasNext()) {
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
        while (input.hasNext()) {
            val next = input.next()
            var bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(next).array()
            stream.write(bytes)
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

    fun write(input: Iterator<FloatArray>) {
        while (input.hasNext()) {
            val nextBuf = input.next()
            audioTrack.write(nextBuf, 0, nextBuf.size, WRITE_BLOCKING)
        }
    }
}

class SineWaveSource(private val frequency: Int) : Iterator<Float> {

    private var t = 1

    override fun hasNext() = t < AUDIO_RATE * 5

    override fun next(): Float {
        return sin(2 * Math.PI * frequency * t++ / AUDIO_RATE).toFloat()
    }

}
