package com.chemecador.secretaria.fragments.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R

class NoteDetailFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notes_detail, container, false)

        tvTitle = view.findViewById(R.id.tv_title)
        tvContent = view.findViewById(R.id.tv_content)
        tvStatus = view.findViewById(R.id.tv_status)

        // Recibir datos del Bundle
        val args = arguments
        if (args != null) {
            val title = args.getString("title", "")
            val content = args.getString("content", "")
            val status = args.getInt("status", -1)

            tvTitle.text = title
            tvContent.text = content

            tvStatus.text = when (status) {
                1 -> {
                    "Completado"
                }
                2 -> {
                    "Sin completar"
                }
                else -> {
                    ""
                }
            }
        }
        if (tvStatus.text.isEmpty()) {
            (view.findViewById(R.id.cb_status) as CheckBox).visibility = View.GONE
        }

        return view
    }
}
