package space.sdrmaker.sdrmobile.dsp

import com.mantz_it.hackrf_android.Hackrf
import java.nio.BufferUnderflowException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit


class HackRFSignalSource(private val hackrf: Hackrf, private val printer: (String) -> Unit) : Iterator<Pair<Float, Float>> {

    var started = false
    private lateinit var queue: ArrayBlockingQueue<ByteArray>
    lateinit var iterator: Iterator<Byte>

    private fun start() {
        printer("HackRF start\n")
        queue = hackrf.startRX()
        started = true
    }

    private fun nextComplex() : Pair<Float, Float> {
        if(!this::iterator.isInitialized || !iterator.hasNext())
            poll()

        var re = iterator.next().toFloat()
        if(!iterator.hasNext()) {
            poll()
            re = iterator.next().toFloat()
        }
        val im = iterator.next().toFloat()

        return Pair(re, im)
    }

    private fun poll() {
        val buffer = queue.poll(1000, TimeUnit.MILLISECONDS) ?: throw BufferUnderflowException()
        iterator = buffer.iterator()
    }

    override fun hasNext() = true

    override fun next(): Pair<Float, Float> {
        if (!started) start()
        return nextComplex()
    }

}