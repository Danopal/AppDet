package com.example.cargar_imagen.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cargar_imagen.databinding.FragmentSeleccionImagenBinding
import com.example.cargar_imagen.viewmodel.ImagenViewModel


class SeleccionImagenFragment : Fragment() {

    private val imagenViewModel: ImagenViewModel by activityViewModels()
    private var _binding: FragmentSeleccionImagenBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val REQUEST_CODE_GALLERY = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeleccionImagenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.botonCargarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            val uriImagen: Uri? = data?.data
            uriImagen?.let {
                imagenViewModel.setImagenUri(it.toString())  // Se guarda el URI
                findNavController().navigate(
                    SeleccionImagenFragmentDirections.actionSeleccionImagenFragmentToPrediccionFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
