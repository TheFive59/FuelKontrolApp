package com.example.fuelkontrol.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fuelkontrol.PrintActivity;
import com.example.fuelkontrol.R;
import com.example.fuelkontrol.adaptadores.Header;
import com.example.fuelkontrol.adaptadores.HeaderAdapter;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.helper.ValidarConectividad;
import com.example.fuelkontrol.util.DatePickerFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ConsultasActivity extends AppCompatActivity {

    TextView txtUsuario1;
    TextView txtUnidad2;
    TextView txtOdometro3;
    TextView txtPapeleta4;
    TextView txtLitrosPapeleta5;
    TextView txtLitrosSurtidos6;
    TextView txtFecha7;


    private HeaderAdapter headerAdapter;
    private ManejadorDB miDB;
    private ValidarConectividad valCon;
    private RecyclerView rcvLista;
    private Spinner spnEmpresa;
    private ArrayList<Header> myDataSet;
    private EditText edtFechaInicio, edtFechaFin, edtUnidad;
    private String shrPreferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtNumeroRegistros, txtTipoConsulta;
    private int estado;
    private SwitchCompat swConsultaDiario;
    public boolean estadoSw=true;
    CheckBox checkBoxImprimir;
    /*
     * Despacho
     */
    File ruta_sd;
    File carpeta_FuelKontrol;
    File archivo;
    Uri uri;
    // declaring width and height
    // for our PDF file.
    int pageHeight = 500;
    int pagewidth = 400;

    // creating a bitmap variable
    // for storing our images
    Bitmap bmp, scaledbmp;

    // constant code for runtime permissions
    private static final int PERMISSION_REQUEST_CODE = 200;

    //Variables para impresion
    String usuarioT = "";
    String unidadT = "";
    String odometroT = "";
    String papeletaT = "";
    String litrosPapeletaT = "";
    String litrosSurtidoT = "";
    String fechaT = "";
    String nombreArchivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);
        mostrarAlerta();
        //Recepcion de datos del recycler view
        // if(intent.hasExtra(Intent.EXTRA_TEXT)) {
        rcvLista = findViewById(R.id.rcvLista);
        edtFechaInicio = findViewById(R.id.edtFechaInicio);
        edtFechaFin = findViewById(R.id.edtFechaFin);
        spnEmpresa = findViewById(R.id.spnEmpresa);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        txtNumeroRegistros = findViewById(R.id.txtNumeroRegistros);
        txtTipoConsulta = findViewById(R.id.txtTipoConsulta);
        edtUnidad = findViewById(R.id.edtUnidad);

        edtFechaInicio.setText(fijarFecha());
        edtFechaFin.setText(fijarFecha());
        edtFechaFin.setText(fijarFecha());

        swConsultaDiario = findViewById(R.id.swConsultaDiario);
        swConsultaDiario.setChecked(true);

        txtUsuario1 = findViewById(R.id.Usurio1);
        txtUnidad2 = findViewById(R.id.unidad1);
        txtOdometro3 = findViewById(R.id.odometro1);
        txtPapeleta4 = findViewById(R.id.Papeleta1);
        txtLitrosPapeleta5 = findViewById(R.id.litrosPapeleta1);
        txtLitrosSurtidos6 = findViewById(R.id.litrosdespacho1);
        txtFecha7 = findViewById(R.id.fecha1);


        checkBoxImprimir = findViewById(R.id.chbImprimir);
        checkBoxImprimir.setChecked(false);
        shrPreferences = (Utilitarios.getDefaultsPreference("tipoUsuario", this));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvLista.setLayoutManager(linearLayoutManager);
        miDB = new ManejadorDB();
        valCon = new ValidarConectividad();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (valCon.validarConectividad(getApplicationContext()) == false) {
            sinInternet();
        } else if (valCon.validarConectividad(getApplicationContext()) == true) {
            try {

                cargarSpinner();
                cargarLista();
            } catch (Exception ex) {
                mostrarErrorServer();
            }
        }
        /**
         * PRINT
         */
        ruta_sd = Environment.getExternalStorageDirectory();

        carpeta_FuelKontrol = new File(ruta_sd.getAbsolutePath(), "FuelKontrol");
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo_fuel);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 100, 95, false);

        // below code is used for
        // checking our permissions.
        if (Utilitarios.checkPermission(getApplicationContext())) {
        } else {
            Utilitarios.requestPermission(PERMISSION_REQUEST_CODE, this);
        }
        edtFechaInicio.setOnClickListener(v -> showDatePickerDialog());
        checkBoxImprimir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBoxImprimir.isChecked()) {
                    generatePDF();
                }
            }
        });
        swConsultaDiario.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (swConsultaDiario.isChecked()) {
                    estadoSw = true;
                } else {
                    estadoSw = false;
                }
            }
        });
        edtFechaFin.setOnClickListener(v -> showDatePickerDialog1());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }

            @Override
            public void onRefresh() {
                if (!valCon.validarConectividad(getApplicationContext())) {
                    sinInternet();
                } else {
                    cargarLista();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    // R.style.DialogTheme
    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String fechaInicioConsultaDespacho = year + "/" + twoDigits(day) + "/" + twoDigits(month + 1);
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month + 1) + "/" + year;
                //final String selectedDate = day + " / " + (month+1) + " / " + year;
                edtFechaInicio.setText(fechaInicioConsultaDespacho);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void showDatePickerDialog1() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month + 1) + "/" + year;
                //Año-dia-mes*/
                final String fechaFinConsultaDespacho = year + "/" + twoDigits(day) + "/" + twoDigits(month + 1);
                //final String selectedDate = day + " / " + (month+1) + " / " + year;
                edtFechaFin.setText(fechaFinConsultaDespacho);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void cargarLista() {
        /**
         *Query para consultar por fechas
         */
        String queryConsultaEmpresa = "Select Papeleta, Usuario, Unidad, Odometro, Litros_Papeleta," +
                "Litros_Surtidos, Fecha_Registro from despacho where fecha_registro BETWEEN '" +
                edtFechaInicio.getText() + "' AND '" + edtFechaFin.getText() + " 23:59:59' and " +
                "empresa=(Select id_empresa from empresas where clave_empresa='"
                + spnEmpresa.getSelectedItem().toString().trim() + "')order by fecha_registro desc;";
        String queryReadByUnidad = "Select Papeleta, Usuario, Unidad, Odometro, Litros_Papeleta, \n" +
                "Litros_Surtidos, Fecha_Registro from despacho \n" +
                "where fecha_registro BETWEEN '" + edtFechaInicio.getText() + "' AND '" + edtFechaFin.getText()
                + " 23:59:59' and Unidad='" + edtUnidad.getText() + "' order " +
                "by fecha_registro desc;";
        String queryReadByFechas = "Select Papeleta, Usuario, Unidad, Odometro, Litros_Papeleta, " +
                "Litros_Surtidos, Fecha_Registro from despacho where fecha_registro BETWEEN '"
                + edtFechaInicio.getText() + "' AND '" + edtFechaFin.getText() + " 23:59:59' " +
                "order by fecha_registro desc;";

        //Si no hay unidad ni empresa
        if (valCon.validarConectividad(getApplicationContext()) == false) {
            sinInternet();
        } else if (valCon.validarConectividad(getApplicationContext()) == true) {
            txtTipoConsulta.setText("");
            if (estadoSw == true) {
                metodoSelect(queryReadByFechas);
                txtTipoConsulta.setText("Registros por fechas.");
                swConsultaDiario.setChecked(false);
            } else if (!edtUnidad.getText().toString().equals("") || spnEmpresa.getSelectedItem().toString().equals("SELECCIONE") && estadoSw == false) {
                metodoSelect(queryReadByUnidad);
                txtTipoConsulta.setText("Registros por unidad.");
                edtUnidad.setText("");
                swConsultaDiario.setChecked(false);
            } else if (!spnEmpresa.getSelectedItem().toString().equals("SELECCIONE") && edtUnidad.getText().toString().isEmpty() && estadoSw == false) {
                metodoSelect(queryConsultaEmpresa);
                selectSpinnerValue(spnEmpresa, "SELECCIONE");
                txtTipoConsulta.setText("Registros por empresa.");

            }
        }
    }

    /**
     * Método general para consultar que recibe un parametro de tipo String.
     *
     * @param querysql
     */
    public void metodoSelect(String querysql) {

        try {
            miDB.CONN(this);
            Statement comm = miDB.CONN(this).createStatement();
            ResultSet rs = comm.executeQuery(querysql);
            myDataSet = new ArrayList<>();
            try {
                while (rs.next()) {
                    myDataSet.add(new Header(rs.getString(1), rs.getString(2),
                            rs.getString(3), rs.getString(4), rs.getString(5),
                            rs.getString(6), rs.getString(7)));
                }
                headerAdapter = new HeaderAdapter(myDataSet);
                rcvLista.setAdapter(headerAdapter);
                estado = myDataSet.size();
                txtNumeroRegistros.setText(String.valueOf(estado));
            } catch (SQLException ec) {
                mostrarErrorServer();
            }
        } catch (SQLException ex) {
            mostrarErrorServer();
        }
    }

    public void cargarSpinner() {
        try {
            miDB.CONN(this);
            String queryRead = "select clave_empresa from empresas;";
            Statement comm = miDB.CONN(this).createStatement();
            ResultSet rs = comm.executeQuery(queryRead);
            try {
                ArrayList<String> data = new ArrayList<String>();
                data.add("SELECCIONE");
                while (rs.next()) {
                    String id = rs.getString("Clave_Empresa");
                    data.add(id);
                }
                //ArrayAdapter NoCoreAdapter = new ArrayAdapter(this,R.layout.spinner_item, data);
                ArrayAdapter NoCoreAdapter = new ArrayAdapter(this, R.layout.spinner_item, data);
                spnEmpresa.setAdapter(NoCoreAdapter);
            } catch (SQLException ec) {
                mostrarErrorServer();
            }
        } catch (SQLException ex) {
            mostrarErrorServer();
        }
    }

    public String obtenerFechaDespacho() {
        //2021-11-24 00:00:00.000

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/dd/MM HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    public String obtenerFechaPdf() {
        //2021-11-24 00:00:00.000

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyddMMHHmmss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public String fijarFecha() {
        //2021-11-24 00:00:00.000
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/dd/MM");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    //Llenar spinner Empresa
    public void mostrarAlerta() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.mostrar_swipe, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(ConsultasActivity.this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    private String twoDigits(int n) {
        return (n <= 9) ? ("0" + n) : String.valueOf(n);
    }

    private void selectSpinnerValue(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(myString)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public void sinInternet() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_toast_layout, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public void mostrarErrorServer() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.error_de_acceso, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }


    private void generatePDF() {
        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        Paint paint = new Paint();
        Paint title = new Paint();

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight,
                1).create();

        // below line is used for setting
        // start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.

        //canvas.drawBitmap(scaledbmp, 30, 25, paint);
        //canvas.drawText("Fuel Kontrol " + Calendar.getInstance().get(Calendar.YEAR), 209, 80, title);

        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15);

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.fondo));
        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
        String folio = txtUnidad2.getText().toString() + txtPapeleta4.getText().toString()+obtenerFechaPdf()+"513";
        canvas.drawBitmap(scaledbmp, 30, 50, paint);
        canvas.drawText("T  i  c  k  e  t    d  e    d  e  s  p  a  c  h  o  .", 90, 40, title);
        canvas.drawText("FUEL KONTROL " + Calendar.getInstance().get(Calendar.YEAR), 140, 80, title);
        canvas.drawText(Utilitarios.getDefaultsPreference("empresaajustes", this), 140, 120, title);
        canvas.drawText("Vale", 100, 190, title);
        canvas.drawText(": " + txtUnidad2.getText().toString(), 220, 190, title);

        canvas.drawText("Usuario", 100, 220, title);
        canvas.drawText(": " + txtUsuario1.getText().toString(), 220, 220, title);

        canvas.drawText("Odometro", 100, 250, title);
        canvas.drawText(": " + txtOdometro3.getText().toString(), 220, 250, title);

        canvas.drawText("Litros" + " lts", 100, 280, title);
        canvas.drawText(": " + txtLitrosPapeleta5.getText().toString() + " lts", 220, 280, title);

        canvas.drawText("Litros surtidos", 100, 310, title);
        canvas.drawText(": " + txtLitrosSurtidos6.getText().toString()+ " lts", 220, 310, title);

        canvas.drawText("Fecha despacho", 60, 360, title);
        canvas.drawText(": " + txtFecha7.getText().toString(), 180, 360, title);
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Creación", 130, 450, title);
        canvas.drawText(": " + obtenerFechaDespacho(), 250, 450, title);

        canvas.drawText("FK"+folio, 290, 490, title);

        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.fondo));
        title.setTextSize(15);

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.CENTER);
        //canvas.drawText("FK", 396, 560, title);

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);
        nombreArchivo = txtUnidad2.getText().toString() + txtPapeleta4.getText().toString()+obtenerFechaPdf()+".pdf";
        // below line is used to set the name of
        // our PDF file and its path

        if (!carpeta_FuelKontrol.exists()) {
            carpeta_FuelKontrol.mkdir();
        }
        try {
            // after creating a file name we will
            // write our PDF file to that location.
            if (!carpeta_FuelKontrol.exists()) {
                carpeta_FuelKontrol.mkdir();
            } else {
                archivo = new File(ruta_sd.getAbsolutePath() + "/FuelKontrol", nombreArchivo);
                pdfDocument.writeTo(new FileOutputStream(archivo));
                shareTicket();
            }
            // below line is to print toast message
            // on completion of PDF generation.
            //Toast.makeText(PrintActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            checkBoxImprimir.setChecked(false);
            Toast.makeText(ConsultasActivity.this, "Error de escritura." + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permiso otorgado.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    void shareTicket() {
        archivo = new File(ruta_sd.getAbsolutePath(), "FuelKontrol/"+nombreArchivo);
        uri = FileProvider.getUriForFile(ConsultasActivity.this, "com.example.fuelkontrol.fileprovider", archivo);
        Intent intent = ShareCompat.IntentBuilder.from(ConsultasActivity.this)
                .setType("application/pdf")
                .setStream(uri)
                .setChooserTitle("Compartir a través")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ConsultasActivity.this.startActivity(intent);
        checkBoxImprimir.setChecked(false);
    }
}