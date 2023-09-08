package com.chemecador.secretaria.utils;

import android.app.Activity;
import android.content.Context;

import com.chemecador.secretaria.gui.CustomToast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static final int SUCCESS = 1;
    public static final int INFO = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;

    public static void showToast(Context context, int type, String message) {
        ((Activity) context).runOnUiThread(() -> new CustomToast(context, type, CustomToast.LENGTH_LONG).show(message));
    }
    public static void showToast(Context context, int type, int resource) {
        ((Activity) context).runOnUiThread(() -> new CustomToast(context, type, CustomToast.LENGTH_LONG).show(context.getString(resource)));
    }

    public static DateTimeFormatter getDayFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public static DateTimeFormatter getFullFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    public static DateTimeFormatter getFullBeautyFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    }
    public static DateTimeFormatter getDayBeautyFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    public static String beautifyDate(String date) {
        DateTimeFormatter fullFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(date, fullFormat);
        DateTimeFormatter newFormat;
        if (dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0) {
            newFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        } else {
            newFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        }
        LocalDateTime newDate = LocalDateTime.parse(date, fullFormat);
        return newDate.format(newFormat);


    }
}
