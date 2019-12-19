package space.sdrmaker.sdrmobile.dsp.utils

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

interface Sink<T> {
    fun write(input: T)
}

/**
 * Generates complex sine wave (quadrature) data stream with specified properties.
 * Samples are in interleaved format.
 *
 * @property frequency Sine wave frequency.
 * @property rate Sampling rate.
 * @property blockSize Size of returned FloatArrays.
 * @property gain Output gain.
 */
class ComplexSineWaveSource(
    private val frequency: Int,
    private val rate: Int,
    private val blockSize: Int,
    private val gain: Int = 1
) : Iterator<FloatArray> {

    private var t = 0

    override fun hasNext() = true

    /**
     * Generate next FloatArray of sine wave data.
     *
     * @return FloatArray of sine wave data.
     */
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

/**
 * Generates sine wave data stream with specified properties.
 *
 * @property frequency Sine wave frequency.
 * @property rate Sampling rate.
 * @property blockSize Size of returned FloatArrays.
 * @property gain Output gain.
 */
class SineWaveSource(
    private val frequency: Int,
    private val rate: Int,
    private val blockSize: Int,
    private val gain: Int = 1
) : Iterator<FloatArray> {

    private var t = 0

    override fun hasNext() = true

    /**
     * Generate next FloatArray of sine wave data.
     *
     * @return FloatArray of sine wave data.
     */
    override fun next() =
        FloatArray(blockSize) { gain * cos(2 * Math.PI * frequency * t++ / rate).toFloat() }
}

/**
 * Copies input data stream to provided queues. Can be used
 * as stream splitter in combination with QueueSource class.
 *
 * @property queues Queues to copy data stream to.
 */
class QueueSink(private vararg val queues: ArrayBlockingQueue<FloatArray>) :
    Sink<FloatArray> {

    /**
     * Copy next batch of data to queues.
     *
     * @param input Input signal to be copied.
     */
    override fun write(input: FloatArray) {
        for (queue in queues) {
            queue.offer(input)
        }
    }
}

/**
 * Provides data stream from specified queue.
 *
 * @property input Queue from which the data should be streamed.
 */
class QueueSource(private val input: ArrayBlockingQueue<FloatArray>) : Iterator<FloatArray> {

    /**
     * Wait for next batch of data to become available in the queue and return it.
     *
     * @return FloatArray of data provided by the queue.
     */
    override fun next(): FloatArray {
        while (true) {
            return input.poll(100, TimeUnit.MILLISECONDS) ?: continue
        }
    }

    override fun hasNext() = true

}