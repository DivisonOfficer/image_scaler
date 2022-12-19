package io.minerinc.shotag_temp

import android.graphics.Matrix
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import java.lang.Math.pow
import java.lang.Math.sqrt

class DetailGesture(val img : ImageView, val windowWidth : Int, val windowHeight : Int, val bitmapWidth : Int, val bitmapHeight : Int, val nestedEnable : (Boolean) -> Unit, val onSingleTouch : ()->Unit) : View.OnTouchListener {


    private var oldF1 : Point? = null
    private var oldF2 : Point? = null


    private var canMove = true




    private var latePoint : Point? = null
    private var f1 : Point? = null
    private var f2 : Point? = null

    private var scaleOldLen = 0



    private var oldScale = 0f
    private var scale = 1f


    private val imgMatrix : Matrix = img.imageMatrix

    private val matrixInfo = FloatArray(9)

    val imageWidth : Int
    val imageHeight : Int
    val defaultScale : Float
    init{
        val wRatio = windowWidth.toFloat() / windowHeight.toFloat()
        val bRatio = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        val ratio = if(bRatio < wRatio) bitmapHeight.toFloat() / windowHeight.toFloat() else bitmapWidth.toFloat() / windowWidth.toFloat()
        defaultScale = 1/ratio
        imageWidth = if(bRatio > wRatio) windowWidth else (windowHeight * bRatio).toInt()
        imageHeight = if(bRatio > wRatio) (windowWidth / bRatio).toInt() else windowHeight


        Log.d(javaClass.name,"Img Scale $defaultScale Width $bitmapWidth height $bitmapHeight")
        imgMatrix.postScale(defaultScale,defaultScale)
        img.imageMatrix = imgMatrix
        scale = defaultScale
        imgMatrix.getValues(matrixInfo)
        fitMatrix()

    }

    fun refreshScale(){
        scale = defaultScale
        fitMatrix()
    }



    var transX = 0f
    var transY = 0f


    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        event?.let{event->
            if(event.pointerCount == 2)
                f2 = Point(event.getX(1).toInt(), event.getY(1).toInt())

            f1 = Point(event.x.toInt(), event.y.toInt())

            /**
             * 터치가 중지된 경우 포인터 기록 삭제
             */
            if(event.pointerCount > 1)
                canMove = false

            if(event.action == MotionEvent.ACTION_DOWN)
            {
                oldF2 = f2
                latePoint = f1
                oldScale = scale
                oldF1 = f1
                setScaleOldLen()
                return true
            }

            if(event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
            {

                if(event.pointerCount < 2) {
                    (f1!! - oldF1!!).let{
                        if(it.x * it.x + it.y * it.y < 10)
                            onSingleTouch()
                    }
                    canMove = true
                }

                f2 = null
                f1 = null

                onDetachScale()
                return true
            }

            if(event.action == MotionEvent.ACTION_MOVE)
            {
                latePoint = if(event.pointerCount <2) {
                    moveImg(latePoint,f1)
                    f1
                } else{
                    nestedEnable(false)
                    scaleImg()
                    Point ((f1!!.x + f2!!.x)/2, (f2!!.y + f1!!.y)/2  )
                }
                return true

            }
        }
        return false
    }

    private fun onDetachScale(){
        oldScale = scale
        scaleOldLen = 0
    }

    private fun setScaleOldLen()
    {
        f1?:run{
            return
        }
        f2?:run{
            return
        }

        scaleOldLen = (f1!! - f2!!).let{
            sqrt(pow(it.x.toDouble(), 2.0) + pow(it.y.toDouble(),2.0)).toInt()
        }
    }



    private fun moveImg(from : Point?, to : Point?)
    {
        if(!canMove)
            return
        from?:run{
            return
        }
        to?:run{
            return
        }
        val tx = (to.x - from.x).toFloat()
        val ty = (to.y - from.y).toFloat()
        matrixInfo[Matrix.MTRANS_X] += tx
        matrixInfo[Matrix.MTRANS_Y] += ty



        fitMatrix(onMove = true)
    }

    private fun fitMatrix(onMove : Boolean = false){
        val scale = matrixInfo[Matrix.MSCALE_X]
        val xpading = (img.width - getMatrixWidth()).coerceAtLeast(0f) / 2
        val ypading = (img.height - getMatrixHeight()).coerceAtLeast(0f) / 2
        Log.i(javaClass.name,"Xpading $xpading Ypading $ypading ${img.height} ${getMatrixHeight()} ${(img.height - imageHeight)}")
        val xEnd = if(xpading > 0f) xpading else windowWidth * 1 - imageWidth * scale / defaultScale

        if(scale <= defaultScale)
            nestedEnable(true)

        matrixInfo[Matrix.MTRANS_X] = matrixInfo[Matrix.MTRANS_X].coerceAtMost(xpading).coerceAtLeast(xEnd)



        matrixInfo[Matrix.MTRANS_Y] = matrixInfo[Matrix.MTRANS_Y].coerceAtMost(ypading).coerceAtLeast(if(ypading <= 0f) windowHeight - imageHeight * scale / defaultScale else ypading)
        imgMatrix.setValues(matrixInfo)
        img.imageMatrix = imgMatrix
    }


    fun scaleImg()
    {
        f1?:run{
            return
        }
        f2?:run{
            return
        }

        if(scaleOldLen < 1)
            setScaleOldLen()

        val scaleLen = (f1!! - f2!!).let{
            sqrt(pow(it.x.toDouble(), 2.0) + pow(it.y.toDouble(),2.0)).toInt()
        }

        val preScale = scale
        scale = (oldScale + (scaleLen - scaleOldLen).toFloat() / img.width.toFloat()).coerceAtLeast(defaultScale).coerceAtMost(defaultScale * 5)
        imgMatrix.getValues(matrixInfo)
        val x = matrixInfo[Matrix.MTRANS_X]
        val y = matrixInfo[Matrix.MTRANS_Y]
        matrixInfo[Matrix.MSCALE_X] = scale
        matrixInfo[Matrix.MSCALE_Y] = scale
        val ax = (f1!! + f2!!).x / 2
        val ay = (f1!! + f2!!).y / 2
        matrixInfo[Matrix.MTRANS_X] = ax + (x - ax) * scale / preScale
        matrixInfo[Matrix.MTRANS_Y] = ay + (y - ay) * scale / preScale

        fitMatrix()

    }


    private fun getMatrixWidth() : Float
    {
        return imageWidth.toFloat() * scale / defaultScale
    }

    private fun getMatrixHeight() : Float
    {
        return imageHeight.toFloat() * scale / defaultScale
    }



}