package io.minerinc.shotag_temp

import android.annotation.SuppressLint
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)



        findViewById<RecyclerView>(R.id.rv_tags).adapter = tagAdapter




        findViewById<ViewPager2>(R.id.vp_root).apply{
            offscreenPageLimit = 1

            adapter = this@MainActivity.adapter

            setOnTouchListener { view, motionEvent ->
                if(motionEvent.pointerCount > 1)
                    isUserInputEnabled = false

                false
            }

            registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        this@MainActivity.adapter.refreshScale()
                    }
                }
            )


        }

    }



    val adapter = ImageAdapter(listOf(
        R.drawable.ic_demo,
        R.drawable.ic_demo_2,
        R.drawable.ic_demo_3,
        R.drawable.ic_demo4,
        R.drawable.ic_newyork,

        ),{
        enablePagerScroll(it)
    })
    {
        findViewById<CardView>(R.id.cv_tags).apply {
            (if(visibility == View.VISIBLE) View.GONE else View.VISIBLE).let{v->
                visibility = v
                findViewById<ImageView>(R.id.iv_back).visibility = v
            }

        }
    }


    private fun enablePagerScroll(status: Boolean) {
        findViewById<ViewPager2>(R.id.vp_root).isUserInputEnabled = status
    }



    val tagAdapter = TagAdapter()

}