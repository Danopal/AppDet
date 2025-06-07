package com.example.cargar_imagen.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cargar_imagen.databinding.FragmentPrediccionBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import com.example.cargar_imagen.viewmodel.ImagenViewModel

class PrediccionFragment : Fragment() {

    private var _binding: FragmentPrediccionBinding? = null
    private val binding get() = _binding!!
    private val imagenViewModel: ImagenViewModel by activityViewModels()
    private val cliente = OkHttpClient()
    private val servidor = "http://3.16.218.220:8000"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrediccionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.botonContinuar.visibility = View.GONE
        binding.botonReintentar.visibility = View.GONE

        binding.botonContinuar.setOnClickListener {
            findNavController().navigate(
                PrediccionFragmentDirections.actionPrediccionFragmentToDecisionFragment()
            )
        }

        binding.botonReintentar.setOnClickListener {
            findNavController().navigate(
                PrediccionFragmentDirections.actionPrediccionFragmentToDecisionFragment()
            )
        }

        imagenViewModel.imagenUri.observe(viewLifecycleOwner) { uriString ->
            if (!uriString.isNullOrEmpty()) {
                val uri = Uri.parse(uriString)
                mostrarImagen(uri)
                enviarImagenAlServidor(uri)
            } else {
                binding.textoPrediccion.text = "No se seleccionó una imagen."
            }
        }
    }


    private fun mostrarImagen(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        binding.selectedImage.setImageBitmap(bitmap)
    }

    private fun enviarImagenAlServidor(uri: Uri) {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val archivo = File.createTempFile("imagen", ".jpg", requireContext().cacheDir)
        inputStream?.use { input -> archivo.outputStream().use { output -> input.copyTo(output) } }

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
                requireActivity().runOnUiThread {
                    binding.textoPrediccion.text = "Error de conexión: ${e.message}"
                    binding.botonReintentar.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val textoExtraido = json.optString("texto_extraido", "")

                if (textoExtraido.isNotEmpty()) {
                    predecirTexto(textoExtraido)
                } else {
                    requireActivity().runOnUiThread {
                        binding.textoPrediccion.text = "No se pudo extraer texto de la imagen."
                        binding.botonReintentar.visibility = View.VISIBLE


                    }
                }
            }
        })
    }

    private fun predecirTexto(texto: String) {
        val textoCodificado = URLEncoder.encode(texto, "UTF-8")
        val url = "$servidor/predecir/?texto=$textoCodificado"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        cliente.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    binding.textoPrediccion.text = "Error al predecir: ${e.message}"
                    binding.botonReintentar.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val json = JSONObject(body ?: "")
                val prediccion = json.optString("prediccion", "Sin respuesta")

                requireActivity().runOnUiThread {
                    binding.textoPrediccion.text = "Predicción: $prediccion"

                    // Cambiar el color de fondo según el resultado
                    if (prediccion.equals("smishing", ignoreCase = true)) {
                        binding.textoPrediccion.setBackgroundColor(
                            resources.getColor(android.R.color.holo_red_dark, null)
                        )
                    } else if (prediccion.equals("no smishing", ignoreCase = true)) {
                        binding.textoPrediccion.setBackgroundColor(
                            resources.getColor(android.R.color.holo_green_dark, null)
                        )
                    } else {
                        binding.textoPrediccion.setBackgroundColor(
                            resources.getColor(android.R.color.darker_gray, null)
                        )
                    }

                    binding.botonContinuar.visibility = View.VISIBLE
                }

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
