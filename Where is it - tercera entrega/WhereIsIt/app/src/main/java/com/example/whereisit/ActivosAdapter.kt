package com.example.whereisit

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage

class ActivosAdapter(
    private val listaActivos: List<ActivoClass>,
    private val onActivarPosicionClickListener: OnActivarPosicionClickListener
) : RecyclerView.Adapter<ActivosAdapter.ActivoViewHolder>() {


    inner class ActivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewActivo: ImageView = itemView.findViewById(R.id.ActivoImageViewID)
        val textViewActivo: TextView = itemView.findViewById(R.id.NombreActivoTextViewID)
        val buttonVerDisponibilidad: Button = itemView.findViewById(R.id.VerDisponibilidadButtonID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.adapter_activos_activity,
            parent,
            false
        )
        return ActivoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActivoViewHolder, position: Int) {
        val activo = listaActivos[position]

        holder.textViewActivo.text = activo.nombre

        // Descargar imagen de Firebase Storage y mostrarla en el ImageView
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/activos/${activo.uid}/image.jpg")

        imageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.imageViewActivo.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                Log.d("ActivosAdapter", "Error al descargar la imagen del activo ${activo.nombre}: $exception")
            }

        holder.buttonVerDisponibilidad.setOnClickListener {
            val disponibilidad = !activo.isDisponible
            onActivarPosicionClickListener.onActivarPosicionClick(activo, disponibilidad)
        }
    }

    override fun getItemCount(): Int = listaActivos.size

    interface OnActivarPosicionClickListener {
        fun onActivarPosicionClick(activo: ActivoClass, disponibilidad: Boolean)
    }


}