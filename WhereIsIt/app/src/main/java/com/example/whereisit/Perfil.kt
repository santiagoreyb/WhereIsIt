package com.example.whereisit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import android.content.Context
import android.widget.Button


class Perfil : AppCompatActivity() {

    private val REQUEST_CODE_IMAGE_CAPTURE = 2
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        imageView = findViewById(R.id.imageView3)
        imageView.setOnClickListener {
            val imageButtonIntent = Intent(this, ImageAdap::class.java)
            startActivityForResult(imageButtonIntent, REQUEST_CODE_IMAGE_CAPTURE)
        }

        val menu = findViewById<ImageView>(R.id.imageView14)
        menu.setOnClickListener(){
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        // Carga la imagen en el ImageView si existe
        val imageUriString = getSharedPreferences("my_prefs", Context.MODE_PRIVATE).getString("image_uri", null)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        }

        val contactos_textViewListener:TextView = findViewById(R.id.textView15)
        contactos_textViewListener.setOnClickListener {
            val contactos_textViewIntent = Intent(this, VistaContactos::class.java)
            startActivity(contactos_textViewIntent)
        }

        val editarPerfilTextView:TextView = findViewById(R.id.textView11)
        editarPerfilTextView.setOnClickListener {
            val editarPerfilIntent = Intent(this,EditarPerfilActivity::class.java)
            startActivity(editarPerfilIntent)
        }

        val soporteButtonTextView:TextView = findViewById(R.id.textView13)
        soporteButtonTextView.setOnClickListener {
            val soporteIntent = Intent(this, SoporteActivity::class.java)
            startActivity(soporteIntent)
        }

        val cerrarSesioonListener_ImageView:ImageView = findViewById(R.id.imageView4)
        cerrarSesioonListener_ImageView.setOnClickListener {
            val cerrarSesioonIntent = Intent(this,MainActivity::class.java)
            startActivity(cerrarSesioonIntent)
        }
        val cerrarSesioonListener_TextView:TextView = findViewById(R.id.textView14)
        cerrarSesioonListener_TextView.setOnClickListener {
            val cerrarSesioonIntent = Intent(this,MainActivity::class.java)
            startActivity(cerrarSesioonIntent)
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
