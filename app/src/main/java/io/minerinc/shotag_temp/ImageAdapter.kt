package io.minerinc.shotag_temp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ImageAdapter(val imageList : List<Int>, val nestedEnable : (Boolean) -> Unit, val onSingleTouch : ()->Unit) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_image,parent,false))
    }


    fun refreshScale(){
        listeners.forEach {
            it.refreshScale()
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        listeners.remove(holder.listener)
        super.onViewRecycled(holder)
    }

    val listeners = mutableListOf<DetailGesture>()


    inner class ViewHolder(val root : View) : RecyclerView.ViewHolder(root)
    {

        var listener : DetailGesture? = null

        @SuppressLint("ClickableViewAccessibility")
        fun bind(resourceId : Int)
        {
            val img = root.findViewById<ImageView>(R.id.iv_shot)

            Glide.with(img).asBitmap().load(resourceId).addListener(
                object : RequestListener<Bitmap>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {

                        resource?.let{
                            bitmap ->

                            val listener = DetailGesture(
                                img,
                                windowWidth = root.width,
                                windowHeight = root.height,
                                bitmapWidth = bitmap.width,
                                bitmapHeight = bitmap.height,
                                nestedEnable = nestedEnable,
                                onSingleTouch
                            )
                            img.setOnTouchListener(
                                listener
                            )
                            this@ViewHolder.listener = listener
                            listeners.add(listener)
                        }


                        return false
                    }
                }
            ).into(img)

        }
    }
}