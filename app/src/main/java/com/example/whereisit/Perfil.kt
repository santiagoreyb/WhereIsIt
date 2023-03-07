package com.example.whereisit

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Perfil : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)
        val menu = findViewById<ImageView>(R.id.imageView14)
        menu.setOnClickListener(){
            val intent = Intent(this, maps_class::class.java)
            startActivity(intent)
        }
        val imageButtonListener = findViewById<ImageView>(R.id.imageView3)
        imageButtonListener.setOnClickListener {
            val imageButtonIntent = Intent(this, ImageAdap::class.java)
            startActivity(imageButtonIntent)
        }
        val contactos_textViewListener:TextView = findViewById(R.id.textView15)
        contactos_textViewListener.setOnClickListener {
            val contactos_textViewIntent = Intent(this,VistaContactos::class.java)
            startActivity(contactos_textViewIntent)
        }
    }
}