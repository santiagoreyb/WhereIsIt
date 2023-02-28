package com.example.whereisit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class IniciarSesion : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.iniciar_sesion_activity)
        val iniciar = findViewById<Button>(R.id.button)
        iniciar.setOnClickListener(){
            val intent = Intent(this, maps_class::class.java)
            startActivity(intent)
        }
    }
}