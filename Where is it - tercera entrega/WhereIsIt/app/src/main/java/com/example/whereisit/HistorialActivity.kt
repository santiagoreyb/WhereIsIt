package com.example.whereisit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class HistorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial_activity)

        var listViewListener:ListView = findViewById(R.id.HistorialListViewID)

        val jsonArray:JSONArray = loadJsonElementsIntoJsonArray()
        var normalArray:Array<String> = fromJsonArrayToArray(jsonArray)
        normalArray = cleanArrayListOfNulls(normalArray)
        val finalListView = loadJsonElementsIntoListView(normalArray)

        listViewListener = finalListView

    }

    private fun loadJSONFromAsset(): String {
        var json = ""
        try {
            val pathFile = getExternalFilesDir(null)
            val fileName = "locations.json"
            val file = File(pathFile, fileName)
            val inputStream = FileInputStream(file)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return json
    }

    private fun loadJsonElementsIntoJsonArray ( ) : JSONArray {

        val json = JSONArray(loadJSONFromAsset())

        for (i in 0 until json.length()) {
            if (!json.isNull(i)) {
                val jsonObject = json.getJSONObject(i)
                val latitudStringFromJO = jsonObject.optString("latitud")
                val longitudStringFromJO = jsonObject.optString("longitud")
                val dateStringFromJO = jsonObject.optString("date")
                val field = "Latitud: $latitudStringFromJO\nLongitud: $longitudStringFromJO\nTime: $dateStringFromJO"
                json.put(i, field)
            }
        }

        return json
    }

    private fun fromJsonArrayToArray ( paisesJsonArray:JSONArray ) : Array<String> {

        return Array(paisesJsonArray.length()) { paisesJsonArray.getString(it) }

    }

    private fun cleanArrayListOfNulls ( array: Array<String> ) : Array<String> {

        var newArray = mutableListOf<String>()

        for (i in array.indices) {
            if (array[i] != "null") {
                newArray.add(array[i])
            }
        }

        return newArray.toTypedArray()
    }

    private fun loadJsonElementsIntoListView ( array:Array<String> ) : ListView {

        // LOADING JSON ARRAY into LISTVIEW
        val adapter = ArrayAdapter(this , android.R.layout.simple_list_item_1 , array)
        val listView: ListView = findViewById(R.id.HistorialListViewID)
        listView.adapter = adapter

        return listView
    }

}