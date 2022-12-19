package io.minerinc.shotag_temp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagAdapter  : RecyclerView.Adapter<TagAdapter.ViewHolder>() {



    val tags = listOf("recommend","need","search")

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTag(tags[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_tag,null))
    }


    inner class ViewHolder(val root : View) : RecyclerView.ViewHolder(root)
    {
        fun bindTag(tag : String)
        {
            root.findViewById<TextView>(R.id.tv_tag).text = tag
            root.setOnClickListener {

            }
        }
    }
}