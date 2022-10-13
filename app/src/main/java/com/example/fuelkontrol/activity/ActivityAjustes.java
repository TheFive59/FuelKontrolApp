package com.example.fuelkontrol.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fuelkontrol.LoginActivity;
import com.example.fuelkontrol.R;
import com.example.fuelkontrol.helper.DBHelper;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.helper.ValidarConectividad;
import com.example.fuelkontrol.util.Config;
import com.example.fuelkontrol.util.Utilidades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ActivityAjustes extends AppCompatActivity {
    private EditText edtServidor, edtBaseDatos, edtUsuario, edtContraBase, edtEmpresa,
            edtFormatoDespacho, edtFormatoConsultas;
    private Button btnGuardar;
    private ImageView imgTest;
    ManejadorDB testDb;
    ValidarConectividad valCon;
    private boolean estadoConexion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        Utilitarios.setDefaultsPreference("usuario", "SinValor", this);
        Utilitarios.setDefaultsPreference("huella", "NOHUELLA", this);
        Utilitarios.setDefaultsPreference("biometricovalido", "DATOSNOVALIDOS", this);
        Utilitarios.setDefaultsPreference("verificador", "0", this);
        Utilitarios.setDefaultsPreference("verificador2", "0", this);

        edtServidor = findViewById(R.id.edtServidor);
        edtBaseDatos = findViewById(R.id.edtBaseDatos);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtContraBase = findViewById(R.id.edtContraBase);
        edtEmpresa = findViewById(R.id.edtEmpresa);
        edtFormatoDespacho = findViewById(R.id.edtFormatoDespacho);
        edtFormatoConsultas = findViewById(R.id.edtFormatoDatos);
        btnGuardar = findViewById(R.id.btnGuardar);
        imgTest = findViewById(R.id.btnTest);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            testDb = new ManejadorDB();
            valCon = new ValidarConectividad();
        } catch (Exception exception) {
            Toast.makeText(this, "Error db" + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        try {
            edtServidor.setText((Utilitarios.getDefaultsPreference("servidor", this)));
            edtBaseDatos.setText((Utilitarios.getDefaultsPreference("basededatos", this)));
            edtUsuario.setText((Utilitarios.getDefaultsPreference("usuariobase", this)));
            edtContraBase.setText((Utilitarios.getDefaultsPreference("contraseñabase", this)));
            edtEmpresa.setText((Utilitarios.getDefaultsPreference("empresaajustes", this)));
            if (edtFormatoConsultas.getText().toString().equals("") || edtFormatoDespacho.getText().toString().equals("")) {
                edtFormatoDespacho.setText((Utilitarios.getDefaultsPreference("formatodespacho", this)));
                edtFormatoConsultas.setText((Utilitarios.getDefaultsPreference("formatoconsultas", this)));
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Error 2" + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        btnGuardar.setEnabled(false);
        //
        btnGuardar.setOnClickListener(v -> guardarPreferencias());
        imgTest.setOnClickListener(v -> {
            if (testConnection() == true) {
                edtServidor.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                edtBaseDatos.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                edtUsuario.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                edtContraBase.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                edtEmpresa.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                edtFormatoDespacho.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                edtFormatoConsultas.setBackgroundResource(R.drawable.stylo_borde_listview_activo);
                btnGuardar.setEnabled(true);
                Toast.makeText(this, "Conexión exitosa es seguro guardar la información.",
                        Toast.LENGTH_LONG).show();
            } else {
                edtServidor.setBackgroundResource(R.drawable.stylo_borde_error);
                edtBaseDatos.setBackgroundResource(R.drawable.stylo_borde_error);
                edtUsuario.setBackgroundResource(R.drawable.stylo_borde_error);
                edtContraBase.setBackgroundResource(R.drawable.stylo_borde_error);
                edtEmpresa.setBackgroundResource(R.drawable.stylo_borde_error);
                edtFormatoDespacho.setBackgroundResource(R.drawable.stylo_borde_error);
                edtFormatoConsultas.setBackgroundResource(R.drawable.stylo_borde_error);
                mostrarErrorServer();
                Toast.makeText(this, "Error en la conexión.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void guardarPreferencias() {

        Utilitarios.setDefaultsPreference("servidor", edtServidor.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("basededatos", edtBaseDatos.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("usuariobase", edtUsuario.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("contraseñabase", edtContraBase.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("empresaajustes", edtEmpresa.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("formatodespacho", edtFormatoDespacho.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("formatoconsultas", edtFormatoConsultas.getText().
                toString(), this);
        Utilitarios.setDefaultsPreference("validarUsuario", "1", this);
        Utilitarios.setDefaultsPreference("usuario", "SinValor", this);
        Utilitarios.setDefaultsPreference("huella", "NOHUELLA", this);
        Utilitarios.setDefaultsPreference("tipousuarioapp", "0", this);
        Config.Mensaje(this, "     Datos actualizados es seguro utilizar Fuel Kontrol✔.Bienvenido a la app.", true, false);
        Intent abrirConfiguracion = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(abrirConfiguracion);
        this.finish();
    }

    boolean testConnection() {
        ResultSet set = null;
        if (valCon.validarConectividad(getApplicationContext()) == false) {
            mostrarToast();
        } else if (valCon.validarConectividad(getApplicationContext()) == true) {
            try {
                if (testDb != null) {
                    try {
                        testDb.test(edtServidor.getText().
                                toString(), edtBaseDatos.getText().
                                toString(), edtUsuario.getText().
                                toString(), edtContraBase.getText().
                                toString());
                        String sql = "SELECT TOP 5 * FROM USUARIOS";
                        Statement smt = testDb.test(edtServidor.getText().
                                toString(), edtBaseDatos.getText().
                                toString(), edtUsuario.getText().
                                toString(), edtContraBase.getText().
                                toString()).createStatement();
                        set = smt.executeQuery(sql);
                    } catch (Exception ex) {
                        estadoConexion = false;
                    }
                    if (set.next()) {
                        estadoConexion = true;

                    } else {
                        estadoConexion = true;
                    }
                    // testDb.CONN(this).close();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Error : " + ex.getMessage(), Toast.LENGTH_LONG).show();
                estadoConexion = false;
            }
        }
        return estadoConexion;
    }

    public void mostrarErrorServer() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.error_de_acceso, findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public void mostrarToast() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_toast_layout, findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}