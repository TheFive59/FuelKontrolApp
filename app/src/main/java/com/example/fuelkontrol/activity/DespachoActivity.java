package com.example.fuelkontrol.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.adaptadores.HeaderAdaptadorVales;
import com.example.fuelkontrol.adaptadores.HeaderAdaptadorValesPendientes;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.ValidarConectividad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DespachoActivity extends AppCompatActivity {
    HeaderAdaptadorValesPendientes headerAdaptadorValesPendientes;
    ManejadorDB miDB;
    ValidarConectividad valCon;
    TextView edtUnidad, edtPapeleta;
    String unidadIngresada;
    RecyclerView rcvListaValesPendientes;
    Button btnSeleccionarPapeleta;
    /*Variables para actualizar el registro*/

    //Actualizar datosdiesel set paso where unidad an papeleta

    String litrosSurtidos, papeletaValidar, fechaRegistro;
    /*Variables para lectura*/
    String unidad,papeleta,litros;

    //Array List para almacenar la consulta
    ArrayList<HeaderAdaptadorVales> arregloValesPendientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despacho);

        Bundle unidadADespachar=getIntent().getExtras();
        if(unidadADespachar !=null){
            unidad=unidadADespachar.getString("unidadEnviada");
            papeleta=unidadADespachar.getString("papeletaEnviada");
            litros=unidadADespachar.getString("litrosEnviada");
            }
        rcvListaValesPendientes = findViewById(R.id.rcvListaValesPendientes);
        btnSeleccionarPapeleta = findViewById(R.id.btnSeleccionarPapeletas);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvListaValesPendientes.setLayoutManager(linearLayoutManager);
        miDB = new ManejadorDB();
        valCon = new ValidarConectividad();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // consultarValesPendientes();


        btnSeleccionarPapeleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(DespachoActivity.this);
                alert.setTitle("Fuel Kontrol");
                alert.setIcon(R.drawable.bg_fuel_kontrol);
                alert.setMessage("Ingresa unidad\uD83D\uDE9B.");
                EditText input = new EditText(DespachoActivity.this);
                input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                input.setWidth(100);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setSingleLine();
                input.setHint("Ingresa unidad");
                input.setTextSize(12);
                input.setTextColor(getResources().getColor(R.color.purple_700));
                input.setBackgroundResource(R.drawable.stylo_borde_editext);
                input.setFocusable(true);
                input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                alert.setView(input);
                alert.setPositiveButton("BUSCAR\uD83D\uDD0D", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        unidadIngresada = input.getText().toString();
                        consultarValesPendientes(unidadIngresada);
                        return;
                    }
                });
                alert.setNegativeButton("    ✘     ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        consultarValesPendientes(unidadIngresada);
                    }
                });
                alert.show();
            }
        });
    }

    public void consultarValesPendientes(String Unidad) {
        miDB.CONN(DespachoActivity.this);
        String obtenerVales = "SELECT PapeletaId, AutobusId, PapLitros, PapFemision from DATOSDIESEL where\n" +
                " PapFemision BETWEEN '" + obtenerFecha() + "' AND '" +
                obtenerFecha() + " 23:59:59' and paso=0 and AutobusId='" + Unidad + "' order by PapFemision desc;";
        try {

            Statement comm = miDB.CONN(this).createStatement();
            ResultSet rs = comm.executeQuery(obtenerVales);
            arregloValesPendientes = new ArrayList<>();

            try {

                while (rs.next()) {
                    arregloValesPendientes.add(new HeaderAdaptadorVales(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                    //Toast.makeText(getApplicationContext(), rs.getString("PAPFEMISION"), Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getApplicationContext(), "Se cargaron " + String.valueOf(arregloValesPendientes.size()) + " papeletas.", Toast.LENGTH_LONG).show();
                headerAdaptadorValesPendientes = new HeaderAdaptadorValesPendientes(arregloValesPendientes);
                rcvListaValesPendientes.setAdapter(headerAdaptadorValesPendientes);
            } catch (SQLException ec) {
                Toast.makeText(this, "Error  " + ec.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException ex) {
            Toast.makeText(DespachoActivity.this, "No se cargaron papeletas.\n Error :" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public String obtenerFecha() {
        //2021-11-24 00:00:00.000
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
/*<ImageView
                android:id="@+id/img_connect"
                android:layout_margin="10dp"
                android:layout_width="30dp"
                android:layout_height="30dp"
                android:background="?selectableItemBackgroundBorderless"
                android:src="@drawable/ic_button_vincular"
                />*/