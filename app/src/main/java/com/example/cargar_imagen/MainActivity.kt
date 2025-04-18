package com.example.cargar_imagen
import android.util.Log
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
import com.yalantis.ucrop.UCrop


class MainActivity : AppCompatActivity() {

    private val IMAGE_REQUEST_CODE = 1001
    private lateinit var selectImageButton: Button
    private lateinit var selectedImage: ImageView
    private lateinit var textoPrediccion: TextView
    private val cliente = OkHttpClient()
    private var imagenUri: Uri? = null
    private val servidor = "http://3.16.218.220:8000" // Reemplaza con tu IP real del servidor

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

        // Comprobamos si los datos existen antes de procesarlos
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            // Asegurarse de que `data` no es nulo
            data?.data?.let { uri ->
                selectedImage.setImageURI(uri)

                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("imagen", ".jpg", cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                // Ahora envía la imagen al servidor en un hilo en segundo plano
                Thread {
                    enviarImagenAlServidor(tempFile)
                }.start()
            } ?: run {
                Toast.makeText(this, "No se seleccionó una imagen", Toast.LENGTH_SHORT).show()
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
                    e.printStackTrace()  // Imprime el error completo en Logcat
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("API Response", "Respuesta completa: $responseBody")  // Verifica el cuerpo completo

                val json = JSONObject(responseBody ?: "")
                val texto = json.optString("texto_extraido", "")

                if (texto.isNotEmpty()) {
                    predecirTexto(texto)  // Llama a la función de predicción con el texto extraído
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

        // Crear la petición GET con el texto en la URL
        val request = Request.Builder()
            .url(url)  // Ahora estamos pasando el parámetro en la URL
            .get()     // Usamos GET porque estamos pasando el texto en la URL
            .build()

        // Hacer la petición
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
