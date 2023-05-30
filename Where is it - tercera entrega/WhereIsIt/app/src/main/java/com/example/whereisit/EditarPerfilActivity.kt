package com.example.whereisit

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.whereisit.databinding.EditarPerfilActivityBinding
import com.example.whereisit.databinding.RegistroUsuariosBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import dalvik.system.InMemoryDexClassLoader

class EditarPerfilActivity : AppCompatActivity() {


    private val REQUEST_CODE_IMAGE_CAPTURE = 2
    private lateinit var binding: EditarPerfilActivityBinding
    private lateinit var imageView: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var auth2: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    val PATH_USERS="users/"

    override fun onResume() {
        super.onResume()
        cargarTodo() // Carga los datos actualizados cada vez que la actividad se muestra en primer plano
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_perfil_activity)

        binding = EditarPerfilActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        auth2 = FirebaseDatabase.getInstance().reference

        cargarTodo()

        imageView = findViewById(R.id.UsuarioImageViewID)
        imageView.setOnClickListener {
            val imageButtonIntent = Intent(this, ImageAdap::class.java)
            startActivityForResult(imageButtonIntent, REQUEST_CODE_IMAGE_CAPTURE)
        }

        // Carga la imagen en el ImageView si existe
        val imageUriString = getSharedPreferences("my_prefs", Context.MODE_PRIVATE).getString("image_uri", null)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        }

        val enviarButtonListener:Button = findViewById(R.id.FinalizarButtonID)
        enviarButtonListener.setOnClickListener {
            if ( validateForm() ) {
                saveImage()
                actualizarDatos()
                Toast.makeText(this,"Tus datos de perfil se han actualizado.",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,"¡ERROR AL ACTUALIZAR!",Toast.LENGTH_LONG).show()
            }

        }

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

    private fun cargarTodo () {
        // Descargar imagen de Firebase Storage y mostrarla en el ImageView
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/profile/${auth.currentUser?.uid}/image.jpg")

        auth2 = database.getReference(PATH_USERS+auth.currentUser!!.uid)
        var nombre:String? = ""

        auth2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Obtener el valor del campo deseado
                nombre = dataSnapshot.child("nombre").getValue(String::class.java)
                var textViewListener: TextView = findViewById(R.id.NombreUsuarioEditTextID)
                textViewListener.text = nombre
                textViewListener = findViewById(R.id.CorreoOrganizacionalEditTextID)
                textViewListener.text = auth.currentUser!!.email.toString()
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
                Log.d("UsuariosAdapter", "Error al descargar la imagen del usuario ${nombre}: $exception")
            }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = binding.CorreoOrganizacionalEditTextID.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.CorreoOrganizacionalEditTextID.error = "Required."
            valid = false
        } else {
            binding.CorreoOrganizacionalEditTextID.error = null
        }
        val password = binding.ContraseniaEditTextID.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.ContraseniaEditTextID.error = "Required."
            valid = false
        } else {
            binding.ContraseniaEditTextID.error = null
        }
        val nombre = binding.NombreUsuarioEditTextID.text.toString()
        if (TextUtils.isEmpty(nombre)) {
            binding.NombreUsuarioEditTextID.error = "Required."
            valid = false
        } else {
            binding.NombreUsuarioEditTextID.error = null
        }
        return valid
    }

    private fun actualizarDatos() {
        val user = auth.currentUser
        if (user != null) {
            // Update user info
            val myUser = PersonaClass()
            myUser.uid = auth.currentUser!!.uid
            myUser.nombre = binding.NombreUsuarioEditTextID.text.toString()

            val newEmail = binding.CorreoOrganizacionalEditTextID.text.toString()
            val newPassword = binding.ContraseniaEditTextID.text.toString()

            user.updateEmail(newEmail)
                .addOnCompleteListener { emailUpdateTask ->
                    if (emailUpdateTask.isSuccessful) {
                        // Email updated successfully
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { passwordUpdateTask ->
                                if (passwordUpdateTask.isSuccessful) {
                                    // Password updated successfully
                                    // Update user data in the database
                                    auth2 = database.getReference(PATH_USERS + auth.currentUser!!.uid)
                                    auth2.setValue(myUser)
                                } else {
                                    // Error updating password
                                    // Handle error
                                }
                            }
                    } else {
                        // Error updating email
                        // Handle error
                    }
                }
        }
    }

    private fun saveImage() {
        val imageBitmap = (imageView.drawable as BitmapDrawable).bitmap
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        imageUri?.let {
            try {
                val outputStream = contentResolver.openOutputStream(imageUri)
                outputStream?.use {
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    findViewById<TextView>(R.id.FotoDePerfilTextViewID).text = "NUEVA IMAGEN CARGADA"
                    setResult(Activity.RESULT_OK, Intent().apply { putExtra("imageUri", imageUri) })
                    uploadImage(imageUri)
                }
            } catch (e: Exception) {
                findViewById<TextView>(R.id.FotoDePerfilTextViewID).text = "ERROR EN CARGAR NUEVA IMAGEN"
            }
        }
    }

    private fun uploadImage(imageUri: Uri?) {
        val file = imageUri ?: Uri.parse("android.resource://com.example.where-is-it/" + R.drawable.perfil)
        val path = "images/profile/" + auth.currentUser!!.uid + "/image.jpg"
        val imageRef = Firebase.storage.reference.child(path)
        imageRef.putFile(file)
            .addOnSuccessListener { // Get a URL to the uploaded content
                Log.i("FBApp", "Successfully uploaded image")
            }
            .addOnFailureListener {
                // Handle unsuccessful uploads
                // ...
            }
    }


}