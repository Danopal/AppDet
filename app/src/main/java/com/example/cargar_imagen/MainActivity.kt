package com.example.cargar_imagen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.YuvImage
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val IMAGE_REQUEST_CODE = 1001
    private lateinit var selectImageButton: Button
    private lateinit var selectedImage: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        selectImageButton = findViewById(R.id.selectImageButton)
        selectedImage = findViewById(R.id.selectedImage)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(intent,IMAGE_REQUEST_CODE)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE){
            val selectedImageUri = data?.data
            try {
                val imageStream = contentResolver.openInputStream(selectedImageUri!!)
                val selectedBitmap = BitmapFactory.decodeStream(imageStream)
                selectedImage.setImageBitmap(selectedBitmap)
            }catch (e:Exception){
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}