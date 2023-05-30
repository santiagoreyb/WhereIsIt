
package com.example.whereisit


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.whereisit.databinding.IniciarSesionActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase



class IniciarSesion : AppCompatActivity() {


    private lateinit var binding: IniciarSesionActivityBinding
    private val REQUEST_CODE_PERMISSIONS = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var auth2: DatabaseReference
    private val database = FirebaseDatabase.getInstance()
    val PATH_USERS="users/"

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.iniciar_sesion_activity)

        binding = IniciarSesionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        auth2 = FirebaseDatabase.getInstance().reference

        val iniciar = findViewById<Button>(R.id.button)
        iniciar.setOnClickListener() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                val email = binding.editTextTextEmailAddress2.text.toString()
                val password = binding.editTextTextPassword.text.toString()
                signInUser(email, password)
            }
        }
    }

    private fun startMapsActivity(currentUser: FirebaseUser?) {

        auth2 = database.getReference(PATH_USERS+auth.currentUser!!.uid)
        var tipoUsuario: String? = ""

        auth2.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Obtener el valor del campo deseado
                tipoUsuario = dataSnapshot.child("tipo").getValue(String::class.java)
                if (tipoUsuario == "Admin") {
                    val intent = Intent(this@IniciarSesion, MapsActivityAdmin::class.java)
                    if (currentUser != null) {
                        intent.putExtra("user", currentUser.email)
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(this@IniciarSesion, MapsActivityActivos::class.java)
                    if (currentUser != null) {
                        intent.putExtra("user", currentUser.email)
                    }
                    startActivity(intent)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error de lectura de datos
                Log.e("TAG", "Error al leer datos: ${databaseError.message}")
            }

        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val email = binding.editTextTextEmailAddress2.text.toString()
                val password = binding.editTextTextPassword.text.toString()
                signInUser(email, password)
            } else {
                Toast.makeText(
                    this,
                    "El permiso es necesario para acceder a la siguiente actividad",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startMapsActivity(currentUser)
        } else {
            binding.editTextTextEmailAddress2.setText("")
            binding.editTextTextPassword.setText("")
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = binding.editTextTextEmailAddress2.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.editTextTextEmailAddress2.error = "Required."
            valid = false
        } else {
            binding.editTextTextEmailAddress2.error = null
        }
        val password = binding.editTextTextPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.editTextTextPassword.error = "Required."
            valid = false
        } else {
            binding.editTextTextPassword.error = null
        }
        return valid
    }

    private fun signInUser(email: String, password: String) {
        if (validateForm()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
// Sign in success, update UI
                        Log.d(ContentValues.TAG, "signInWithEmail:success:")
                        val user = auth.currentUser
                        updateUI(auth.currentUser)
                    } else {
                        Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
        }
    }


}
