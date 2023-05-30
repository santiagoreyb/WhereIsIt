
package com.example.whereisit


import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.whereisit.databinding.AniadirActivoActivityBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage


class AniadirActivoActivity:AppCompatActivity() {


    private lateinit var binding: AniadirActivoActivityBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var myRef: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    private val storage = Firebase.storage
    private lateinit var imagen: ImageView

    private val REQUEST_CODE_CAMERA_PERMISSION = 2
    private val REQUEST_CODE_IMAGE_CAPTURE = 3
    private val REQUEST_CODE_IMAGE_PICKER = 4
    private val REQUEST_CODE_WRITE_EXTERNAL_PERMISSION = 5

    val PATH_USERS="users/"
    val PATH_ACTIVOS="activos/"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.aniadir_activo_activity)

        binding = AniadirActivoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        myRef = FirebaseDatabase.getInstance().reference

        imagen = findViewById(R.id.ActivoImageViewID)
        binding.ActivoImageViewID.setOnClickListener {
            val options = arrayOf<CharSequence>("Tomar foto", "Seleccionar de la galería", "Cancelar")
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Elige una opción")
            builder.setItems(options) { dialog, item ->
                when {
                    options[item] == "Tomar foto" -> {
                        // Verificar permisos de cámara y almacenamiento
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
                        } else {
                            openCamera()
                        }
                    }
                    options[item] == "Seleccionar de la galería" -> {
                        // Verificar permiso de almacenamiento
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_PERMISSION)
                        } else {
                            openGallery()
                        }
                    }
                    options[item] == "Cancelar" -> {
                        dialog.dismiss()
                    }
                }
            }
            builder.show()
        }

        val buttonListener:Button = findViewById(R.id.AniadirActivoButtonID)
        buttonListener.setOnClickListener {

            if ( validateForm() ) {
                buscarCorreo()
                saveImage()
            }

        }

    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imagen.setImageBitmap(imageBitmap)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_PERMISSION)
            }
        } else if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            imagen.setImageURI(imageUri)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_PERMISSION)
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_PERMISSION) {
            if (grantResults.all { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "El permiso es necesario para guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {

        var valid = true

        val nombreReferencia = binding.NombreReferenciaEditTextID.text.toString()
        if (TextUtils.isEmpty(nombreReferencia)) {
            binding.NombreReferenciaEditTextID.error = "Required."
            valid = false
        } else {
            binding.NombreReferenciaEditTextID.error = null
        }

        val activo = binding.IDActivoEditTextID.text.toString()
        if (TextUtils.isEmpty(activo)) {
            binding.IDActivoEditTextID.error = "Required."
            valid = false
        } else {
            binding.IDActivoEditTextID.error = null
        }

        val correo = binding.CorreoResponsableEditTextID.text.toString()
        if (TextUtils.isEmpty(correo)) {
            binding.CorreoResponsableEditTextID.error = "Required."
            valid = false
        } else {
            binding.CorreoResponsableEditTextID.error = null
        }

        return valid
    }

    private fun buscarCorreo() {

        val valorCorreo: EditText = findViewById(R.id.CorreoResponsableEditTextID)
        val correo = valorCorreo.text.toString()

        val userQuery = database.getReference("users").orderByChild("email").equalTo(correo)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val usuario = userSnapshot.getValue(PersonaClass::class.java)
                    val uid = userSnapshot.key

                    if (usuario != null && uid != null) {
                        // Obtener el valor del edittextuidactivo
                        val valorUidActivo: EditText = findViewById(R.id.ID_ActivoEditTextID)
                        val uidActivo = valorUidActivo.text.toString()

                        // Agregar el uidActivo a la lista de activos del usuario
                        usuario.activos.add(uidActivo)

                        // Guardar los cambios en la base de datos
                        val userRef = database.getReference("users/$uid")
                        userRef.setValue(usuario)

                        Log.d("Actualización", "Se agregó el uidActivo: $uidActivo a la lista de activos del usuario con uid: $uid")
                        guardarNuevoActivo()
                        return
                    }
                }
                Log.d("Búsqueda", "No se encontró un usuario con el correo: $correo en la base de datos")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Búsqueda", "Error al buscar el usuario en la base de datos: ${databaseError.message}")
            }

        })

    }

    private fun guardarNuevoActivo() {

        val valorUidActivo: EditText = findViewById(R.id.ID_ActivoEditTextID)
        val uidActivo = valorUidActivo.text.toString()

        val nuevoActivo = ActivoClass()
        nuevoActivo.uid = uidActivo
        nuevoActivo.nombre = findViewById<EditText>(R.id.NombreReferenciaEditTextID).text.toString()
        nuevoActivo.isDisponible = true
        nuevoActivo.latitud = "1.0"
        nuevoActivo.longitud = "1.0"

        val activosRef = database.getReference("activos")
        val nuevoActivoRef = activosRef.child(uidActivo)
        nuevoActivoRef.setValue(nuevoActivo)
            .addOnSuccessListener {
                Log.d("Guardado", "Nuevo activo guardado exitosamente en la ubicación activos/$uidActivo")
            }
            .addOnFailureListener { exception ->
                Log.e("Guardado", "Error al guardar el nuevo activo en la ubicación activos/$uidActivo: ${exception.message}")
            }

    }

    private fun saveImage() {
        val imageBitmap = (imagen.drawable as BitmapDrawable).bitmap
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        imageUri?.let {
            try {
                val outputStream = contentResolver.openOutputStream(imageUri)
                outputStream?.use {
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK, Intent().apply { putExtra("imageUri", imageUri) })
                    uploadImage(imageUri)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage(imageUri: Uri?) {
        val file = imageUri ?: Uri.parse("android.resource://com.example.where-is-it/" + R.drawable.cel)
        val uidText:String = findViewById<EditText?>(R.id.ID_ActivoEditTextID).text.toString()
        val path = "images/activos/$uidText/image.jpg"
        val imageRef = storage.reference.child(path)
        imageRef.putFile(file)
            .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                    // Get a URL to the uploaded content
                    Log.i("FBApp", "Successfully uploaded image")
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(exception: Exception) {
                    // Handle unsuccessful uploads
                    // ...
                }
            })
    }


}
