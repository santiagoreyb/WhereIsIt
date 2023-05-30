package com.example.whereisit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListaActivosAdapterActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var activosAdapter: ActivosAdapter
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lista_activos_activity)

        database = FirebaseDatabase.getInstance()
        val listaActivos: MutableList<ActivoClass> = mutableListOf()
        val recyclerViewActivos: RecyclerView = findViewById(R.id.ActivosRecyclerViewID)
        activosAdapter = ActivosAdapter(listaActivos, object : ActivosAdapter.OnActivarPosicionClickListener {
            override fun onActivarPosicionClick(activo: ActivoClass, disponibilidad: Boolean) {
                // Cambiar la disponibilidad del activo en la base de datos
                val activoRef = database.getReference("activos/${activo.uid}")
                activoRef.child("disponible").setValue(disponibilidad)

                // Actualizar la disponibilidad del activo en la lista
                activo.isDisponible = disponibilidad
                activosAdapter.notifyDataSetChanged()

                // Manejar el evento de clic en el botón "Ver posición" del activo
                val intent = Intent(this@ListaActivosAdapterActivity, MapsActivityPorPersona::class.java)
                intent.putExtra("uid", activo.uid)
                startActivity(intent)
            }
        })
        recyclerViewActivos.adapter = activosAdapter
        recyclerViewActivos.layoutManager = LinearLayoutManager(this)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        uid = currentUser?.uid ?: ""

        // Obtener la lista de activos desde la base de datos Firebase Realtime Database
        val activosRef = database.getReference("activos")
        activosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listaActivos.clear()

                for (activoSnapshot in dataSnapshot.children) {
                    val activo = activoSnapshot.getValue(ActivoClass::class.java)
                    if (activo != null) {
                        listaActivos.add(activo)
                    }
                }

                activosAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error de la lectura de la lista de activos
                Toast.makeText(this@ListaActivosAdapterActivity, "Error al obtener la lista de activos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}