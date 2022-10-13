package com.example.fuelkontrol.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.helper.DBHelper;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.util.Config;
import com.example.fuelkontrol.util.Utilidades;

public class LoginAjustes extends AppCompatActivity {
    Button btnAjustes;
    EditText edtContra, edtUsuario;
    TextView tvMostrarContra;
    DBHelper dbHelper;
    private boolean esVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ajustes);
        //Enlace de componentes
        btnAjustes = (Button) findViewById(R.id.btnLogin);
        edtContra = (EditText) findViewById(R.id.edtContra);
        edtUsuario = (EditText) findViewById(R.id.edtUsuario);
        tvMostrarContra = (TextView) findViewById(R.id.mostrarContra);
        tvMostrarContra.setTextColor(Color.BLUE);

        dbHelper = new DBHelper(this, "FUELKONTROLADMIN", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db != null) {
            ContentValues v = new ContentValues();
            v.put(Utilidades.KEY_id, "2");
            v.put(Utilidades.KEY_ClaveUsuario, "root");
            v.put(Utilidades.KEY_ContraUsuario, "root");
            db.insert(Utilidades.TABLE, Utilidades.KEY_id, v);
        }else{

        }
                //Agregamos escuchadores a los objetos
        edtUsuario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnAjustes.setEnabled(true);
                } else {
                    btnAjustes.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtContra.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnAjustes.setEnabled(true);
                } else {
                    btnAjustes.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnAjustes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (edtUsuario.toString().trim().length() == 0 && edtContra.toString().trim().length() == 0) {
                    btnAjustes.setEnabled(true);
                }
            }
        });
        btnAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (db != null) {
                    validacionUsuario();
                }

            }
        });

        tvMostrarContra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!esVisible) {
                    edtContra.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    esVisible = true;
                    tvMostrarContra.setTextColor(Color.BLUE);
                    tvMostrarContra.setText("Mostrar contraseña");
                    ///aqui puedes cambiar el texto del boton, o textview, o cambiar la imagen de un imageView.
                } else {
                    edtContra.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    esVisible = false;
                    tvMostrarContra.setTextColor(Color.BLACK);
                    tvMostrarContra.setText("Ocultar contraseña");
                    ///aqui puedes cambiar el texto del boton, o textview, o cambiar la imagen de un imageView.
                }
            }
        });
    }

    public void validacionUsuario() {
try {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String sql = "select clave, contra from usuario where clave='" + edtUsuario.getText().
            toString() + "' and contra='" + edtContra.getText().toString() + "';";
    Cursor fila = db.rawQuery(sql, null);

    if (fila.moveToFirst()) {
        do {
        } while (fila.moveToNext());
        Utilitarios.setDefaultsPreference("servidor", "0", this);
        Utilitarios.setDefaultsPreference("basededatos", "DIESEL", this);
        Utilitarios.setDefaultsPreference("usuariobase", "0", this);
        Utilitarios.setDefaultsPreference("contraseñabase", "0", this);
        Utilitarios.setDefaultsPreference("empresaajustes", "0", this);
        Utilitarios.setDefaultsPreference("formatodespacho", "0", this);
        Utilitarios.setDefaultsPreference("formatoconsultas", "0", this);
        Toast.makeText(this, "Acceso correcto.", Toast.LENGTH_LONG).show();
        //Config.Mensaje(this,"Acceso correcto.", false, false);
        abrirAjustes();
    } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginAjustes.this);
        builder.setTitle("Error");
        builder.setMessage("El usuario no ha sido registrado\no no tiene los permisos necesarios.");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}catch (Exception exception){
    Toast.makeText(this, "Error "+exception.getMessage(), Toast.LENGTH_LONG).show();
}
    }

    public void abrirAjustes() {
        Intent abrirConfiguracion = new Intent(getApplicationContext(), ActivityAjustes.class);
        startActivity(abrirConfiguracion);
        finish();
    }

}