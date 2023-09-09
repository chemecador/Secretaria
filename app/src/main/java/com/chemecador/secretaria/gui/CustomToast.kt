package com.chemecador.secretaria.gui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.chemecador.secretaria.R


class CustomToast(private val context: Context, private val mType: Int, duration: Int) : Toast(context) {
    companion object {
        const val TOAST_SUCCESS = 1
        const val TOAST_INFO = 2
        const val TOAST_WARNING = 3
        const val TOAST_ERROR = 4
    }
    init {
        // Configurar la duración del toast
        setDuration(duration)
    }

    fun show(text: CharSequence?) {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val cView = li.inflate(R.layout.toast_layout, null)
        when (mType) {
            TOAST_SUCCESS -> cView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.item_success
                )
            )

            TOAST_INFO -> cView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.item_info
                )
            )

            TOAST_WARNING -> cView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.item_warning
                )
            )

            TOAST_ERROR -> cView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.item_error
                )
            )
        }
        val tv = cView.findViewById<TextView>(R.id.text_toast)
        tv.text = text
        this.view = cView
        super.show()
    }
}