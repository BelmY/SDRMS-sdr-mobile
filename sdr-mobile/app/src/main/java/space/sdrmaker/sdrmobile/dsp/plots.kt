package space.sdrmaker.sdrmobile.dsp.plots

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.get
import androidx.core.graphics.set
import space.sdrmaker.sdrmobile.dsp.utils.Sink
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit


/**
 * Android View displaying FFT plot.
 *
 * @constructor
 *
 * @param context Android View context.
 * @param attrs Android View attributes.
 * @param defStyleAttr
 */
class FFTView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Sink<FloatArray> {

    private val seriesPaint: Paint = Paint()
    private var series: Path = Path()
    private val axisPaint: Paint = Paint()
    private var axis: Path = Path()
    private var labelPaint: Paint = Paint()
    private var gridPaint: Paint = Paint()
    private var grid: Path = Path()

    private var yLabel = "[dB]"
    private var xLabel = "[MHz]"
    private var yMin = -100
    private var yMax = 0
    private var xMin = 0f
    private var xMax = 100f
    private var yTicks: IntArray = (-100 until 0 step 20).toList().toIntArray()
    private var xTicks: FloatArray = (0 until 100 step 20).toList().toIntArray().map {it.toFloat()}.toFloatArray()

    private val yLabelMargin = 10f
    private val labelSize = 20f

    init {
        seriesPaint.isAntiAlias = true
        seriesPaint.style = Paint.Style.STROKE
        seriesPaint.color = Color.BLUE
        seriesPaint.strokeWidth = 2f

        axisPaint.isAntiAlias = true
        axisPaint.style = Paint.Style.STROKE
        axisPaint.color = Color.BLACK
        axisPaint.strokeWidth = 10f

        labelPaint.isAntiAlias = true
        labelPaint.style = Paint.Style.STROKE
        labelPaint.color = Color.BLACK
        labelPaint.strokeWidth = 2f
        labelPaint.textSize = labelSize

        gridPaint.isAntiAlias = true
        gridPaint.alpha = 12
        gridPaint.style = Paint.Style.STROKE
        gridPaint.color = Color.GRAY
        gridPaint.strokeWidth = 2f

    }

    /**
     * Sets scale, label and ticks of frequency axis.
     *
     * @param centerFrequency Center frequency of the receiver.
     * @param bandwidth Receiver bandwidth.
     * @param ticks FloatArray of axis ticks that should be drawn.
     * @param label Axis label, default [MHz].
     */
    fun setupXAxis(
        centerFrequency: Float,
        bandwidth: Float,
        ticks: FloatArray,
        label: String = "[MHz]"
    ) {
        xMin = centerFrequency - bandwidth / 2
        xMax = centerFrequency + bandwidth / 2
        xTicks = ticks
        xLabel = label
    }

    /**
     * Sets scale, label and ticks of Y axis.
     *
     * @param min Minimum value.
     * @param max Maximum value.
     * @param ticks FloatArray of axis ticks that should be drawn.
     * @param label Axis label, default [dB].
     */
    fun setupYAxis(
        min: Int = -100,
        max: Int = 0,
        ticks: IntArray = (-100 until 0 step 20).toList().toIntArray(),
        label: String = "[dB]"
    ) {
        yMin = min
        yMax = max
        yTicks = ticks
        yLabel = label
    }

    private fun drawAxis(canvas: Canvas) {
        axis = Path()
        axis.moveTo(0f, 0f)
        axis.lineTo(0f, height.toFloat())
        axis.lineTo(width.toFloat(), height.toFloat())
        canvas.drawPath(axis, axisPaint)

        grid = Path()
        for (tick in yTicks) {
            grid.moveTo(0f, yTranslate(tick.toFloat()))
            grid.lineTo(width.toFloat(), yTranslate(tick.toFloat()))
        }

        for (tick in xTicks) {
            grid.moveTo(xTranslate(tick.toFloat()), 0f)
            grid.lineTo(xTranslate(tick.toFloat()), height.toFloat())
        }

        canvas.drawPath(grid, gridPaint)

        for (tick in yTicks) {
            canvas.drawText(
                tick.toString(),
                yLabelMargin,
                yTranslate(tick.toFloat()) - labelSize / 2 + 1,
                labelPaint
            )
        }

        for (tick in xTicks) {
            canvas.drawText(
                tick.toString(),
                xTranslate(tick.toFloat()),
                height - labelSize / 2 + 1,
                labelPaint
            )
        }

        canvas.drawText(xLabel, width - 60f, height - labelSize / 2 + 1, labelPaint)
        canvas.drawText(yLabel, yLabelMargin, labelSize + 1, labelPaint)
    }

    private fun xTranslate(value: Float) = (value - xMin) * width / (xMax - xMin)

    private fun yTranslate(value: Float) = height - (value - yMin) * height / (yMax - yMin)

    /**
     * Plots next FloatArray.
     *
     * @param input FloatArray representing power spectrum calculated by FFT.
     */
    override fun write(input: FloatArray) {
        calculatePath(input)
        invalidate()
    }

    private fun calculatePath(fft: FloatArray) {

        series = Path()
        for (i in 0 until width) {
            val value = yTranslate(fft[(i * fft.size / width)])
            if (i == 0) {
                series.moveTo(0f, value)
            } else {
                series.lineTo(i.toFloat(), value)
            }
        }

    }

    override fun onDraw(canvas: Canvas) {
        drawAxis(canvas)
        canvas.drawPath(series, seriesPaint)
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

    private fun translate(value: Float): Float {
//        return height * (1 - value)
        return height - 15 * value
    }

    private fun calculateRow(fft: FloatArray) {
        row = IntArray(width) { i -> fft[i * fft.size / width].toInt() * 500 }
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