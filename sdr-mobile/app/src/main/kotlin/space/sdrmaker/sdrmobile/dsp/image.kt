package space.sdrmaker.sdrmobile.dsp

import android.graphics.*
import androidx.core.graphics.set
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class SyncedSample(val value: Float, val isSyncA: Boolean = false, val isSyncB: Boolean = false)

class NOAALineSyncer(private val input: Iterator<FloatArray>) : Iterator<Array<SyncedSample>> {

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
    private val syncThreshold = 37

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

    private fun getSyncedSample(): SyncedSample {
        if (window.size < syncPatternLength)
            return SyncedSample(window.peekLast()!!)

        var synca = 0
        var syncb = 0
        for ((counter, sample) in window.withIndex()) {
            if (sample.roundToInt() == syncaSeq[counter])
                synca++
            if (sample.roundToInt() == syncbSeq[counter])
                syncb++
        }

        return SyncedSample(
            window.peekLast()!!,
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

class NOAAImageSink(  // TODO: implement Sink interface
    private val input: Iterator<Array<SyncedSample>>,
    private val outPath: String,
    private val verbose: Boolean = false
) {

    private val lineLenght = 2080
    private var x = 0
    private var y = 0
    private var result = FloatArray(lineLenght)

    fun write() {
        for (samples in input) {
            for (sample in samples) {
                if (y >= lineLenght) {
                    y = 0
                    x++
                    val newResult = FloatArray(lineLenght * (x + 1))
                    System.arraycopy(result, 0, newResult, 0, result.size)
                    result = newResult
                    if (verbose)
                        println("Line $x")
                }

                if (sample.isSyncA) {
                    if (verbose)
                        println("Sync A: ($x, $y)")
                    y = 0
                } else if (sample.isSyncB) {
                    if (verbose)
                        println("Sync B: ($x, $y)")
                    y = lineLenght / 2
                }

                result[lineLenght * x + y] = sample.value
                y++
            }
        }

        val bitmap = Bitmap.createBitmap(
            lineLenght,
            x + 1,
            Bitmap.Config.ARGB_8888
        )
        result.forEachIndexed { index, value ->
            bitmap[index % lineLenght, floor(index.toFloat() / lineLenght).toInt()] =
                ((2f.pow(8).toInt() - 1) shl 24) +
                        ((value * (2f.pow(8).toInt() - 1)).toInt() shl 16) +
                        ((value * (2f.pow(8).toInt() - 1)).toInt() shl 8) +
                        (value * (2f.pow(8).toInt() - 1)).toInt()
        }
        val file = FileOutputStream(outPath)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, file)
        file.close()
    }
}