package com.example.whereisit

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class Perfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)
        val menu = findViewById<ImageView>(R.id.imageView14)
        menu.setOnClickListener(){
            val intent = Intent(this, maps_class::class.java)
            startActivity(intent)
        }

    }
}