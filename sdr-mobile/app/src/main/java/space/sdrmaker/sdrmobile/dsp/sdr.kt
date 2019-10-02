package space.sdrmaker.sdrmobile.dsp

import com.mantz_it.hackrf_android.Hackrf
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.max


const val PACKET_QUEUE_SIZE = 10


class HackRFSignalSource(
    private val hackrf: Hackrf
) :
    Iterator<FloatArray> {

    private lateinit var queue: ArrayBlockingQueue<ByteArray>
    private var packets: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(PACKET_QUEUE_SIZE)
    private var maxValue = 1f

    override fun hasNext() = true

    override fun next(): FloatArray {
        if (!this::queue.isInitialized) start()
        val packet = packets.poll(1000, TimeUnit.MILLISECONDS)
        return if (packet == null) {
            println("HackTF: compute thread buffer underrun\n")
            FloatArray(2) { 0f }
        } else {
            val result = FloatArray(packet.size) { i -> normalize(packet[i].toFloat())}
            hackrf.returnBufferToBufferPool(packet)
            result
        }
    }

    private fun normalize(value: Float) : Float {
        if(value.absoluteValue > maxValue)
            maxValue = value.absoluteValue

        return value / maxValue
    }

    private fun start() {
        println("HackRF start\n")
        queue = hackrf.startRX()
        thread {
            while (true) {
                val packet =
                    queue.poll(1000, TimeUnit.MILLISECONDS)
                if (packet == null ) {
                    println("HackRF: receive thread buffer underrun\n")
                    continue
                }
                if (!packets.offer(packet)) {
                    println("HackRF: receive thread buffer overrun\n")
                }
            }
        }
    }
}