package com.example.cargar_imagen
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import android.util.Log



class MainActivity : AppCompatActivity() {

    private val IMAGE_REQUEST_CODE = 1001
    private lateinit var selectImageButton: Button
    private lateinit var selectedImage: ImageView
    private lateinit var textoPrediccion: TextView
    private val cliente = OkHttpClient()
    private var imagenUri: Uri? = null
    private val servidor = "http://3.16.218.220:8000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectImageButton = findViewById(R.id.selectImageButton)
        selectedImage = findViewById(R.id.selectedImage)
        textoPrediccion = findViewById(R.id.textoPrediccion)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            imagenUri = data?.data
            selectedImage.setImageURI(imagenUri)

            imagenUri?.let { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("imagen", ".jpg", cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                enviarImagenAlServidor(tempFile)
            }
        }
    }

    private fun enviarImagenAlServidor(archivo: File) {
        val cuerpoArchivo = archivo.asRequestBody("image/*".toMediaTypeOrNull())
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", archivo.name, cuerpoArchivo)
            .build()

        val request = Request.Builder()
            .url("$servidor/procesar-imagen/")
            .post(multipart)
            .build()

        cliente.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    textoPrediccion.text = "Error de conexión: ${e.message}"
                    e.printStackTrace()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val texto = json.optString("texto_extraido", "")

                if (texto.isNotEmpty()) {
                    predecirTexto(texto)
                } else {
                    runOnUiThread {
                        textoPrediccion.text = "No se pudo extraer texto de la imagen"
                    }
                }
            }
        })
    }

    private fun predecirTexto(texto: String) {
        // Codificar el texto para asegurarnos de que se pueda enviar en la URL
        val textoCodificado = URLEncoder.encode(texto, "UTF-8")

        // Crear la URL con el texto como parámetro de query
        val url = "$servidor/predecir/?texto=$textoCodificado"

        // Crear la solicitud GET con el texto como parámetro en la URL
        val request = Request.Builder()
            .url(url)  // Ahora estamos pasando el parámetro en la URL
            .get()     // Usamos GET porque estamos pasando el texto en la URL
            .build()

        // Hacer la solicitud
        cliente.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    textoPrediccion.text = "Error al predecir: ${e.message}"  // Mostrar el error
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("API Response", "Respuesta completa: $responseBody")  // Log para ver la respuesta completa

                val json = JSONObject(responseBody ?: "")
                val prediccion = json.optString("prediccion", "Sin respuesta")

                runOnUiThread {
                    textoPrediccion.text = "Predicción: $prediccion"
                }
            }
        })
    }

}
 