package com.example.cargar_imagen.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cargar_imagen.databinding.FragmentDecisionBinding
import androidx.fragment.app.activityViewModels
import com.example.cargar_imagen.viewmodel.ImagenViewModel




class DecisionFragment : Fragment() {

    private val imagenViewModel: ImagenViewModel by activityViewModels()
    private var _binding: FragmentDecisionBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecisionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.botonSi.setOnClickListener {
            findNavController().navigate(
                DecisionFragmentDirections.actionDecisionFragmentToSeleccionImagenFragment()
            )
        }


        binding.botonNo.setOnClickListener {
            findNavController().navigate(
                DecisionFragmentDirections.actionDecisionFragmentToSalirFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
