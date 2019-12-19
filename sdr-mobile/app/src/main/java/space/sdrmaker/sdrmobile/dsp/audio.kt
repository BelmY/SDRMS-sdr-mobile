package space.sdrmaker.sdrmobile.dsp.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import space.sdrmaker.sdrmobile.dsp.utils.Sink

/**
 * Plays input data as PCM audio stream.
 *
 * @constructor
 *
 * @param sampleRate Audio sample rate.
 */
class AudioSink(sampleRate: Int = 44100) :
    Sink<FloatArray> {

    private val audioTrack: AudioTrack

    init {
        var mBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        if (mBufferSize == AudioTrack.ERROR || mBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            mBufferSize = sampleRate * AudioRecord.MetricsConstants.CHANNELS.toInt() * 2
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
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
            )
            .setBufferSizeInBytes(mBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack.play()
    }

    /**
     * Plays provided samples as PCM audio.
     *
     * @param input Real-valued samples of audio data.
     */
    override fun write(input: FloatArray) {
        audioTrack.write(input, 0, input.size,
            AudioTrack.WRITE_BLOCKING
        )
    }
}