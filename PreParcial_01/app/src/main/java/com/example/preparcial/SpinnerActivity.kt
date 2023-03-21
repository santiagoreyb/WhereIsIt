package com.example.preparcial

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class SpinnerActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spinner)

        val selectedItemOnSpinner = intent.getStringExtra("selectedItem").toString()

        val textViewToSet:TextView = findViewById (R.id.textViewResult)
        val mensageResultado = "¡Hola! $selectedItemOnSpinner"
        textViewToSet.text = mensageResultado
        if(selectedItemOnSpinner.equals(selectedItemOnSpinner)){

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            var latitud = findViewById<TextView>(R.id.latitud)
            var longitud = findViewById<TextView>(R.id.longitud)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                Log.i("LOCATION",
                    "onSuccess location")
                if (location != null) {
                    Log.i("LOCATION", "Longitud: " + location.longitude)
                    Log.i("LOCATION", "Latitud: " + location.latitude)
                    latitud.text = location.latitude.toString();
                    longitud.text = location.longitude.toString();
                }
            }
        }

    }


}

