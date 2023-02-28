package com.example.whereisit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val iniciar = findViewById<Button>(R.id.button5)
        val registrar = findViewById<Button>(R.id.button6)
        iniciar.setOnClickListener(){
            val intent = Intent(this, IniciarSesion::class.java)
            startActivity(intent)
        }
        registrar.setOnClickListener(){
            val intent = Intent(this, Registrarse::class.java)
            startActivity(intent)
        }
    }
}