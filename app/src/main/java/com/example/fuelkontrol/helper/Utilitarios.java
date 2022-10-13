package com.example.fuelkontrol.helper;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.Menu;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utilitarios {

    public static void setDefaultsPreference(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaultsPreference(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
    public static void tintMenuItemIcon(Context context, Menu menu, int idItem, int color) {
        Drawable drawable = menu.findItem(idItem).getIcon();

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable,color );
        menu.findItem(idItem).setIcon(drawable);
    }
    public static boolean checkPermission(Context context) {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 ==
                PackageManager.PERMISSION_GRANTED;
    }
    public static void requestPermission(int permmision, Activity act) {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(act, new String[]
                {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, permmision);
    }
    public static String obtenerFechaDespacho(Activity activity) {
        //2021-11-24 00:00:00.000
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        String formato = Utilitarios.getDefaultsPreference("formatodespacho", activity);
        SimpleDateFormat sdfDate = new SimpleDateFormat(formato);
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}