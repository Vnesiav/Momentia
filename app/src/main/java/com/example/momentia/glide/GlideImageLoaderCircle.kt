package com.example.momentia.glide

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.momentia.R

class GlideImageLoaderCircle(private val context: Context) : ImageLoader {
    override fun loadImage(imageUrl: String, imageView: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.image_fail)
            .circleCrop()
            .into(imageView)
    }
}