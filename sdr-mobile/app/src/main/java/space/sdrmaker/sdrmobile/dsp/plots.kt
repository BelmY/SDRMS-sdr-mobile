package space.sdrmaker.sdrmobile.dsp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.get
import androidx.core.graphics.set
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit


class FFTView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint: Paint = Paint()
    private var path: Path = Path()
    private lateinit var queue: ArrayBlockingQueue<FloatArray>
    private var stopRequested = false

    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.BLUE
        mPaint.strokeWidth = 2f
    }

    fun start(newQueue: ArrayBlockingQueue<FloatArray>) {
        queue = newQueue
        stopRequested = false
        while (!stopRequested) {
            val fft = queue.poll(100, TimeUnit.MILLISECONDS) ?: continue
            calculatePath(fft)
            invalidate()
        }
    }

    fun stop() {
        stopRequested = true
    }

    private fun translate(value: Float) : Float {
        return height + 1 - (height / 5) * value
    }

    private fun calculatePath(fft: FloatArray) {

        path = Path()
        for (i in 0 until width) {
            val value = translate(fft[(i * fft.size / width)])
            if (i == 0) {
                path.moveTo(0f, value)
            } else {
                path.lineTo(i.toFloat(), value)
            }
        }

    }

    override fun onDraw(canvas: Canvas) {
        if (!this::queue.isInitialized) return
        canvas.drawPath(path, mPaint)
    }
}

class WaterfallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var queue: ArrayBlockingQueue<FloatArray>
    private lateinit var bitmap: Bitmap
    private lateinit var row: IntArray
    private val mPaint: Paint = Paint()
    private var stopRequested = false
    private var canvasInitialized = false

    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.BLUE
        mPaint.strokeWidth = 1f
    }

    private fun initBitmap() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = Color.GREEN
            }
        }

        println("Bitmap samples: ${bitmap[500, 0]} ${bitmap[500, 3]} ${bitmap[500, 100]}")
    }

    private fun initCanvas(canvas: Canvas) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                canvas.drawPoint(x.toFloat(), y.toFloat(), mPaint)
            }
        }
        canvasInitialized = true
    }

    fun start(newQueue: ArrayBlockingQueue<FloatArray>) {
        queue = newQueue
        stopRequested = false
        initBitmap()
        while (!stopRequested) {
            val fft = queue.poll(100, TimeUnit.MILLISECONDS) ?: continue
            calculateRow(fft)
            invalidate()
        }
    }

    fun stop() {
        stopRequested = true
    }

    private fun translate(value: Float) : Float {
//        return height * (1 - value)
        return height - 15 * value
    }

    private fun calculateRow(fft: FloatArray) {
        row = IntArray(width) {i -> fft[i * fft.size / width].toInt() * 500}
//        println("Row samples: ${row[10]} ${row[100]} ${row[500]}")
    }

    override fun onDraw(canvas: Canvas) {
        if (!this::queue.isInitialized) return
//        if (!canvasInitialized) initCanvas(canvas)
//        println("Redraw")
        canvas.translate(0f, 1f)
        for (x in 0 until width) {
//            mPaint.color = row[x]
//            canvas.drawPoint(x.toFloat(), 0f, mPaint)
            bitmap[x, 0] = row[x]
        }

//        for (x in 0 until width) {
//            for (y in height - 1 downTo 1) {
//                bitmap[x, y] = bitmap[x, y - 1]
//            }
//        }

//        println("Bitmap samples: ${bitmap[500, 0]} ${bitmap[500, 3]} ${bitmap[500, 100]}")

        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
}