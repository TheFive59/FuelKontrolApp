package com.example.fuelkontrol.prueba;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.helper.Utilitarios;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(),
                    "devices").commit();
        else
            onBackStackChanged();
    }
    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        //getFragmentManager().beginTransaction().replace(R.id.fragment).addToBackStack("tag").commit();
        return true;
    }
   private static long presionado;
    @Override
    public void onBackPressed(){
        //Si tiene fragments simplemente ejecuta la pulsación Back normal
        if (getSupportFragmentManager().getBackStackEntryCount()>0)
            super.onBackPressed();
        else {
            if (presionado + 2500 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(this, R.string.double_back_pressed, Toast.LENGTH_SHORT).show();
            presionado = System.currentTimeMillis();
        }
    }
}
