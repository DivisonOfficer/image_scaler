package io.minerinc.shotag_temp

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.Point
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import kotlin.math.pow

class DetailGesture(private val img : ImageView, val windowWidth : Int, val windowHeight : Int, val bitmapWidth : Int, val bitmapHeight : Int, val nestedEnable : (Boolean) -> Unit, val onSingleTouch : ()->Unit) : View.OnTouchListener {


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

    private val imageWidth : Int
    private val imageHeight : Int
    private val defaultScale : Float
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, _event: MotionEvent?): Boolean {
        _event?.let{event->
            if(event.pointerCount == 2)
                f2 = Point(event.getX(1).toInt(), event.getY(1).toInt())

            f1 = Point(event.x.toInt(), event.y.toInt())


            /**
             * 손가락을 두개 누른 경우 손가락 하나만 필요한 경우와 분리
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


            /**
             * 터치가 중지된 경우 포인터 기록 삭제
             */
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

            /**
             * 손가락 하나일떄 : 이미지 이동
             * 손가락 두개일떄 : 이미지 확대축소
             */
            if(event.action == MotionEvent.ACTION_MOVE)
            {
                latePoint = if(event.pointerCount <2) {
                    Log.e(javaClass.name,"SINGLE MOVE DETACHED")
                    moveImg(latePoint,f1)
                    scaleOldLen = 0
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


    /**
     * 손을 똇을 시 연산된 scale 정보를 old 값으로 지정하여 저장
     */
    private fun onDetachScale(){
        oldScale = scale
        scaleOldLen = 0
    }


    /**
     * 처음 onDown 시 터치한 손가락 간격 계산
     */
    private fun setScaleOldLen()
    {
        f1?:run{
            return
        }
        f2?:run{
            return
        }

        oldScale = scale

        scaleOldLen = (f1!! - f2!!).let{
            kotlin.math.sqrt(it.x.toDouble().pow(2.0) + it.y.toDouble().pow(2.0)).toInt()
        }.apply{
            Log.e(javaClass.name,"OLDLEN REFRESHED $this")
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

        /**
         * 터치 지점의 이동 만큼 현재의 좌표를 변형
         */

        val tx = (to.x - from.x).toFloat()
        val ty = (to.y - from.y).toFloat()
        matrixInfo[Matrix.MTRANS_X] += tx
        matrixInfo[Matrix.MTRANS_Y] += ty



        fitMatrix()
    }

    private fun fitMatrix(){

        /**
         * 연산한 Matrix 데이터를 보정하여 imageview 에 업로드
         * x 좌표와 y 좌표를 이미지 패딩과 현재 이미지 확대 상태에 따라 이미지가 화면을 벗어나지 않도록 보정
         */

        val scale = matrixInfo[Matrix.MSCALE_X]
        val xPadding = (img.width - getMatrixWidth()).coerceAtLeast(0f) / 2
        val yPadding = (img.height - getMatrixHeight()).coerceAtLeast(0f) / 2
        val xEnd = if(xPadding > 0f) xPadding else windowWidth * 1 - imageWidth * scale / defaultScale

        if(scale <= defaultScale)
            nestedEnable(true)

        matrixInfo[Matrix.MTRANS_X] = matrixInfo[Matrix.MTRANS_X].coerceAtMost(xPadding).coerceAtLeast(xEnd)



        matrixInfo[Matrix.MTRANS_Y] = matrixInfo[Matrix.MTRANS_Y].coerceAtMost(yPadding).coerceAtLeast(if(yPadding <= 0f) windowHeight - imageHeight * scale / defaultScale else yPadding)
        imgMatrix.setValues(matrixInfo)
        img.imageMatrix = imgMatrix
    }


    private fun scaleImg()
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
            kotlin.math.sqrt(it.x.toDouble().pow(2.0) + it.y.toDouble().pow(2.0)).toInt()
        }

        val preScale = scale


        // 처음 터시한 두 손가락의 간극 (scaleOldLen) 과 현재 손가락의 간극 (scaleLen) 의 길이 차이를 현재 scale 값 (oldScale) 에 더하여 새로운 scale 값 지정
        // oldScale 은 터치를 뗄 시 갱신

        val nextScale = (oldScale + (scaleLen - scaleOldLen).toFloat() / img.width.toFloat() * 2.5f).coerceAtLeast(defaultScale).coerceAtMost(defaultScale * 5)

        imgMatrix.getValues(matrixInfo)
        val prevX = matrixInfo[Matrix.MTRANS_X]
        val prevY = matrixInfo[Matrix.MTRANS_Y]
        matrixInfo[Matrix.MSCALE_X] = nextScale
        matrixInfo[Matrix.MSCALE_Y] = nextScale
        val pointX = (f1!! + f2!!).x / 2
        val pointY = (f1!! + f2!!).y / 2

        // 손으로 누르는 곳이 이미지의 동일한 지점을 표시해야 한다. 이때, 터치 좌표는 고정. 즉, 이미지 좌표를 움직여서 보정한다
        // next state x 포인팅 위치 = previous state x 포인팅 위치
        // (nextX - pointX) / nextScale = (prevX - pointX) / prevScale
        matrixInfo[Matrix.MTRANS_X] = pointX + (prevX - pointX) * nextScale / preScale
        matrixInfo[Matrix.MTRANS_Y] = pointY + (prevY - pointY) * nextScale / preScale

        scale = nextScale

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