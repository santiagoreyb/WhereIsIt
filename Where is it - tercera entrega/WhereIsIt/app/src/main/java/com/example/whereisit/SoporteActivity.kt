package com.example.whereisit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast

class SoporteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.soporte_activity)

        val spinnerListener:Spinner = findViewById(R.id.spinnerID)

        val enviarButtonListener:Button = findViewById(R.id.button)
        enviarButtonListener.setOnClickListener {
            val itemSelectedString = spinnerListener.selectedItem.toString()
            Toast.makeText(this,"Su $itemSelectedString se ha enviad@.",Toast.LENGTH_LONG).show()
        }

    }

}
