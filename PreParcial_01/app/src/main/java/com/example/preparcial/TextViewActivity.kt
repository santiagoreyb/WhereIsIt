package com.example.preparcial

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.ListView
import android.widget.Toast
import com.example.whereisit.ContactsAdapter

class TextViewActivity : AppCompatActivity() {
    private var mProjection: Array<String>? = null
    private var mCursor: Cursor? = null
    private var mContactsAdapter: ContactsAdapter? = null
    private var mLista: ListView? = null
    private val REQUEST_CODE_CONTACTS = 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        mLista = findViewById(R.id.listContacts)
        mProjection = arrayOf(ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY)
        mContactsAdapter = ContactsAdapter(this, null, 0)
        mLista?.adapter = mContactsAdapter
        initView()
    }
    private fun initView() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, mProjection, null, null, null)
            mContactsAdapter?.changeCursor(mCursor)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initView()
            } else {
                Toast.makeText(this, "Permiso de CONTACTOS denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
