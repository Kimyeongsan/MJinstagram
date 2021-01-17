package com.example.mjinstagram.navigation.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mjinstagram.R

class PhotoFragment : Fragment() {

    private lateinit var photoViewModel: PhotoViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        photoViewModel =
                ViewModelProvider(this).get(PhotoViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_photo, container, false)
        val textView: TextView = root.findViewById(R.id.text_photo)
        photoViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}