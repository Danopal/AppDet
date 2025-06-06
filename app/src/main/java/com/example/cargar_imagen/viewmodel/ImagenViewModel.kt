package com.example.cargar_imagen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImagenViewModel : ViewModel() {

    private val _imagenUri = MutableLiveData<String>()
    val imagenUri: LiveData<String> get() = _imagenUri

    fun setImagenUri(uri: String) {
        _imagenUri.value = uri
    }
}
