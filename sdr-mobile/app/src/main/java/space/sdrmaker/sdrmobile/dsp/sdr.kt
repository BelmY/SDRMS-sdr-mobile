package space.sdrmaker.sdrmobile.dsp

import com.mantz_it.hackrf_android.Hackrf
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


const val PACKET_QUEUE_SIZE = 1024

//class OldHackRFSignalSource(private val hackrf: Hackrf, private val printer: (String) -> Unit) :
//    Iterator<Pair<Float, Float>> {
//
//    var started = false
//    private lateinit var queue: ArrayBlockingQueue<ByteArray>
//    private var packets: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(PACKET_QUEUE_SIZE)
//    private lateinit var packet: ByteArray
//    lateinit var iterator: Iterator<Byte>
//
//    private fun start() {
//        printer("HackRF start\n")
//        queue = hackrf.startRX()
//        started = true
//        thread {
//            while (true) {
////                try {
//                val packet =
//                    queue.poll(1000, TimeUnit.MILLISECONDS) ?: throw BufferUnderflowException()
//                printer("Size: ${packets.size}\n")
//                if (!packets.offer(packet)) {
//                    printer("Buffer overrun")
//                }
//
////                } catch (e: Exception) {
////                    val sw = StringWriter()
////                    val pw = PrintWriter(sw)
////                    e.printStackTrace(pw)
////                    printer(sw.toString())
////                    Thread.sleep(5000)
////                }
//            }
//        }
//
//    }
//
//    private fun nextComplex(): Pair<Float, Float> {
////        try {
//        if (!this::iterator.isInitialized || !iterator.hasNext())
//            poll()
//
//        var re = iterator.next().toFloat()
//        if (!iterator.hasNext()) {
//            poll()
//            re = iterator.next().toFloat()
//        }
//        val im = iterator.next().toFloat()
//
//        return Pair(re, im)
////        } catch (e: BufferUnderflowException) {
////            printer("Buffer underrun")
////            Thread.sleep(10000)
////            return Pair(0f, 0f)
////        }
//    }
//
////    private fun poll() {
////        val buffer = queue.poll(1000, TimeUnit.MILLISECONDS) ?: throw BufferUnderflowException()
////        iterator = buffer.iterator()
////    }
//
//    private fun poll() {
//        if (this::packet.isInitialized)
//            hackrf.returnBufferToBufferPool(packet)
//        packet = queue.poll(1000, TimeUnit.MILLISECONDS) ?: throw BufferUnderflowException()
//        iterator = packet.iterator()
//    }
//
////    private fun poll() {
////        val buffer = packets.poll(1000, TimeUnit.MILLISECONDS) ?: throw BufferUnderflowException()
////        iterator = buffer.iterator()
////    }
//
//    override fun hasNext() = true
//
//    override fun next(): Pair<Float, Float> {
//        if (!started) start()
//        return nextComplex()
//    }
//
//
//}

//class ByteHackRFSignalSource(
//    private val hackrf: Hackrf,
//    private val gain: Float = 1f,
//    private val logger: (String) -> Unit = { msg -> println(msg) }
//) :
//    Iterator<ByteArray> {
//
//    private lateinit var queue: ArrayBlockingQueue<ByteArray>
//    private var packets: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(PACKET_QUEUE_SIZE)
//
//    override fun hasNext() = true
//
//    override fun next(): ByteArray {
//        if (!this::queue.isInitialized) start()
//        val packet = packets.poll(1000, TimeUnit.MILLISECONDS)
//        return if (packet == null) {
//            logger("HackTF: compute thread buffer underrun\n")
//            ByteArray(2) { 0 }
//        } else {
//            val result = FloatArray(packet.size) { i -> packet[i].toFloat() * gain / 128}
//            logger("${result[0]} ${result[1]} ")
//            hackrf.returnBufferToBufferPool(packet)
//            packet
//        }
//    }
//
//    private fun start() {
//        logger("HackRF start\n")
//        queue = hackrf.startRX()
//        thread {
//            while (true) {
//                val packet =
//                    queue.poll(1000, TimeUnit.MILLISECONDS)
//
//                if (packet == null ) {
//                    logger("HackRF: receive thread buffer underrun\n")
//                    continue
//                }
//
////                logger("Size: ${packets.size}\n")
//                if (!packets.offer(packet)) {
//                    logger("HackRF: receive thread buffer overrun\n")
//                }
//            }
//        }
//    }
//}

class HackRFSignalSource(
    private val hackrf: Hackrf,
    private val gain: Float = 1f,
    private val logger: (String) -> Unit = { msg -> println(msg) }
) :
    Iterator<FloatArray> {

    private lateinit var queue: ArrayBlockingQueue<ByteArray>
    private var packets: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(PACKET_QUEUE_SIZE)

    override fun hasNext() = true

    override fun next(): FloatArray {
        if (!this::queue.isInitialized) start()
        val packet = packets.poll(1000, TimeUnit.MILLISECONDS)
        return if (packet == null) {
            logger("HackTF: compute thread buffer underrun\n")
            FloatArray(2) { 0f }
        } else {
            val result = FloatArray(packet.size) { i -> packet[i].toFloat() * gain}
            hackrf.returnBufferToBufferPool(packet)
            result
        }
    }

    private fun start() {
        logger("HackRF start\n")
        queue = hackrf.startRX()
        thread {
            while (true) {
                val packet =
                    queue.poll(1000, TimeUnit.MILLISECONDS)
                if (packet == null ) {
                    logger("HackRF: receive thread buffer underrun\n")
                    continue
                }
                if (!packets.offer(packet)) {
                    logger("HackRF: receive thread buffer overrun\n")
                }
            }
        }
    }
}