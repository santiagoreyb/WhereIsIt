
package com.example.preparcial


import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    var selectedItemFromSpinner : String = ""
    lateinit var takePhotoButton: Button

    override fun onCreate ( savedInstanceState : Bundle? ) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonObject:Button = findViewById (R.id.button)
        buttonObject.setOnClickListener {
            val buttonIntent = Intent(this,ButtonActivity::class.java)
            startActivity(buttonIntent)
        }

        val buttonSpinnerObject:Button = findViewById (R.id.buttonSpinner)
        buttonSpinnerObject.setOnClickListener {
            clickOnSpinner()
        }

        val imageButtonObject:ImageButton = findViewById (R.id.imageButton2)
        imageButtonObject.setOnClickListener {
            val imageButtonIntent = Intent(this,ImageButtonActivity::class.java)
            startActivity(imageButtonIntent)
        }

        val textViewObject:TextView = findViewById (R.id.textView)
        textViewObject.setOnClickListener {
            val textViewIntent = Intent(this,TextViewActivity::class.java)
            startActivity(textViewIntent)
        }

    }

    fun clickOnSpinner () {
        val spinnerObject:Spinner = findViewById (R.id.spinner)
        spinnerObject.onItemSelectedListener = this
        val spinnerIntent = Intent(this,SpinnerActivity::class.java)
        spinnerIntent.putExtra("selectedItem",this.selectedItemFromSpinner)
        startActivity(spinnerIntent)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        this.selectedItemFromSpinner = parent?.selectedItem.toString()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}
