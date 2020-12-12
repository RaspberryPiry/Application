package com.example.rasberrypiryapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt


class DrawView(context: Context?, attrs: AttributeSet) : View(context, attrs) {
    private var drawPath: Path? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private var color: Int? = null
    private var tool: Int? = null
    var matrixSize = 32;

    private fun setupDrawing() {
        drawPath = Path()
        drawPaint = Paint()
        drawPaint!!.isAntiAlias = true
        drawPaint!!.strokeWidth = 50f
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
        color = R.color.white
    }

    fun changeColor(color: Int) {
        this.color = color
        if (tool == PEN) {
            changeToPen()
        } else if (tool == BRUSH) {
            changeToBrush()
        } else if (tool == ERASER) {
            changeToEraser()
        }
    }

    fun changeToBrush() {
        tool = BRUSH
        drawPaint!!.color = color!!
    }

    fun changeToPen() {
        tool = PEN
        val c = ColorUtils.setAlphaComponent(color!!, 255)
        drawPaint!!.color = c
    }

    fun changeToEraser() {
        tool = ERASER
        drawPaint!!.color = ColorUtils.setAlphaComponent(0, 255)
    }

    fun drawDot(width: Int, xPos: Float, yPos: Float) {
        val w = width/matrixSize
        if(xPos - w < 0 || yPos - w < 0 || xPos + w > width || yPos + w > width) return
        drawPaint!!.strokeWidth = w.toFloat();
        val x = (xPos / w).toDouble().roundToInt() * w
        val y = (yPos / w).toDouble().roundToInt() * w
        drawPath!!.moveTo(x.toFloat(), y.toFloat())
        drawPath!!.lineTo(x.toFloat(), y.toFloat())
        drawCanvas!!.drawPath(drawPath!!, drawPaint!!)
        drawPath!!.reset()
    }

    fun drawWithPixelData(pixelData: ArrayList<ArrayList<String>>) {
        val w = width/matrixSize
        drawPaint!!.strokeWidth = w.toFloat();
        var x = 0
        var y = 0
        for (i in 0 until matrixSize) {
            for (j in 0 until matrixSize) {
                x = w * j + w/2
                y = w * i + w/2
                drawPaint!!.color = Color.parseColor("#FF" + pixelData[i][j])
                drawPath!!.moveTo(x.toFloat(), y.toFloat())
                drawPath!!.lineTo(x.toFloat(), y.toFloat())
                drawCanvas!!.drawPath(drawPath!!, drawPaint!!)
                drawPath!!.reset()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val newW = w.toDouble().roundToInt()
        super.onSizeChanged(newW, newW, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(newW, newW, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
        Log.e("you", "w : $w, h : $h, width : $newW, height : $height")
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        canvas.drawPath(drawPath!!, drawPaint!!)
        layoutParams.height = width
        layoutParams = layoutParams
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> drawDot(width, touchX, touchY)
            MotionEvent.ACTION_MOVE -> drawDot(width, touchX, touchY)
            MotionEvent.ACTION_UP -> {}
            else -> return false
        }
        invalidate()
        return true
    }

    fun getPixelString(): String {
        var enterCount = 0
        var result = ""
        var input: Int
        val w = width / matrixSize
        for(i in 0 until matrixSize) {
            for (j in 0 until matrixSize) {
                input = canvasBitmap!!.getPixel(i * w, j * w)
                result += java.lang.String.format("%02x%02x%02x", Color.red(input), Color.green(input), Color.blue(input))
                result += " "
                if(++enterCount % 16 == 0) result += "\n"
            }
        }
        return result
    }

    init {
        setupDrawing()
    }
}