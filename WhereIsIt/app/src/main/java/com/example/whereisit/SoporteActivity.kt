package com.example.whereisit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class SoporteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.soporte_activity)

        val enviarButtonListener:Button = findViewById(R.id.button)
        enviarButtonListener.setOnClickListener {
            Toast.makeText(this,"Su solicitud se ha enviado.",Toast.LENGTH_LONG).show()
        }

    }

}
