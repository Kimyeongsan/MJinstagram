package com.example.mjinstagram.navigation.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhotoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is photo Fragment"
    }
    val text: LiveData<String> = _text
}