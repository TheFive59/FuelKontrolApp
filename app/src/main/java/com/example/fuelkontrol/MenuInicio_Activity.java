package com.example.fuelkontrol;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.android.volley.toolbox.StringRequest;
import com.example.fuelkontrol.activity.ConsultasActivity;
import com.example.fuelkontrol.activity.InsertarPapeletaActivity;
import com.example.fuelkontrol.activity.LoginAjustes;
import com.example.fuelkontrol.helper.DBHelper;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.helper.ValidarConectividad;
import com.example.fuelkontrol.prueba.MainActivity;
import com.example.fuelkontrol.util.Config;
import com.example.fuelkontrol.util.Utilidades;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MenuInicio_Activity extends AppCompatActivity {
    ManejadorDB miDB;
    ValidarConectividad valCon;
    String shrEstado;
    TextView txtUser, anio;
    BottomNavigationView closeS;
    BottomNavigationView navView;
    String shrTipoUsuario;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Handler handler;
   // ListView lista;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_inicio);
        navView = (BottomNavigationView) findViewById(R.id.nav_view);
        closeS = (BottomNavigationView) findViewById(R.id.menu_usuario);
        closeS.setBackgroundColor(getResources().getColor(R.color.color_transparente));
        navView.setBackgroundColor(getResources().getColor(R.color.color_transparente));
        //lista= (ListView) findViewById(R.id.listaNumero);
        shrTipoUsuario = Utilitarios.getDefaultsPreference("tipousuarioapp", this).trim();
        txtUser = findViewById(R.id.txtUser);
        txtUser.setText(new StringBuilder().append(getString(R.string.bienvenida)).
                append(Utilitarios.getDefaultsPreference(
                        "usuarioapp", this)).toString());
        anio = findViewById(R.id.txtAnio);
        anio.setText("✓ " + Calendar.getInstance().get(Calendar.YEAR));
        valCon = new ValidarConectividad();
        miDB = new ManejadorDB();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        shrEstado = (Utilitarios.getDefaultsPreference("usuario", getApplicationContext())).trim();
        dbHelper = new DBHelper(this, "FUELKONTROLADMIN", null, 1);
        //cargarDatosSql();
        handler = new Handler();
        ejecutarTarea();
        navView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_inicio:
                            if (!valCon.validarConectividad(getApplicationContext())) {
                                errorInicio();
                            } else {
                                txtUser.setTextSize(30);
                                txtUser.setTextColor((getResources().getColor(R.color.fondo)));
                                txtUser.setText(new StringBuilder().append(getString(R.string.
                                        bienvenida)).
                                        append(Utilitarios.getDefaultsPreference(
                                                "usuarioapp",
                                                MenuInicio_Activity.this)).toString());
                            }
                            break;
                        case R.id.navigation_insert:
                            int valorInsert = 1;
                            //insertMenu();
                            validarUsuario(valorInsert);
                            break;
                        case R.id.navigation_dashboard:
                            int valorSearch = 2;
                            validarUsuario(valorSearch);
                            //searchMenu();
                            break;
                        case R.id.navigation_notifications:
                            int valorDespacho = 3;
                            validarUsuario(valorDespacho);
                            //despachoMenu();
                            break;
                        case R.id.navigation_configuracion:
                            int valorSettings = 4;
                            validarUsuario(valorSettings);
                            //settingsMenu();
                            break;
                    }
                    return true;
                });

        closeS.setOnNavigationItemSelectedListener(
                item -> {
                    if (item.getItemId() == R.id.cerrarSesion) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(
                                MenuInicio_Activity.this);
                        alert.setTitle("Fuel Kontrol");
                        alert.setIcon(R.drawable.bg_fuel_kontrol);
                        alert.setMessage("Desea cerrar sesión\uD83D\uDD10?.");
                        alert.setPositiveButton("CERRAR SESION\uD83D\uDD13",
                                (dialog, whichButton) -> {
                                    shrEstado = "USUARIONORECORDADO";
                                    Utilitarios.setDefaultsPreference("usuario", shrEstado,
                                            MenuInicio_Activity.this);
                                    Utilitarios.setDefaultsPreference("biometricovalido",
                                            "DATOSINCOMPLETOS",
                                            MenuInicio_Activity.this);
                                    Utilitarios.setDefaultsPreference("huella",
                                            "NOHUELLA", MenuInicio_Activity.this);
                                    Utilitarios.setDefaultsPreference("verificador",
                                            "", MenuInicio_Activity.this);
                                    Utilitarios.setDefaultsPreference("verificador1",
                                            "", MenuInicio_Activity.this);


                                    Intent abrirLogin = new Intent(getApplicationContext(),
                                            LoginActivity.class);
                                    startActivity(abrirLogin);
                                    finish();
                                });
                        alert.setNegativeButton("      ✘       ",
                                (dialog, which) -> {
                                });
                        alert.show();
                    }
                    return true;
                });

    }

    private void validarUsuario(int buttonValue) {

        if (shrTipoUsuario.equals("1")) {
            usuarioRoot(buttonValue);
        } else if (shrTipoUsuario.equals("2")) {
            if (buttonValue == 1) {
                insertMenu();
            }
            /**
             * 2 insertar papeletas
             */
        } else if (shrTipoUsuario.equals("3")) {
            /**
             * 3 despacho impresion y vales
             */
            if (buttonValue == 1) {
                insertMenu();
            }
            if (buttonValue == 2) {
                searchMenu();
            }
            if (buttonValue == 3) {
                despachoMenu();
            }

        } else if (shrTipoUsuario.equals("4")) {
            /**
             * 4 despacho
             */
            if (buttonValue == 3) {
                despachoMenu();
            }

        } else if (shrTipoUsuario.equals("5")) {
            if (buttonValue == 2) {
                searchMenu();
            }
        }
    }

    private void usuarioRoot(int buttonValue) {
        if (buttonValue == 1) {
            insertMenu();
        }
        if (buttonValue == 2) {
            searchMenu();
        }
        if (buttonValue == 3) {
            despachoMenu();
        }
        if (buttonValue == 4) {
            settingsMenu();
        }
    }

    private void settingsMenu() {
        txtUser.setTextSize(30);
        txtUser.setTextColor((getResources().getColor(R.color.fondo)));
        txtUser.setText("A J U S T E S\nFUEL KONTROL");
        Intent abrirAjustes = new Intent(getApplicationContext(),
                // LoginAjustes.class);
                LoginAjustes.class);
        startActivity(abrirAjustes);
        finish();
    }

    private void despachoMenu() {
        if (!valCon.validarConectividad(getApplicationContext())) {
            errorInicio();
        } else if (valCon.validarConectividad(getApplicationContext())) {
            txtUser.setText("D E S P A C H O\nFUEL KONTROL");
            Intent abrirDespacho = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(abrirDespacho);
            this.finish();
        }
    }

    private void searchMenu() {
        if (!valCon.validarConectividad(getApplicationContext())) {
            errorInicio();
        } else if (valCon.validarConectividad(getApplicationContext())) {
            txtUser.setTextSize(30);
            txtUser.setTextColor((getResources().getColor(R.color.fondo)));
            txtUser.setText("C O N S U L T A S\nFUEL KONTROL");
            Intent abrirConsultas = new Intent(getApplicationContext(),
                    ConsultasActivity.class);
            startActivity(abrirConsultas);

        }
    }

    private void insertMenu() {
        if (!valCon.validarConectividad(getApplicationContext())) {
            errorInicio();
        } else if (valCon.validarConectividad(getApplicationContext())) {
            txtUser.setTextSize(30);
            txtUser.setTextColor((getResources().getColor(R.color.fondo)));
            txtUser.setText("V A L E S\nFUEL KONTROL");
            Intent abrirVales = new Intent(getApplicationContext(),
                    InsertarPapeletaActivity.class);
            startActivity(abrirVales);
        }
    }

    void errorInicio() {
        txtUser.setTextSize(16);
        txtUser.setTextColor((getResources().getColor(R.color.color_emitiendo_error)));
        txtUser.setText(getString(R.string.no_internet));
    }


    public void consultarValores() {
        try {
            ArrayList<String> datos = new ArrayList<String>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT * FROM " + Utilidades.TABLE_DESPACHO + " where " + Utilidades.KEY_Estado +
                    " =0 and " + Utilidades.KEY_FolioDespacho + " ='FKPRUEBAASINK';";
            Cursor fila = db.rawQuery(sql, null);
            if (fila.moveToFirst()) {
                do {
                    String Papeleta = fila.getString(0);
                    String Unidad = fila.getString(1);
                    int Manguera = Integer.parseInt(fila.getString(2));
                    Double LitrosPapeleta = Double.parseDouble(fila.getString(3));
                    Double LitrosSurtido = Double.parseDouble(fila.getString(4));
                    String FechaDespacho = fila.getString(5);
                    //String UsuarioApp = fila.getString(6);
                    //String Empresa = fila.getString(7);
                    String Referencia = fila.getString(8);
                    String FolioDespacho = fila.getString(9);
                    //(String pa, String un, int ma, Double lp, Double ld, String fd,String re,String fol) {
                    /*datos.add(fila.getString(1) + fila.getString(2) +
                            fila.getString(3) + fila.getString(4) +
                            fila.getString(5) + fila.getString(6) +
                            fila.getString(7) + fila.getString(8) +
                            fila.getString(9) + fila.getString(10));*/
                    /*insertarDespacho(Papeleta, Unidad, Manguera, LitrosPapeleta, LitrosSurtido,
                            FechaDespacho, Referencia, FolioDespacho);*/

                } while (fila.moveToNext());
                /*ArrayAdapter NoCoreAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, datos);
                lista.setAdapter(NoCoreAdapter);*/
            } else {
                //Toast.makeText(this, "NOENCONTRADO", Toast.LENGTH_LONG).show();
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Er " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void insertarDespacho(String pa, String un, int ma, Double lp, Double ld, String fd,
                                  String re, String fol) {

        PreparedStatement prep;

        try {
            prep = miDB.CONN(this).prepareStatement(
                    "INSERT INTO despacho (papeleta,unidad,bomba,manguera,litros_papeleta," +
                            "litros_surtidos,status_comunicacion,status_despacho,fecha_emision," +
                            "fecha_registro,usuario,importe,impuestos,total,cliente_operador," +
                            "empresa,id_almacen,status_exportacion,fecha_exportacion,km_papeleta," +
                            "odometro,odometro_ant,rendimiento_inst,tipo_movimiento,rendimiento_papeleta," +
                            "rendimiento_viaje,factor_km,km_viaje,litros_surtidos_parcial,referencia," +
                            "relleno,id_rol,id_servicio,distancia_traslado,cincho_actual_1,cincho_actual_2," +
                            "cincho_nuevo_1,cincho_nuevo_2,folio_despacho,TC01,TC02,TC03,TC06,TC08," +
                            "TC90,TC92,TC95,enviado,TC80,TC81,TC82,TC83,TC84,TC85,TC86,TC87,TC88,TC89) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?" +
                            "       ,?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?,?,?,?,?,?,?,?,?," +
                            "        ?,?,?,?,?,?,?,?);");
            prep.setString(1, pa);
            prep.setString(2, un);
            prep.setString(3, "1");
            prep.setInt(4, ma);
            prep.setDouble(5, lp);
            prep.setDouble(6, ld);
            prep.setString(7, "1");
            prep.setString(8, "1");
            prep.setString(9, fd);
            prep.setString(10, fd);
            prep.setString(11, Utilitarios.getDefaultsPreference("usuarioapp", this));
            prep.setDouble(12, 0.0);
            prep.setDouble(13, 0.0);
            prep.setString(14, "0.0");
            prep.setString(15, "0");
            prep.setString(16, Utilitarios.getDefaultsPreference("empresaajustes", this));
            prep.setString(17, "0");
            prep.setString(18, "0");
            prep.setString(19, fd);
            prep.setString(20, "1");
            prep.setString(21, "1");
            prep.setString(22, "1");
            prep.setString(23, "1");
            prep.setString(24, "1");
            prep.setString(25, "1");
            prep.setString(26, "1");
            prep.setString(27, "1");
            prep.setString(28, "1");
            prep.setString(29, "1");
            prep.setString(30, re);
            prep.setString(31, "1");
            prep.setString(32, "1");
            prep.setString(33, "1");
            prep.setString(34, "1");
            prep.setString(35, "1");
            prep.setString(36, "1");
            prep.setString(37, "1");
            prep.setString(38, "1");
            prep.setString(39, fol);
            prep.setString(40, "");
            prep.setString(41, "");
            prep.setString(42, "");
            prep.setString(43, "");
            prep.setString(44, "");
            prep.setString(45, "");
            prep.setString(46, "");
            prep.setString(47, "");
            prep.setString(48, "");
            prep.setString(49, "");
            prep.setString(50, "");
            prep.setString(51, "");
            prep.setString(52, "");
            prep.setString(53, "");
            prep.setString(49, "");
            prep.setString(54, "");
            prep.setString(55, "");
            prep.setString(56, "");
            prep.setString(57, "");
            prep.setString(58, "");
            int insercion = prep.executeUpdate();
            if (insercion > 0) {
                Toast.makeText(this, "Registro exitoso.", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException throwables) {
            Toast.makeText(this, "Error " + throwables, Toast.LENGTH_LONG).show();
            throwables.printStackTrace();
        }


    }

    private final int TIEMPO = 5000;

    public void ejecutarTarea() {

        handler.postDelayed(new Runnable() {
            public void run() {
                if (valCon.validarConectividad(MenuInicio_Activity.this) == true) {
                    consultarValores();
                }
                handler.postDelayed(this, TIEMPO);
            }
        }, TIEMPO);

    }

    public void cargarDatosSql() {
        try {
            dbHelper = new DBHelper(this, "FUELKONTROLADMIN", null, 1);
            SQLiteDatabase conn = dbHelper.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(Utilidades.KEY_Papeleta, 123);
            v.put(Utilidades.KEY_Unidad, 234);
            v.put(Utilidades.KEY_Manguera, 1);
            v.put(Utilidades.KEY_LitrosPapeleta, 10.0);
            v.put(Utilidades.KEY_LitrosSurtidos, 10.1);
            v.put(Utilidades.KEY_FechaDespacho, Utilitarios.obtenerFechaDespacho(this));
            v.put(Utilidades.KEY_UsuarioApp, Utilitarios.getDefaultsPreference("usuarioapp", this));
            v.put(Utilidades.KEY_Empresa, Utilitarios.getDefaultsPreference("empresaajustes", this));
            v.put(Utilidades.KEY_Referencia, "FK " + Utilitarios.obtenerFechaDespacho(this));
            v.put(Utilidades.KEY_FolioDespacho, "FKPRUEBAASINK");
            v.put(Utilidades.KEY_Estado, 0);
            conn.insert(Utilidades.TABLE_DESPACHO, null, v);
            //System.out.println(Utilidades.CREATE_TABLE_DESPACHO);
        } catch (SQLiteAbortException ex) {
            System.out.println("ERROR " + ex);
        }
    }

}