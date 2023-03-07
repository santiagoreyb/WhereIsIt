package com.example.whereisit

import android.os.Bundle
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ImageAdap : AppCompatActivity() {

    val REQUEST_CODE_CAMERA_PERMISSION = 1
    val REQUEST_CODE_IMAGE_CAPTURE = 2
    val REQUEST_CODE_IMAGE_PICKER = 3
    lateinit var takePhotoButton: Button
    lateinit var galleryButton: Button
    lateinit var imagen: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.img)

        takePhotoButton = findViewById<Button>(R.id.takephoto)
        galleryButton = findViewById<Button>(R.id.gallery)
        imagen = findViewById<ImageView>(R.id.imageView9)

        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
        }

        takePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
            } else {
                openCamera()
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imagen.setImageBitmap(imageBitmap)
        }
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data?.data
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
            imagen.setImageBitmap(bitmap)
        }
    }

}