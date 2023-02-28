package com.example.whereisit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class maps_class : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)
        val menu = findViewById<ImageView>(R.id.imageView12)
        menu.setOnClickListener(){
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }
    }
}