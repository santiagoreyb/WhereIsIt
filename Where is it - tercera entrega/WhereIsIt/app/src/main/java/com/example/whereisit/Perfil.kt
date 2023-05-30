package com.example.whereisit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class Perfil : AppCompatActivity() {


    private val REQUEST_CODE_IMAGE_CAPTURE = 2
    private lateinit var imageView: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var auth2: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    val PATH_USERS="users/"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        auth = Firebase.auth
        auth2 = FirebaseDatabase.getInstance().reference

        imageView = findViewById(R.id.imageView3)
        imageView.setOnClickListener {
            val imageButtonIntent = Intent(this, ImageAdap::class.java)
            startActivityForResult(imageButtonIntent, REQUEST_CODE_IMAGE_CAPTURE)
        }

        val menu = findViewById<ImageView>(R.id.imageView14)
        menu.setOnClickListener(){
            val intent = Intent(this, MapsActivityAdmin::class.java)
            startActivity(intent)
        }

        cargarImagen()

        // Carga la imagen en el ImageView si existe
        val imageUriString = getSharedPreferences("my_prefs", Context.MODE_PRIVATE).getString("image_uri", null)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        }

        val contactos_textViewListener:TextView = findViewById(R.id.textView15)
        contactos_textViewListener.setOnClickListener {
            val contactos_textViewIntent = Intent(this, VistaContactos::class.java)
            startActivity(contactos_textViewIntent)
        }

        val editarPerfilTextView:TextView = findViewById(R.id.textView11)
        editarPerfilTextView.setOnClickListener {
            val editarPerfilIntent = Intent(this,EditarPerfilActivity::class.java)
            startActivity(editarPerfilIntent)
        }

        val soporteButtonTextView:TextView = findViewById(R.id.textView13)
        soporteButtonTextView.setOnClickListener {
            val soporteIntent = Intent(this, SoporteActivity::class.java)
            startActivity(soporteIntent)
        }

        val cerrarSesioonListener_ImageView:ImageView = findViewById(R.id.imageView4)
        cerrarSesioonListener_ImageView.setOnClickListener {
            logout()
        }

        val cerrarSesioonListener_TextView:TextView = findViewById(R.id.textView14)
        cerrarSesioonListener_TextView.setOnClickListener {
            logout()
        }

    }

    private fun logout () {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageUri = data?.getParcelableExtra<Uri>("imageUri")
            imageView.setImageURI(imageUri)
            // Guarda el URI de la imagen en SharedPreferences
            val editor = getSharedPreferences("my_prefs", Context.MODE_PRIVATE).edit()
            editor.putString("image_uri", imageUri.toString())
            editor.apply()
        }
    }

    private fun cargarImagen () {
        // Descargar imagen de Firebase Storage y mostrarla en el ImageView
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/profile/${auth.currentUser?.uid}/image.jpg")

        auth2 = database.getReference(PATH_USERS+auth.currentUser!!.uid)
        var campoValor:String? = ""

        auth2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Obtener el valor del campo deseado
                campoValor = dataSnapshot.child("nombre").getValue(String::class.java)
                val textViewListener:TextView = findViewById(R.id.textView9)
                textViewListener.text = campoValor
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error de lectura de datos
                Log.e("TAG", "Error al leer datos: ${databaseError.message}")
            }
        })

        imageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                Log.d("UsuariosAdapter", "Error al descargar la imagen del usuario ${campoValor}: $exception")
            }
    }


}
