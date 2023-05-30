package com.example.whereisit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.whereisit.databinding.OsmMapActivityBinding
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class OsmMapActivity: AppCompatActivity() {

    private lateinit var binding:OsmMapActivityBinding
    lateinit var roadManager: RoadManager
    private var roadOverlay: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID)
        binding = OsmMapActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)

        roadManager = OSRMRoadManager(this, "ANDROID")

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val latitudInicial = intent.getStringExtra("puntoInicialLatitud")
        val longitudInicial = intent.getStringExtra("puntoInicialLongitud")
        val latitudFinal = intent.getStringExtra("puntoFinalLatitud")
        val longitudFinal = intent.getStringExtra("puntoFinalLongitud")

        val coordenadaInicial = latitudInicial?.toDouble()
            ?.let { longitudInicial?.toDouble()?.let { it1 -> GeoPoint(it, it1) } }
        val coordenadaFinal = latitudFinal?.toDouble()
            ?.let { longitudFinal?.toDouble()?.let { it1 -> GeoPoint(it, it1) } }

        // Log.i("OSM Inicial","LA: $latitudInicial; LO: $longitudInicial")
        // Log.i("OSM Final","LA: $latitudFinal; LO: $longitudFinal")

        val mapController = binding.osmMap.controller
        mapController.setZoom(15) // Ajusta el nivel de zoom deseado
        mapController.setCenter(coordenadaInicial) // Establece el centro del mapa en el punto de inicio de la ruta

        if (coordenadaInicial != null) {
            if (coordenadaFinal != null) {
                drawRoute(coordenadaInicial,coordenadaFinal)
            }
        }

    }

    private fun drawRoute(start: GeoPoint, finish: GeoPoint) {

        val marker = Marker(binding.osmMap)
        marker.title = "Mi Marcador"
        marker.position = start
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.osmMap.overlays.add(marker)

        val marker2 = Marker(binding.osmMap)
        marker2.title = "Mi Marcador"
        marker2.position = finish
        marker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.osmMap.overlays.add(marker2)

        val routePoints = ArrayList<GeoPoint>()
        routePoints.add(start)
        routePoints.add(finish)
        val road = roadManager.getRoad(routePoints)
        Log.i("OSM_acticity", "Route length: ${road.mLength} klm")
        Log.i("OSM_acticity", "Duration: ${road.mDuration / 60} min")
        roadOverlay?.let { binding.osmMap.overlays.remove(it) }
        roadOverlay = RoadManager.buildRoadOverlay(road)
        roadOverlay?.outlinePaint?.color = Color.BLUE
        roadOverlay?.outlinePaint?.strokeWidth = 10f
        binding.osmMap.overlays.add(roadOverlay)

    }

}