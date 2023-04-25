package com.example.whereisit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.whereisit.databinding.MapsActivityBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.MapStyleOptions
import org.json.JSONArray
import java.io.*
import java.util.*
import kotlin.math.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var initialLatitude: String? = null
    private var initialLongitude: String? = null
    private var firstLatitude: String? = null
    private var firstLongitude: String? = null
    private var elevation : String? = null
    private var latitude : String? = null
    private var longitude : String? = null
    private var latitudePorMarker : String? = null
    private var longitudePorMarker : String? = null

    private lateinit var mMap: GoogleMap
    private lateinit var binding: MapsActivityBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback

    private val RADIUS_OF_EARTH_KM : Float = 6371.01F

    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1

    private lateinit var mGeocoder: Geocoder

    // Limits for the geocoder search (Colombia)
    companion object {
        const val lowerLeftLatitude = 1.396967
        const val lowerLeftLongitude = -78.903968
        const val upperRightLatitude = 11.983639
        const val upperRightLongitude = -71.869905
    }

    var localizaciones = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Verificar si se tiene el permiso de escritura en almacenamiento externo
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        } else {
            // Si ya se tiene el permiso, llamar a la función para guardar el archivo JSON
            writeJSONObject()
        }
        binding = MapsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mLocationRequest = createLocationRequest()

        var distanceDifference: Double?

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this@MapsActivity,
                        R.raw.style_json
                    )
                )

                val location = locationResult.lastLocation
                // Log.i("LOCATION", "Location update in the callback: $location")
                if (location != null) {
                    elevation = location.altitude.toString()
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                }
                distanceDifference = initialLatitude?.let {
                    initialLongitude?.let { it1 ->
                        latitude?.let { it2 ->
                            longitude?.let { it3 ->
                                distance(
                                    it.toDouble(),
                                    it1.toDouble(), it2.toDouble(), it3.toDouble()
                                )
                            }
                        }
                    }
                }
                distanceDifference = distanceDifference?.times(1000)

                if (distanceDifference!! >= 100.0) {
                    mMap.clear()
                    val actualLocation = latitude?.let {
                        location?.let { it1 ->
                            LatLng(
                                it.toDouble(),
                                it1.longitude
                            )
                        }
                    }
                    val listAddressLocation : List<Address> = latitude?.let {
                        longitude?.let { it1 ->
                            mGeocoder.getFromLocation(
                                it.toDouble(), it1.toDouble(),1)
                        }
                    } as List<Address>
                    val nameAddressLocation = listAddressLocation[0].getAddressLine(0).toString()
                    // Log.i("Location",nameAddressLocation)
                    actualLocation?.let {
                        MarkerOptions().position(it).title(nameAddressLocation)
                    }
                        ?.let { mMap.addMarker(it) }
                    actualLocation?.let { CameraUpdateFactory.newLatLngZoom(it, 15F) }
                        ?.let { mMap.moveCamera(it) }
                    writeJSONObject()
                    initialLongitude = longitude
                    initialLatitude = latitude
                    distanceDifference = 0.0
                }
            }
        }
        buscador()

        val menu = findViewById<ImageView>(R.id.TresPuntosImageViewID)
        menu.setOnClickListener(){
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        val calcularRutaListener:Button = findViewById(R.id.CalcularRutaButtonID)
        calcularRutaListener.setOnClickListener {
            val calcularRutaIntent = Intent(this,OsmMapActivity::class.java)
            calcularRutaIntent.putExtra("puntoInicialLatitud",latitude)
            calcularRutaIntent.putExtra("puntoInicialLongitud",longitude)
            calcularRutaIntent.putExtra("puntoFinalLatitud",latitudePorMarker)
            calcularRutaIntent.putExtra("puntoFinalLongitud",longitudePorMarker)
            startActivity(calcularRutaIntent)
        }

        val historialListener:Button = findViewById(R.id.HistorialButtonID)
        historialListener.setOnClickListener {
            val historialIntent = Intent(this,HistorialActivity::class.java)
            startActivity(historialIntent)
        }
        val aniadirListener:Button = findViewById(R.id.AniadirButtonID)
        aniadirListener.setOnClickListener {
            val aniadirIntent = Intent(this,AniadirActivoActivity::class.java)
            startActivity(aniadirIntent)
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near PUJ.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        requestLocationFunction()
    }

    private fun buscador ( ) {
        //Inicialización del objeto
        mGeocoder = Geocoder(baseContext)
        //Cuando se realice la busqueda
        binding.texto.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val addressString = binding.texto.text.toString()
                if (addressString.isNotEmpty()) {
                    try {
                        // Límite de búsqueda
                        val addresses: List<Address>? = mGeocoder.getFromLocationName(
                            addressString,
                            2,
                            lowerLeftLatitude,
                            lowerLeftLongitude,
                            upperRightLatitude,
                            upperRightLongitude
                        )
                        val nameAddressLocation = addresses?.get(0)?.getAddressLine(0).toString()
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressResult = addresses[0]
                            val position = LatLng(addressResult.latitude, addressResult.longitude)
                            longitudePorMarker = addressResult.longitude.toString()
                            latitudePorMarker = addressResult.latitude.toString()
                            mMap.addMarker(MarkerOptions().position(position).title(nameAddressLocation))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15F))
                            // newDistanceForInteraction(addressResult.latitude,addressResult.longitude)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "La dirección esta vacía", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun requestLocationFunction ( ) {
        if (!verificarUbicacionHabilitada()) {
            return
        }
        var isFirstLocationFound = false
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // # Add a marker in Colombia and move the camera
            if (!isFirstLocationFound) {
                firstLatitude = location.latitude.toString()
                firstLongitude = location.longitude.toString()
                isFirstLocationFound = true
            }

            initialLatitude = location.latitude.toString()
            initialLongitude = location.longitude.toString()
            latitudePorMarker = location.latitude.toString()
            longitudePorMarker = location.longitude.toString()

            latitude = initialLatitude
            longitude = initialLongitude
            writeJSONObject()

            val actualLocation = LatLng(location.latitude, location.longitude)

            mMap.addMarker(MarkerOptions().position(actualLocation).title("Actual Location"))
            // mMap.moveCamera(CameraUpdateFactory.newLatLng(actualLocation))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actualLocation, 15F))
            // mMap.moveCamera(CameraUpdateFactory.zoomTo(15F))
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)

        mMap.setOnMapLongClickListener { latLng ->
            // mMap.clear()
            try {
                val listAddressLocation : List<Address> = mGeocoder.getFromLocation(latLng.latitude,latLng.longitude,1) as List<Address>
                val nameAddressLocation = listAddressLocation[0].getAddressLine(0).toString()
                // Log.i("Location",nameAddressLocation)
                longitudePorMarker = latLng.longitude.toString()
                latitudePorMarker = latLng.latitude.toString()
                mMap.addMarker(MarkerOptions().position(latLng).title(nameAddressLocation))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
                newDistanceForInteraction(latLng.latitude,latLng.longitude)
            } catch (e: IOException) {
                Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun newDistanceForInteraction ( newLat:Double , newLng:Double ) {
        var newDistance:Double? = 0.0
        val baseLatitudeForDistance = latitude?.toDouble()
        val baseLongitudeForDistance = longitude?.toDouble()
        newDistance = baseLatitudeForDistance?.let {
            baseLongitudeForDistance?.let { it1 ->
                newLat?.let { it2 ->
                    newLng?.let { it3 ->
                        distance(
                            it, it1, it2, it3
                        )
                    }
                }
            }
        }
        newDistance = newDistance?.times(1000)
        // Log.i("New Distance",newDistance)
        Toast.makeText(this,"La nueva distancia es: $newDistance.",Toast.LENGTH_LONG).show()
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setInterval(10000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                * sin(lngDistance / 2) * sin(lngDistance / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
    }

    private fun writeJSONObject( ) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        } else {
            localizaciones.put(latitude?.let {
                longitude?.let { it1 ->
                    MyLocation(
                        Date(System.currentTimeMillis()), it.toDouble(),
                        it1.toDouble()
                    ).toJSON()
                }
            })
            val output: Writer?
            val filename = "locations.json"
            try {
                val file = File(baseContext.getExternalFilesDir(null), filename)
                Log.i("LOCATION", "Ubicacion de archivo: $file")
                output = BufferedWriter(FileWriter(file))
                output.write(localizaciones.toString())
                output.close()
                Toast.makeText(applicationContext, "Location saved", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun verificarUbicacionHabilitada(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // La ubicación no está habilitada
            AlertDialog.Builder(this)
                .setMessage("Para mostrar la ubicación del usuario, necesitamos que habilite la ubicación del dispositivo")
                .setPositiveButton("Habilitar ubicación") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancelar") { _, _ -> }
                .show()
            return false
        }
        return true
    }

}

