package dev.jahir.frames.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import dev.jahir.frames.R

open class NumberFragment() : Fragment() {

    var number = "Hola"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text: TextView? = view.findViewById(R.id.fragment_title)
        text?.text = number
    }

    companion object {
        @JvmStatic
        fun create(number: String = "Hola") = NumberFragment().apply {
            this.number = number
        }
    }
}