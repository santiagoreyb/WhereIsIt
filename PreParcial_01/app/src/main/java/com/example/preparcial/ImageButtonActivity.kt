package com.example.preparcial

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class ImageButtonActivity : AppCompatActivity() {

    val REQUEST_CODE_IMAGE_PICKER = 1
    lateinit var imagen:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_button)
        imagen = findViewById<ImageView>(R.id.imageView_width_ancho_)

        imagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
            imagen.setImageBitmap(bitmap)
        }
    }

}