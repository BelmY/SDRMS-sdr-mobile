package space.sdrmaker.sdrmobile.dsp

import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.roundToInt

class SyncedSample(val value: Float, val isSyncA: Boolean = false, isSyncB: Boolean = false)

class NOAALineSyncer (private val input: Iterator<FloatArray>) : Iterator<Array<SyncedSample>> {

    private val syncaSeq = intArrayOf(
        0, 0, 0, 0,
        1, 1, 0, 0,
        1, 1, 0, 0,
        1, 1, 0, 0,
        1, 1, 0, 0,
        1, 1, 0, 0,
        1, 1, 0, 0,
        1, 1, 0, 0,
        0, 0, 0, 0,
        0, 0, 0, 0
    )

    private val syncbSeq = intArrayOf(
        0, 0, 0, 0,
        1, 1, 1, 0, 0,
        1, 1, 1, 0, 0,
        1, 1, 1, 0, 0,
        1, 1, 1, 0, 0,
        1, 1, 1, 0, 0,
        1, 1, 1, 0, 0,
        1, 1, 1, 0, 0,
        0
    )

    private val syncPatternLength = 40
    private val syncThreshold = 35

    private val window = LinkedBlockingDeque<Float>(syncPatternLength)

    override fun next(): Array<SyncedSample> {
        val nextArray = input.next()
        val result = Array(nextArray.size) { SyncedSample(0f) }
        for ((counter, sample) in nextArray.withIndex()) {
            windowAppend(sample)
            result[counter] = getSyncedSample()
        }

        return result
    }

    private fun getSyncedSample() : SyncedSample {
        if (window.size < syncPatternLength)
            return SyncedSample(window.peekLast())

        var synca = 0
        var syncb = 0
        for((counter, sample) in window.withIndex()) {
            if (sample.roundToInt() == syncaSeq[counter])
                synca++
            if (sample.roundToInt() == syncbSeq[counter])
                syncb++
        }

        return SyncedSample(
            window.peekLast(),
            synca > syncThreshold,
            syncb > syncThreshold
        )
    }

    private fun windowAppend(value: Float) {
        while (window.size >= syncPatternLength)
            window.poll()
        window.add(value)
    }

    override fun hasNext() = input.hasNext()

}