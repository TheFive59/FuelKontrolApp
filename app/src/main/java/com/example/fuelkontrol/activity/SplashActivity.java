package com.example.fuelkontrol.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.fuelkontrol.BuildConfig;
import com.example.fuelkontrol.LoginActivity;
import com.example.fuelkontrol.R;
import com.example.fuelkontrol.helper.DBHelper;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.util.Utilidades;

public class SplashActivity extends AppCompatActivity {
    DBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        dbHelper = new DBHelper(this, "FUELKONTROLADMIN", null, 1);

        switch(getFirstTimeRun(this)) {
            case 0:
                Intent intento= new Intent(SplashActivity.this, LoginAjustes.class);
                startActivity(intento);
                this.finish();
                break;
            case 1:
                Intent login= new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(login);
                this.finish();
                break;
            case 2:

                break;
        }
    }

    public static int getFirstTimeRun(Context contexto) {
        SharedPreferences sp = contexto.getSharedPreferences("MYAPP", 0);
        int result, currentVersionCode = BuildConfig.VERSION_CODE;
        int lastVersionCode = sp.getInt("FIRSTTIMERUN", -1);
        if (lastVersionCode == -1) result = 0; else
            result = (lastVersionCode == currentVersionCode) ? 1 : 2;
        sp.edit().putInt("FIRSTTIMERUN", currentVersionCode).apply();
        return result;
    }


}