package com.example.whereisit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import dalvik.system.InMemoryDexClassLoader

class EditarPerfilActivity : AppCompatActivity() {


    private val REQUEST_CODE_IMAGE_CAPTURE = 2
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_perfil_activity)

        imageView = findViewById(R.id.imageView16)
        imageView.setOnClickListener {
            val imageButtonIntent = Intent(this, ImageAdap::class.java)
            startActivityForResult(imageButtonIntent, REQUEST_CODE_IMAGE_CAPTURE)
        }

        // Carga la imagen en el ImageView si existe
        val imageUriString = getSharedPreferences("my_prefs", Context.MODE_PRIVATE).getString("image_uri", null)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        }

        val enviarButtonListener:Button = findViewById(R.id.button)
        enviarButtonListener.setOnClickListener {
            Toast.makeText(this,"Tus datos de perfil se han actualizado.",Toast.LENGTH_LONG).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageUri = data?.getParcelableExtra<Uri>("imageUri")
            imageView.setImageURI(imageUri)

            // Guarda el URI de la imagen en SharedPreferences
            val editor = getSharedPreferences("my_prefs", Context.MODE_PRIVATE).edit()
            editor.putString("image_uri", imageUri.toString())
            editor.apply()
        }

    }


}