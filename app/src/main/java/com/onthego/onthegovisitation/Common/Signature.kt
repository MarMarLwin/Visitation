package com.onthego.onthegovisitation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.crashlytics.android.Crashlytics
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

internal class Signature
private constructor(context : Context, attrs : AttributeSet?, _mContent : LinearLayout?) :
        View(context, attrs)
{
    private val paint = Paint()
    private val path = Path()
    private var bitmap : Bitmap? = null
    private var mContent : LinearLayout? = null
    var lastTouchX : Float = 0.toFloat()
    var lastTouchY : Float = 0.toFloat()
    private val dirtyRect = RectF()

    init
    {
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = STROKE_WIDTH
        mContent = _mContent
    }

    fun save(v : View, StoredPath : String)
    {
        Log.v("log_tag", "Width: " + v.width)
        Log.v("log_tag", "Height: " + v.height)
        if (bitmap == null)
        {
            bitmap = Bitmap.createBitmap(mContent !!.width, mContent !!.height, Bitmap.Config.RGB_565)
        }
        val canvas = Canvas(bitmap)
        try
        {
            // Output the file
            val mFileOutStream = FileOutputStream(StoredPath)
            v.draw(canvas)
            // Convert the output file to Image such as .png
            bitmap !!.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream)

            mFileOutStream.flush()
            mFileOutStream.close()
        }
        catch (e : Exception)
        {
            Log.v("log_tag", e.toString())
            Crashlytics.log(e.toString())
        }
    }

    fun clear()
    {
        path.reset()
        invalidate()
    }

    override fun draw(canvas : Canvas?)
    {
        super.draw(canvas)
        canvas !!.drawPath(path, paint)
    }

    override fun dispatchTouchEvent(event : MotionEvent?) : Boolean
    {
//        return super.dispatchTouchEvent(event)
        val eventX = event !!.x
        val eventY = event.y
//        mGetSign.setEnabled(true)
        when (event.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                return true
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP ->
            {
                resetDirtyRect(eventX, eventY)
                val historySize = event.historySize
                for (i in 0 until historySize)
                {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                }
                path.lineTo(eventX, eventY)
            }
            else ->
            {
                debug("Ignored touch event: $event")
                return false
            }
        }

        postInvalidate(
                (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
                (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
                (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
                (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
        )

        lastTouchX = eventX
        lastTouchY = eventY

        return true
    }

    private fun debug(string : String)
    {
        Log.v("log_tag", string)
    }

    private fun expandDirtyRect(historicalX : Float, historicalY : Float)
    {
        if (historicalX < dirtyRect.left)
        {
            dirtyRect.left = historicalX
        }
        else if (historicalX > dirtyRect.right)
        {
            dirtyRect.right = historicalX
        }

        if (historicalY < dirtyRect.top)
        {
            dirtyRect.top = historicalY
        }
        else if (historicalY > dirtyRect.bottom)
        {
            dirtyRect.bottom = historicalY
        }
    }

    private fun resetDirtyRect(eventX : Float, eventY : Float)
    {
        dirtyRect.left = min(lastTouchX, eventX)
        dirtyRect.right = max(lastTouchX, eventX)
        dirtyRect.top = min(lastTouchY, eventY)
        dirtyRect.bottom = max(lastTouchY, eventY)
    }

    companion object
    {
        internal val STROKE_WIDTH = 5f
        internal val HALF_STROKE_WIDTH = STROKE_WIDTH / 2
        private var instance : Signature? = null
        fun getInstance(context : Context, signLayout : LinearLayout) : Signature?
        {
            if (instance == null) instance = Signature(context, null, signLayout)
            return instance
        }
    }
}
