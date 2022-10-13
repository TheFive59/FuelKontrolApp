package com.example.fuelkontrol.prueba;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.adaptadores.HeaderAdaptadorVales;
import com.example.fuelkontrol.adaptadores.HeaderAdaptadorValesPendientes;
import com.example.fuelkontrol.helper.DBHelper;
import com.example.fuelkontrol.helper.ExitApp;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.helper.ValidarConectividad;
import com.example.fuelkontrol.util.Utilidades;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {
    /**
     * Alert personalizao
     */
    Dialog customDialog = null;

    public enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;
    private TextView textoRecibido;
    private TextView receiveText;
    private ProgressBar progressBar;
    private TextView lblDespacho;
    TextView lblStatus;
    ArrayList<String> myDataSet;
    /**
     * Variables de validacion posicion 1
     */
    String respuesta1 = null;
    String respuesta2 = null;
    String respuesta3 = new String();
    String val3 = new String();
    String val1 = null, val2 = null;
    private int manguera, manguera2;
    private double litrosSurtir;
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;
    int times;

    HeaderAdaptadorValesPendientes headerAdaptadorValesPendientes;
    ManejadorDB miDB;
    ValidarConectividad valCon;
    String unidadIngresada;
    RecyclerView rcvListaValesPendientes;
    Button btnSeleccionarPapeleta;
    String unidad, papeleta, litros;
    ArrayList<HeaderAdaptadorVales> arregloValesPendientes;
    LayoutInflater inflater1;
    View view1;
    LayoutInflater inflater2;
    View view2;
    LayoutInflater inflaterLista;
    View viewLista;

    SwitchCompat swStatusd1, swStatusd2;
    /**
     * Variables para la insercion del despacho
     */
    TextView txtUnidadP1, txtUnidadP2,
            txtPapD1, txtPapD2,
            txtLitrosPap1, txtLitrosPap2,
            txtLitrosBomba1, txtLitrosBomba2;
    String unidadDespacho, papeletaDespacho, folioDespacho;
    Double litrosPapDespacho, litrosBombaDespacho;
    int mangueraDespacho;

    /*
     *SQLITE
     */
    DBHelper dbHelper;
    SQLiteDatabase db;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        Bundle unidadADespachar = getArguments();
        if (unidadADespachar != null) {
            unidad = unidadADespachar.getString("unidadEnviada");
            papeleta = unidadADespachar.getString("papeletaEnviada");
            litros = unidadADespachar.getString("litrosEnviada");
        }
        lblStatus = view.findViewById(R.id.lblStatus);
        txtUnidadP1 = view.findViewById(R.id.UnidadB1);
        txtUnidadP2 = view.findViewById(R.id.UnidadB2);
        txtPapD1 = view.findViewById(R.id.PapeletasB1);
        txtPapD2 = view.findViewById(R.id.PapeletasB2);
        txtLitrosPap1 = view.findViewById(R.id.LitrosB1);
        txtLitrosPap2 = view.findViewById(R.id.LitrosB2);
        txtLitrosBomba1 = view.findViewById(R.id.txtLitrosG);
        txtLitrosBomba2 = view.findViewById(R.id.txtLitrosG2);
        inflater1 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view1 = inflater1.inflate(R.layout.error_de_acceso, null);
        RelativeLayout rltV1 = (RelativeLayout) view1.findViewById(R.id.relativeLayout1);

        inflater2 = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view1 = inflater2.inflate(R.layout.error_de_acceso, null);
        RelativeLayout rltV2 = (RelativeLayout) view1.findViewById(R.id.relativeLayout1);

        inflaterLista = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewLista = inflaterLista.inflate(R.layout.item_vales_pendientes, container, false);

        swStatusd1 = (SwitchCompat) view.findViewById(R.id.swStatusD1);
        swStatusd2 = (SwitchCompat) view.findViewById(R.id.swStatusD2);

        rcvListaValesPendientes = view.findViewById(R.id.rcvListaValesPendientes);
        btnSeleccionarPapeleta = view.findViewById(R.id.btnSeleccionarPapeletas);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcvListaValesPendientes.setLayoutManager(linearLayoutManager);

        miDB = new ManejadorDB();
        valCon = new ValidarConectividad();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        dbHelper = new DBHelper(getActivity(), "FUELKONTROLADMIN", null, 1);

        db = dbHelper.getWritableDatabase();

        swStatusd1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (swStatusd1.isChecked()) {
                    progressBar.setProgress(0);
                    lblDespacho.setText("Activando despacho de B1...");
                    String enviar = "01HIDE\n";
                    //String enviar = "01HIDE\\N 02HIDE";
                    send(enviar);
                    Utilitarios.setDefaultsPreference("verificador", "Despachando", getActivity());
                    litrosSurtir = Double.parseDouble(txtLitrosPap1.getText().toString());
                    manguera = 1;
                    obtenerDatosARegistrar();
                    mangueraDespacho = 1;
                    updateDatosDiesel();
                    consultarValesPendientes(txtUnidadP1.getText().toString().trim());
                }
            }
        });
        swStatusd2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (swStatusd2.isChecked()) {
                    lblDespacho.setText("Activando despacho de B2...");
                    progressBar.setProgress(0);
                    String enviar = "02HIDE\n";
                    send(enviar);
                    Utilitarios.setDefaultsPreference("verificador2", "Despachando2", getActivity());
                    litrosSurtir = Double.parseDouble(txtLitrosPap2.getText().toString());
                    manguera2 = 2;
                    obtenerDatosARegistrar();
                    mangueraDespacho = 2;
                    updateDatosDiesel();
                    consultarValesPendientes(txtUnidadP2.getText().toString().trim());
                }
            }
        });
        mostrar();
        //mostrarIngresarUnidad();
        btnSeleccionarPapeleta.setOnClickListener(v -> {
            //mostrarIngresarUnidad();
            mostrar();
        });
        txtLitrosBomba2 = view.findViewById(R.id.txtLitrosG2);
        textoRecibido = view.findViewById(R.id.txtEstatusBluetooth);
        myDataSet = new ArrayList<String>(3);
        times = 0;
        progressBar = view.findViewById(R.id.progressBar);
        lblDespacho = view.findViewById(R.id.lblProgreso);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
        // menu.findItem(R.id.hex).setChecked(hexEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.restart) {
            send("01NPERM\\N 02NPERM");
            ExitApp.now(getActivity(), R.string.double_back_pressed);
            Utilitarios.setDefaultsPreference("verificador",
                    "", getActivity());
            Utilitarios.setDefaultsPreference("verificador2",
                    "", getActivity());/*
            getActivity().getFragmentManager().popBackStack();
            getFragmentManager().beginTransaction().remove(this).commit();
            getActivity().finish();*/

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("Conectando...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if (connected != Connected.True) {
            Toast.makeText(getActivity(), getString(R.string.text_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if (hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // receiveText.append(spn);
            //textoRecibido.setText("");
            service.write(data);

        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    private void receive(byte[] data) {
        if (hexEnabled) {
            //textoRecibido.append(TextUtil.toHexString(data) + '\n');
        } else {
            String msg = new String(data);
            if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                // don't show CR as ^M if directly before LF
                msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                // special handling if CR and LF come in separate fragments
                if (pendingNewline && msg.charAt(0) == '\n') {
                    Editable edt = textoRecibido.getEditableText();
                    if (edt != null && edt.length() > 1)
                        edt.replace(edt.length() - 2, edt.length(), "");
                }
                pendingNewline = msg.charAt(msg.length() - 1) == '\r';
            }
            //lblStatus.append(TextUtil.toCaretString(msg, newline.length() != 0));
        }
    }

    private void status(String str) {
        textoRecibido.setText(str);
       /* SpannableStringBuilder spn = new SpannableStringBuilder(str);
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)),
                0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        //textoRecibido.append(spn);
        //textoRecibido.append(spn);
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("Conectado");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("Conexión fallida,sin lectura, o el el dispositivo está apagado.");
        //status("Conexión fallida: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        //receive(data);
        //lblStatus.setText(receive(data));
        String res1puesta = new String(data, StandardCharsets.UTF_8);
        lblStatus.setText(res1puesta);
        String eP = Utilitarios.getDefaultsPreference("verificador", getActivity());
        String eP2 = Utilitarios.getDefaultsPreference("verificador2", getActivity());
        System.out.println("Aqui" + res1puesta);
        if (manguera == 1) {
            if (eP == "showInicio" && validacionFound(data, "01")) {

                if (manguera == 1) {
                    mostrarDigitos(data, "01");
                    lblDespacho.setText("Finalizando...");
                    if (val1.equals(val2) && val1.equals(val3) && val2.equals(val3)) {
                        //System.out.println("Entro 1");
                        send("01STOP\n");
                        progressBar.setProgress(0);
                        swStatusd1.setChecked(false);
                        txtLitrosBomba1.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Despacho finalizado B1");
                        txtUnidadP1.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Registrando... B1");
                        obtenerDatosARegistrar();
                        mangueraDespacho = 1;
                        txtPapD1.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        insertarDespacho();
                        //updateDatosDiesel();
                        lblDespacho.setText("Despacho Finalizado B1");
                        txtLitrosPap1.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        Utilitarios.setDefaultsPreference("verificador", "DespachoFinalizado", getActivity());
                        consultarValesPendientes(txtUnidadP1.getText().toString().trim());
                        //textoLitros.setBackgroundColor(getResources().getColor(R.color.color_despacho_activo));
                    } else {
                        System.out.println("Error 1");
                        val1 = null;
                        val2 = null;
                        val3 = null;
                    }
                }/* else {
                    foundPosicion2(data, "02");
                    if (val1de2.equals(val2de2) && val1de2.equals(val3de2) && val2de2.equals(val3de2)) {
                        System.out.println(val1de2 + val2de2 + val3de2);
                        send("02STOP\n");
                        progressBar.setProgress(0);
                        swStatusd2.setChecked(false);
                        txtLitrosBomba2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Despacho finalizado");
                        Utilitarios.setDefaultsPreference("verificador", "DespachoFinalizado2", getActivity());
                    } else {
                        System.out.println("Error");
                        val1de2 = null;
                        val2de2 = null;
                        val3de2 = null;
                    }
                }*/

            }
        }
        if (manguera2 == 2) {
            //System.out.println("Posicion 2");
            if (eP2 == "showInicio" && validacionFound2(data, "02")) {

                if (manguera2 == 2) {
                    mostrarDigitos(data, "02");
                    lblDespacho.setText("Finalizando...");
                    if (val1.equals(val2) && val1.equals(val3) && val2.equals(val3)) {
                        //System.out.println("Entro 1");
                        send("02STOP\n");
                        progressBar.setProgress(0);
                        swStatusd2.setChecked(false);
                        txtLitrosBomba2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Despacho finalizado B2");
                        txtUnidadP2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Registrando B2...");
                        obtenerDatosARegistrar();
                        mangueraDespacho = 2;
                        txtPapD2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        insertarDespacho();
                        // updateDatosDiesel();
                        lblDespacho.setText("Despacho Finalizado B2");
                        txtLitrosPap2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        Utilitarios.setDefaultsPreference("verificador2", "DespachoFinalizado2", getActivity());
                        //textoLitros.setBackgroundColor(getResources().getColor(R.color.color_despacho_activo));
                        consultarValesPendientes(txtUnidadP2.getText().toString().trim());
                    } else {
                        System.out.println("Error 2");
                        val1 = null;
                        val2 = null;
                        val3 = null;
                    }
                }/* else {
                    foundPosicion2(data, "02");
                    if (val1de2.equals(val2de2) && val1de2.equals(val3de2) && val2de2.equals(val3de2)) {
                        System.out.println(val1de2 + val2de2 + val3de2);
                        send("02STOP\n");
                        progressBar.setProgress(0);
                        swStatusd2.setChecked(false);
                        txtLitrosBomba2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Despacho finalizado");
                        Utilitarios.setDefaultsPreference("verificador", "DespachoFinalizado2", getActivity());
                    } else {
                        System.out.println("Error");
                        val1de2 = null;
                        val2de2 = null;
                        val3de2 = null;
                    }
                }*/

            }
        }
       /* if (manguera == 2) {
            if (eP2 == "showInicio" && validacionFound2(data)) {
                if (manguera == 2) {
                    foundPosicion2(data, "02");
                    if (val1de2.equals(val2de2) && val1de2.equals(val3de2) && val2de2.equals(val3de2)) {
                        send("02STOP\n");
                        swStatusd1.setChecked(false);
                        progressBar.setProgress(0);
                        txtLitrosBomba1.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Despacho finalizado");
                        Utilitarios.setDefaultsPreference("verificador2", "DespachoFinalizado2", getActivity());
                    } else {
                        System.out.println("Error 2");
                        val1 = null;
                        val2 = null;
                        val3 = null;
                    }
                } else {
                    foundPosicion2(data, "02");
                    if (val1de2.equals(val2de2) && val1de2.equals(val3de2) && val2de2.equals(val3de2)) {
                        System.out.println("Entro2");
                        System.out.println(val1de2 + val2de2 + val3de2);
                        send("02STOP\n");
                        progressBar.setProgress(0);
                        //txtLitrosBomba2.setBackgroundColor(getResources().getColor(R.color.color_despacho_activo));
                        txtLitrosBomba2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_despacho_finalizado));
                        lblDespacho.setText("Despacho finalizado");
                        Utilitarios.setDefaultsPreference("verificador2", "DespachoFinalizado2", getActivity());
                        swStatusd2.setChecked(false);
                    } else {
                        System.out.println("Error 2");
                        val1de2 = null;
                        val2de2 = null;
                        val3de2 = null;
                    }
                }

            }
        }*/
        if (manguera == 1) {
            despacho(data, "01", litrosSurtir);
        }
        if (manguera2 == 2) {
            despacho2(data, "02", litrosSurtir);
        }

        if (manguera == 1) {
            if (eP != "Despachando" && eP != "Despachando" && eP != "DespachoFinalizado1")
                glits(data, "01");
        } else {
            if (eP2 != "Despachando" && eP != "Despachando" && eP != "DespachoFinalizado2")
                glits(data, "02");
        }
        if (manguera2 == 2) {
            if (eP != "Despachando" && eP != "Despachando" && eP != "DespachoFinalizado1")
                glits(data, "02");
        } else {
            if (eP2 != "Despachando" && eP != "Despachando" && eP != "DespachoFinalizado2")
                glits(data, "02");
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        //status("Se perdio la conexión: " + e.getMessage());
        status("Se perdio la conexión: BT desactivado.");
        disconnect();
    }

    /**
     * Obtenemos las respuestas cada 10 segundos para validar que el despacho realmente ha terminado.
     *
     * @param data
     * @param pos
     * @return
     */
    String mostrarDigitos(byte[] data, String pos) {
//Compartidos
        String busqueda = pos + "Found";
        String patron = "\\d+\\.\\d+";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);

        respuesta3 = new String(data, StandardCharsets.UTF_8);
        //Esperar lectura respuesta dos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                respuesta1 = new String(data, StandardCharsets.UTF_8);
                Matcher match1 = regex.matcher(respuesta1);
                if (match1.find()) {
                    Pattern pattern1 = Pattern.compile(patron);
                    Matcher matcher1 = pattern1.matcher(respuesta1);

                    while (matcher1.find()) {
                        val2 = matcher1.group();

                    }
                }
            }
        }, 9000);
        //Esperar lectura respuesta dos
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            public void run() {
                respuesta2 = new String(data, StandardCharsets.UTF_8);
                Matcher match2 = regex.matcher(respuesta2);
                if (match2.find()) {
                    Pattern pattern2 = Pattern.compile(patron);
                    Matcher matcher2 = pattern2.matcher(respuesta1);

                    while (matcher2.find()) {
                        val3 = matcher2.group();

                    }
                }
            }
        }, 10000);


        Matcher match = regex.matcher(respuesta3);
        if (match.find()) {
            Pattern pattern = Pattern.compile(patron);
            Matcher matcher = pattern.matcher(respuesta3);

            while (matcher.find()) {
                val1 = matcher.group();
            }
        }
        return "";
    }

    /**
     * Validar que las respuestas sean equivalentes al comando Found para hacer uso del metodo que obtiene las respuestas.
     */
    boolean validacionFound(byte[] data, String pos) {
        String respuesta = new String(data, StandardCharsets.UTF_8);
        Boolean validacionfoundok = false;
        String busqueda = pos + "Found";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            validacionfoundok = true;
        }
        return validacionfoundok;
    }

    boolean validacionFound2(byte[] data, String pos) {
        String respuesta = new String(data, StandardCharsets.UTF_8);
        Boolean validacionfoundok = false;
        String busqueda = pos + "Found";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            validacionfoundok = true;
        }
        return validacionfoundok;
    }

    /**
     * boolean validacionFound2(byte[] data) {
     * String respuesta = new String(data, StandardCharsets.UTF_8);
     * Boolean validacionfoundok = false;
     * String busqueda = "02Found";
     * Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);
     * Matcher match = regex.matcher(respuesta);
     * if (match.find()) {
     * validacionfoundok = true;
     * }
     * return validacionfoundok;
     * }
     * <p>
     * <p>
     * METODO que ejecuta el despacho enviando la secuencia de comandos
     *
     * @param data
     */
    public void despacho(byte[] data, String pos, Double litros) {
        String s = new String(data, StandardCharsets.UTF_8);

        String verificador = "Despachando";
        try {
            //Verificador despacho vD
            String vD = Utilitarios.getDefaultsPreference("verificador", getActivity());
            // System.out.println(vD + "INICIO");
            if (hide1(s) && vD == "Despachando") {
                //timer.schedule(verificarLectura, 1000, 1000);
                progressBar.setProgress(15);
                lblDespacho.setText("15%");
                verificador = "hideInicio";
                Utilitarios.setDefaultsPreference("verificador", verificador, getActivity());
                esperarEnviarNPerm(5000, pos);
                progressBar.setProgress(35);
                lblDespacho.setText("35%");
            } else if (hide1(s) == false) {
            }
            String vN = Utilitarios.getDefaultsPreference("verificador", getActivity());
            if (nperm(pos, s) && vN == "hideInicio") {
                progressBar.setProgress(55);
                lblDespacho.setText("55%");
                verificador = "npermInicio";
                Utilitarios.setDefaultsPreference("verificador", verificador, getActivity());
                esperarEnviarStop(5000, pos);
            }
            String vS = Utilitarios.getDefaultsPreference("verificador", getActivity());
            if (stop(pos, s) && vS == "npermInicio") {

                progressBar.setProgress(75);
                lblDespacho.setText("75%");
                verificador = "stopInicio";
                Utilitarios.setDefaultsPreference("verificador", verificador, getActivity());
                esperarEnviarSurtir(5000, pos, litros);

            }
            //String respuesta,String pos,Double litros
            if (surtir(s, pos, litros) && vD == "stopInicio") {

                verificador = "surtirInicio";
                Utilitarios.setDefaultsPreference("verificador", verificador, getActivity());
                progressBar.setProgress(100);
                lblDespacho.setText("100%");

                esperarEnviarActivarDespacho(5000, pos);

            }
            String vX = Utilitarios.getDefaultsPreference("verificador", getActivity());
            if (xperm(s, pos) && vX == "surtirInicio") {
                lblDespacho.setText("Surtiendo...");
                verificador = "xpermInicio";
                Utilitarios.setDefaultsPreference("verificador", verificador, getActivity());
                esperarEnviarShow(2);

            }
            if (show12(s)) {
                verificador = "showInicio";
                String verificador2 = "showInicio2";
                if (manguera == 1) {
                    Utilitarios.setDefaultsPreference("verificador", verificador, getActivity());
                }

            }

        } catch (Exception ex) {
        }
    }

    public void despacho2(byte[] data, String pos, Double litros) {
        String s = new String(data, StandardCharsets.UTF_8);

        String verificador = "Despachando";
        try {
            //Verificador despacho vD
            String vD = Utilitarios.getDefaultsPreference("verificador2", getActivity());

            if (hide2(s) && vD == "Despachando2") {
                System.out.println("DESPAC");
                progressBar.setProgress(15);
                lblDespacho.setText("15%");
                verificador = "hideInicio";
                Utilitarios.setDefaultsPreference("verificador2", verificador, getActivity());
                esperarEnviarNPerm(5000, pos);
                progressBar.setProgress(35);
                lblDespacho.setText("35%");
            }
            String vN = Utilitarios.getDefaultsPreference("verificador2", getActivity());
            if (nperm(pos, s) && vN == "hideInicio") {
                progressBar.setProgress(55);
                lblDespacho.setText("55%");
                verificador = "npermInicio";
                Utilitarios.setDefaultsPreference("verificador2", verificador, getActivity());
                esperarEnviarStop(5000, pos);
            }
            String vS = Utilitarios.getDefaultsPreference("verificador2", getActivity());
            if (stop(pos, s) && vS == "npermInicio") {

                progressBar.setProgress(75);
                lblDespacho.setText("75%");
                verificador = "stopInicio";
                Utilitarios.setDefaultsPreference("verificador2", verificador, getActivity());
                esperarEnviarSurtir(5000, pos, litros);

            }
            //String respuesta,String pos,Double litros
            if (surtir(s, pos, litros) && vD == "stopInicio") {

                verificador = "surtirInicio";
                Utilitarios.setDefaultsPreference("verificador2", verificador, getActivity());
                progressBar.setProgress(100);
                lblDespacho.setText("100%");

                esperarEnviarActivarDespacho(5000, pos);

            }
            String vX = Utilitarios.getDefaultsPreference("verificador2", getActivity());
            if (xperm(s, pos) && vX == "surtirInicio") {
                lblDespacho.setText("Surtiendo...");
                verificador = "xpermInicio";
                Utilitarios.setDefaultsPreference("verificador2", verificador, getActivity());
                esperarEnviarShow(2);

            }
            if (show12(s)) {
                verificador = "showInicio";
                Utilitarios.setDefaultsPreference("verificador2", verificador, getActivity());
            }

        } catch (Exception ex) {
        }
    }

    boolean hide2(String s) {

        Boolean hideok = false;
        String aguja2 = "02HIDE OK\n";
        //String aguj = "01HIDE OK";

        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja2) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(s);
        String aguja1 = "02HIDE OK";
        //texto ^^^
        Pattern regex1 = Pattern.compile("\\b" + Pattern.quote(aguja1) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match1 = regex1.matcher(s);
        if (match.find() || match1.find()) {
            hideok = true;

        } else {
            hideok = false;
        }
        return hideok;
    }

    /**
     * HIDE POSICION 1,2
     */
    boolean hide1(String s) {

        Boolean hideok = false;
        String aguja2 = "01HIDE OK";
        //String aguj = "01HIDE OK";

        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja2) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(s);
        String aguja1 = "02HIDE OK";
        //texto ^^^
        Pattern regex1 = Pattern.compile("\\b" + Pattern.quote(aguja1) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match1 = regex1.matcher(s);
        if (match.find()) {
            hideok = true;

        } else {
            hideok = false;
        }
        return hideok;
    }

    boolean hide12(String s) {

        Boolean hideok = false;
        String aguja2 = "01HIDE OK\n\r";
        //String aguj = "01HIDE OK";

        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja2) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(s);
        String aguja1 = "02HIDE OK";
        //texto ^^^
        Pattern regex1 = Pattern.compile("\\b" + Pattern.quote(aguja1) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match1 = regex1.matcher(s);
        if (match.find() && match1.find()) {
            hideok = true;

        } else {
            hideok = false;
        }
        return hideok;
    }

    /**
     * NPERM POSICION A USAR
     */
    boolean nperm(String pos, String respuesta) {
        Boolean hideok = false;
        String aguja = pos + "NPERM OK";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            hideok = true;
        } else {
            hideok = false;
        }
        return hideok;
    }

    /**
     * STOP POSICION A USAR
     */
    boolean stop(String pos, String respuesta) {
        Boolean stopok = false;
        String aguja = pos + "STOP OK";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            stopok = true;
        } else {
            stopok = false;
        }
        return stopok;
    }

    /**
     * SULTS POSICION A USAR
     */
    boolean surtir(String respuesta, String pos, Double litros) {
        Boolean surtirOk = false;
        String busqueda = pos + "SULTS OK" + pos + "Litros: " + litros;

        Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            surtirOk = true;
        } else {
            surtirOk = false;
        }
        return surtirOk;
    }

    /**
     * XPERM POR POSICION A USAR
     */
    boolean xperm(String respuesta, String pos) {
        Boolean xpermok = false;
        String busqueda = pos + "XPERM OK";

        Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            xpermok = true;
        } else {
            xpermok = false;
        }
        return xpermok;
    }

    /**
     * 01SHOW 02 SHOW
     */
    boolean show2(String s) {

        Boolean showok = false;
        String aguja2 = "02SHOW OK\n\r";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja2) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(s);
        if (match.find()) {
            showok = true;

        } else {
            showok = false;
        }

        return showok;
    }

    /**
     * 01SHOW 02 SHOW
     */
    boolean show12(String s) {

        Boolean showok = false;
        String aguja2 = "01SHOW OK\n\r";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(aguja2) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(s);
        String aguja1 = "02SHOW OK";
        Pattern regex1 = Pattern.compile("\\b" + Pattern.quote(aguja1) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match1 = regex1.matcher(s);
        if (match.find() && match1.find()) {
            showok = true;

        } else {
            showok = false;
        }

        return showok;
    }

    /**
     * METODO PARA OBTENER LOS LITROS
     */
    boolean glits(byte[] data, String pos) {
        String respuesta = new String(data, StandardCharsets.UTF_8);
        Boolean xpermok = false;
        String busqueda = pos + "GLITS";
        String patron = "\\d+\\.\\d+";
        Pattern regex = Pattern.compile("\\b" + Pattern.quote(busqueda) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher match = regex.matcher(respuesta);
        if (match.find()) {
            Pattern pattern = Pattern.compile(patron);
            Matcher matcher = pattern.matcher(respuesta);

            while (matcher.find()) {
                //System.out.println(matcher.group());
                if (pos == "01") {
                    txtLitrosBomba1.setText(matcher.group());
                } else {
                    txtLitrosBomba2.setText(matcher.group());
                }

            }

            xpermok = true;
        } else {
            xpermok = false;
        }


        return xpermok;
    }

    /**
     * Método para enviar comando SHOW al servicio Bluettoth y recibe como parametro
     *
     * @param milisegundos
     */
    public void esperarEnviarShow(int milisegundos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                String enviar = "01SHOW\\N 02SHOW";
                send(enviar);
            }
        }, milisegundos);
    }

    /**
     * Método para enviar comando XPERM al servicio Bluettoth y recibe como parametro
     *
     * @param milisegundos
     */
    public void esperarEnviarActivarDespacho(int milisegundos, String pos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                String comandoSurtir = pos + "XPERM\n";
                send(comandoSurtir);
            }
        }, milisegundos);
    }

    /**
     * Método para enviar comando SULTS al servicio Bluettoth y recibe como parametro
     *
     * @param milisegundos
     */
    public void esperarEnviarSurtir(int milisegundos, String pos, Double litros) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                String comandoSurtir = pos + "SULTS " + litros + "\n";
                send(comandoSurtir);
            }
        }, milisegundos);
    }

    /**
     * Método para enviar comando NPERM al servicio Bluettoth y recibe como parametro
     *
     * @param milisegundos
     */
    public void esperarEnviarNPerm(int milisegundos, String pos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                send(pos + "NPERM\n");
            }
        }, milisegundos);
    }

    /**
     * Método para enviar comando STOP al servicio Bluettoth y recibe como parametro
     *
     * @param milisegundos
     */
    public void esperarEnviarStop(int milisegundos, String pos) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                send(pos + "STOP\n");
            }
        }, milisegundos);
    }

    private void insertarDespacho() {

        PreparedStatement prep;

        try {
            prep = miDB.CONN(getActivity()).prepareStatement(
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

            prep.setString(1, papeletaDespacho);
            prep.setString(2, unidadDespacho);
            prep.setString(3, "1");
            prep.setInt(4, mangueraDespacho);
            prep.setDouble(5, litrosPapDespacho);
            prep.setDouble(6, litrosBombaDespacho);
            prep.setString(7, "1");
            prep.setString(8, "1");
            prep.setString(9, obtenerFechaDespacho());
            prep.setString(10, obtenerFechaDespacho());
            prep.setString(11, Utilitarios.getDefaultsPreference("usuarioapp", getActivity()));
            prep.setDouble(12, 0.0);
            prep.setDouble(13, 0.0);
            prep.setString(14, "0.0");
            prep.setString(15, "0");
            prep.setString(16, Utilitarios.getDefaultsPreference("empresaajustes", getActivity()));
            prep.setString(17, "0");
            prep.setString(18, "0");
            prep.setString(19, obtenerFechaDespacho());
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
            prep.setString(30, "FK " + obtenerFechaDatosDiesel());
            prep.setString(31, "1");
            prep.setString(32, "1");
            prep.setString(33, "1");
            prep.setString(34, "1");
            prep.setString(35, "1");
            prep.setString(36, "1");
            prep.setString(37, "1");
            prep.setString(38, "1");
            prep.setString(39, folioDespacho);
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
                Toast.makeText(getActivity(), "Registro exitoso.", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException throwables) {
            Toast.makeText(getActivity(), "Error " + throwables, Toast.LENGTH_LONG).show();
            throwables.printStackTrace();
        }
    }

    public void consultarValesPendientes(String Unidad) {

        if (valCon.validarConectividad(getActivity()) == false) {
            mostrarToast();
        } else if (valCon.validarConectividad(getActivity()) == true) {
            try {
                miDB.CONN(getActivity());
                String obtenerVales = "SELECT PapeletaId, AutobusId, PapLitros, PapFemision from DATOSDIESEL where\n" +
                        " PapFemision BETWEEN '" + obtenerFechaDatosDiesel() + "' AND '" +
                        obtenerFechaDatosDiesel() + " 23:59:59' and paso=0 and AutobusId='" + Unidad + "' order by PapFemision desc;";
                System.out.println(obtenerVales);
                if (valCon.validarConectividad(getActivity()) == false) {
                    mostrarToast();
                } else if (valCon.validarConectividad(getActivity()) == true) {
                    try {
                        Statement comm = miDB.CONN(getActivity()).createStatement();
                        ResultSet rs = comm.executeQuery(obtenerVales);
                        arregloValesPendientes = new ArrayList<>();
                        try {
                            while (rs.next()) {
                                arregloValesPendientes.add(new HeaderAdaptadorVales(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                            }
                            if (String.valueOf(arregloValesPendientes.size()).isEmpty()) {
                                Toast.makeText(this.getActivity(), "No se encontraron registros", Toast.LENGTH_LONG).show();
                            } else {
                                headerAdaptadorValesPendientes = new HeaderAdaptadorValesPendientes(arregloValesPendientes);
                                rcvListaValesPendientes.setAdapter(headerAdaptadorValesPendientes);
                            }

                        } catch (SQLException ec) {

                        }
                    } catch (SQLException ex) {

                    }
                }
            } catch (Exception t) {
            }
        }
    }

    private void updateDatosDiesel() {
        obtenerDatosARegistrar();
        try {
            String updateDatosDiesel = "UPDATE DATOSDIESEL SET PASO = ? where AutobusId = ? and PapeletaId= ?";
            PreparedStatement preparedStmt = miDB.CONN(getActivity()).
                    prepareStatement(updateDatosDiesel);
            preparedStmt.setInt(1, 1);
            preparedStmt.setString(2, unidadDespacho);
            preparedStmt.setString(3, papeletaDespacho);
            preparedStmt.executeUpdate();
            int update = preparedStmt.executeUpdate();
            if (update > 0) {
                if (manguera == 1) {
                    txtPapD1.setBackground(getResources().getDrawable(R.drawable.stylo_borde_listview_activo));

                }
                if (manguera2 == 2) {
                    txtPapD2.setBackground(getResources().getDrawable(R.drawable.stylo_borde_listview_activo));
                }

                /*Toast.makeText(getActivity(), "Estado de vale actualizado.",
                        Toast.LENGTH_LONG).show();*/
            }
            miDB.CONN(getActivity()).close();
        } catch (SQLException throwables) {
            Toast.makeText(getActivity(), "Error " + throwables, Toast.LENGTH_LONG).show();
        }
    }

    public String obtenerFechaDespacho() {
        //2021-11-24 00:00:00.000
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        String formato = Utilitarios.getDefaultsPreference("formatodespacho", getActivity());
        SimpleDateFormat sdfDate = new SimpleDateFormat(formato);
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public String obtenerFechaDatosDiesel() {
        //2021-11-24 00:00:00.000
        char buf[] = new char[10];
        String formato = Utilitarios.getDefaultsPreference("formatoconsultas", getActivity());
        formato.getChars(1, 10, buf, 0);
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        SimpleDateFormat sdfDate = new SimpleDateFormat(String.valueOf(buf).trim());
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void mostrarToast() {
        LayoutInflater inflater = getLayoutInflater();
        //setContentView(R.layout.activity_main);
        //View view = inflater.inflate(R.layout.cust_toast_layout, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(getActivity());
        toast.setView(view1);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public void mostrarErrorServer() {
        Toast toast = new Toast(getActivity());
        toast.setView(view2);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    void obtenerDatosARegistrar() {
        if (manguera == 1) {
            //text.trim()
            unidadDespacho = txtUnidadP1.getText().toString().trim();
            papeletaDespacho = txtPapD1.getText().toString().trim();
            litrosPapDespacho = Double.parseDouble(txtLitrosPap1.getText().toString());
            litrosBombaDespacho = Double.parseDouble(txtLitrosBomba1.getText().toString());
            mangueraDespacho = 1;
            folioDespacho = unidadDespacho + papeletaDespacho +
                    obtenerFechaDespacho().trim();

        } else {
            unidadDespacho = txtUnidadP2.getText().toString().trim();
            papeletaDespacho = txtPapD2.getText().toString().trim();
            litrosPapDespacho = Double.parseDouble(txtLitrosPap2.getText().toString());
            litrosBombaDespacho = Double.parseDouble(txtLitrosBomba2.getText().toString());
            mangueraDespacho = 2;
            folioDespacho = unidadDespacho + papeletaDespacho + obtenerFechaDespacho().trim();
        }
    }

    /*  public void mostrarIngresarUnidad() {
          AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);

          alert.setTitle("Fuel Kontrol");
          alert.setIcon(R.drawable.bg_fuel_kontrol);
          alert.setMessage("Ingresa unidad\uD83D\uDE9B.");
          EditText input = new EditText(getActivity());
          input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
          input.setWidth(100);
          input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
          input.setSingleLine();
          input.setHintTextColor(getResources().getColor(R.color.purple_700));
          input.setHint("Ingresa unidad");
          input.setTextSize(14);
          input.setTextColor(getResources().getColor(R.color.purple_700));
          input.setBackgroundResource(R.drawable.stylo_borde_editext);
          input.setFocusable(true);
          input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
          alert.setView(input);

          alert.setPositiveButton("BUSCAR\uD83D\uDD0D", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                  unidadIngresada = input.getText().toString();
                  if (valCon.validarConectividad(getActivity()) == false) {
                      mostrarToast();
                  } else if (valCon.validarConectividad(getActivity()) == true) {
                      try {
                          consultarValesPendientes(unidadIngresada);
                          InputMethodManager imm = (InputMethodManager) service.getSystemService(Context.INPUT_METHOD_SERVICE);
                          imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                      } catch (Exception t) {
                          mostrarErrorServer();
                      }
                  }
                  return;
              }
          });
          alert.setNegativeButton("    ✘     ", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {

              }
          });
          alert.show();

      }*/
    public void mostrar() {
        customDialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //customDialog.setCancelable(false);
        customDialog.setContentView(R.layout.dialog);
        TextView titulo = customDialog.findViewById(R.id.titulo);
        titulo.setText("Fuel Kontrol");
        EditText input = customDialog.findViewById(R.id.input);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setSingleLine();
        input.setHintTextColor(getResources().getColor(R.color.purple_700));
        input.setHint("Unidad");
        input.setTextSize(14);
        input.setTextColor(getResources().getColor(R.color.purple_700));
        input.setBackgroundResource(R.drawable.stylo_borde_editext);
        input.setFocusable(true);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        //input.setText("Mensaje con el contenido del dialog");
        TextView showMensaje = (TextView) customDialog.findViewById(R.id.mensaje);
        showMensaje.setText("Ingresa unidad\uD83D\uDE9B.");
        ((Button) customDialog.findViewById(R.id.aceptar)).setOnClickListener(view3 -> {
            customDialog.dismiss();
            unidadIngresada = input.getText().toString();
            if (valCon.validarConectividad(getActivity()) == false) {
                mostrarToast();
            } else if (valCon.validarConectividad(getActivity()) == true) {
                try {
                    InputMethodManager imm = (InputMethodManager) service.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    consultarValesPendientes(unidadIngresada);

                } catch (Exception t) {
                    mostrarErrorServer();
                }
            }
        });
        customDialog.findViewById(R.id.cancelar).setOnClickListener(view32 -> customDialog.dismiss());
        customDialog.show();
    }

    void cargarDatosPosicion(int posicion) {
        if (db != null && !textoRecibido.getText().toString().equals("Conectado")) {
            manguera = posicion;
            obtenerDatosARegistrar();
            mangueraDespacho = posicion;
            cargarDatosSql();
        }
    }

    public void cargarDatosSql() {
        try {
            dbHelper = new DBHelper(getActivity(), "FUELKONTROLADMIN", null, 1);
            SQLiteDatabase conn = dbHelper.getWritableDatabase();
            ContentValues v = new ContentValues();
            v.put(Utilidades.KEY_Papeleta, papeleta);
            v.put(Utilidades.KEY_Unidad, unidad);
            v.put(Utilidades.KEY_Manguera, mangueraDespacho);
            v.put(Utilidades.KEY_LitrosPapeleta, litrosPapDespacho);
            v.put(Utilidades.KEY_LitrosSurtidos, litrosBombaDespacho);
            v.put(Utilidades.KEY_FechaDespacho, obtenerFechaDespacho());
            v.put(Utilidades.KEY_UsuarioApp, Utilitarios.getDefaultsPreference("usuarioapp", getActivity()));
            v.put(Utilidades.KEY_Empresa, Utilitarios.getDefaultsPreference("empresaajustes", getActivity()));
            v.put(Utilidades.KEY_Referencia, "FK " + obtenerFechaDatosDiesel());
            v.put(Utilidades.KEY_FolioDespacho, folioDespacho);
            v.put(Utilidades.KEY_Estado, 0);
            conn.insert(Utilidades.TABLE_DESPACHO, null, v);
            System.out.println(Utilidades.CREATE_TABLE_DESPACHO);
        } catch (SQLiteAbortException ex) {
            //System.out.println("ERROR "+ex);
        }
    }
}
