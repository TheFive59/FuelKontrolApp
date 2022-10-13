package com.example.fuelkontrol.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.helper.ValidarConectividad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InsertarPapeletaActivity extends AppCompatActivity {
    public ValidarConectividad valCon;
    public ManejadorDB miDB;
    EditText edtEmpresa, edtAutobus, edtOperador, edtOperador2, edtLitros, edtOficina, edtPapeleta, edtKilometros, edtObresvaciones;
    Button btnInsertar;
    ResultSet rs;
    Statement comm;
    String empresa, autobus, operador, operador2, litros, oficina, papeleta, kilometros, fechaRegistro, observaciones = "RegistroManual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar_papeleta);
        edtEmpresa = (EditText) findViewById(R.id.edtEmpresa);
        edtEmpresa.setText(Utilitarios.getDefaultsPreference("empresaajustes", InsertarPapeletaActivity.this));
        edtAutobus = (EditText) findViewById(R.id.edtAutobus);
        edtOperador = (EditText) findViewById(R.id.edtOperador1);
        edtOperador2 = (EditText) findViewById(R.id.edtOperador2);
        edtLitros = (EditText) findViewById(R.id.edtLitros);
        edtOficina = (EditText) findViewById(R.id.edtOficina);
        edtPapeleta = (EditText) findViewById(R.id.edtPapeleta);
        edtKilometros = (EditText) findViewById(R.id.edtKilometros);
        edtObresvaciones = (EditText) findViewById(R.id.edtObservaciones);
        btnInsertar = (Button) findViewById(R.id.btnInsertarPapeleta);
        valCon = new ValidarConectividad();
        miDB = new ManejadorDB();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        edtEmpresa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtAutobus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtOperador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtOperador2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtLitros.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtOficina.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtPapeleta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnInsertar.setEnabled(count > 0 || !s.toString().equals(""));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtKilometros.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    btnInsertar.setEnabled(true);
                } else {
                    btnInsertar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnInsertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!valCon.validarConectividad(getApplicationContext())) {
                    errorInternet();
                } else if (valCon.validarConectividad(getApplicationContext())) {
                    insertarVale();
                }
            }
        });
    }
    private void insertarVale() {

        PreparedStatement prep;
        empresa = Utilitarios.getDefaultsPreference("empresaajustes", InsertarPapeletaActivity.this);
        autobus = edtAutobus.getText().toString();
        operador = edtOperador.getText().toString();
        operador2 = edtOperador2.getText().toString();
        litros = edtLitros.getText().toString();
        oficina = edtOficina.getText().toString();
        papeleta = edtPapeleta.getText().toString();
        kilometros = edtKilometros.getText().toString();
        observaciones = edtObresvaciones.getText().toString();
        if (observaciones.equals("")) {
            observaciones = "SO";
        }
        try {
            int insercion = 0;
            try{
            prep = miDB.CONN(this).prepareStatement(
                    "INSERT INTO DATOSDIESEL (EmpresaId,OficinaId,AutobusId,PapeletaId,OperadorId," +
                            "Operador2Id,PapFemision,PapEstatus,PapKms,PapLitros,PapObservaciones,Paso,Relleno," +
                            "Tipo,TC01,TC02,TC03,TC06,TC08,TC90,TC92,TC95,TC80,TC81,TC82,TC83,TC84,TC85,TC86," +
                            "TC87,TC88,TC89) VALUES (" +
                            "        ?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?);");
            
            prep.setString(1, empresa);
            prep.setString(2, oficina);
            prep.setString(3, autobus);
            prep.setString(4, papeleta);
            prep.setString(5, operador);
            prep.setString(6, operador2);
            prep.setString(7, obtenerFecha());
            prep.setString(8, "S");
            prep.setString(9, kilometros);
            prep.setString(10, litros);
            prep.setString(11, observaciones);
            prep.setString(12, "false");
            prep.setString(13, "false");
            prep.setString(14, "M");
            prep.setString(15, "");
            prep.setString(16, "");
            prep.setString(17, "");
            prep.setString(18, "");
            prep.setString(19, "");
            prep.setString(20, "");
            prep.setString(21, "");
            prep.setString(22, "");
            prep.setString(23, "");
            prep.setString(24, "");
            prep.setString(25, "");
            prep.setString(26, "");
            prep.setString(27, "");
            prep.setString(28, "");
            prep.setString(29, "");
            prep.setString(30, "");
            prep.setString(31, "");
            prep.setString(32, "");
            insercion = prep.executeUpdate();
            }catch (SQLException exception){

            }
            if (insercion > 0) {
                limpiarCampos();
                Toast.makeText(getApplicationContext(), "Se ingresó vale : " + papeleta + ".", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            mostrarErrorServer();
            //Toast.makeText(this.getApplicationContext(), "Error al ingresar."+e.getMessage(), Toast.LENGTH_LONG).show();

        }


    }

    public String obtenerFecha() {
        //2021-11-24 00:00:00.000
        String formato= Utilitarios.getDefaultsPreference("formatoconsultas", this);
        SimpleDateFormat sdfDate = new SimpleDateFormat(formato);//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void limpiarCampos() {
        edtAutobus.setText("");
        edtOperador.setText("");
        edtOperador2.setText("");
        edtLitros.setText("");
        edtPapeleta.setText("");
        edtKilometros.setText("");
        edtObresvaciones.setText("");
    }
    public void errorInternet() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_toast_layout, findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
    public void mostrarErrorServer() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.error_de_acceso, findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}