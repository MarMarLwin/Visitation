package com.onthego.onthegovisitation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image_splash.*

class ImageSplashActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_splash)
//        var byteArray = intent.getByteArrayExtra("imageByteArr")
//        var bitmap= BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val filePath = intent.getStringExtra("imagePath")
        val bitmap =
            Utils.getResizedBitmap(
                filePath,
                width,
                height
            )
        splashImageView.setImageBitmap(bitmap)

        cancelImageButton.setOnClickListener {
            this.finish()
        }
    }
}