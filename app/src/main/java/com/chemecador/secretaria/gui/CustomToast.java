package com.chemecador.secretaria.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.chemecador.secretaria.R;


public class CustomToast extends Toast {

    public static final int TOAST_SUCCESS = 1;
    public static final int TOAST_INFO = 2;
    public static final int TOAST_WARNING = 3;
    public static final int TOAST_ERROR = 4;

    private final Context context;

    private final int mType;

    public CustomToast(Context cont, int type, int duration) {
        super(cont);
        context = cont;
        this.mType = type;
        this.setDuration(duration);
    }

    public void show(CharSequence text) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View cView = li.inflate(R.layout.toast_layout, null);

        switch (mType) {
            case TOAST_SUCCESS:
                cView.setBackgroundColor(ContextCompat.getColor(context, R.color.item_success));
                break;

            case TOAST_INFO:
                cView.setBackgroundColor(ContextCompat.getColor(context, R.color.item_info));
                break;

            case TOAST_WARNING:
                cView.setBackgroundColor(ContextCompat.getColor(context, R.color.item_warning));
                break;

            case TOAST_ERROR:
                cView.setBackgroundColor(ContextCompat.getColor(context, R.color.item_error));
                break;
        }

        TextView tv = cView.findViewById(R.id.text_toast);
        tv.setText(text);

        this.setView(cView);

        super.show();
    }
}

