package com.example.fuelkontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelkontrol.adaptadores.HeaderAdaptadorVales;
import com.example.fuelkontrol.adaptadores.HeaderAdaptadorValesPendientes;
import com.example.fuelkontrol.evento.UiToastEvent;
import com.example.fuelkontrol.helper.EnhancedSharedPreferences;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.NotificationHelper;
import com.example.fuelkontrol.helper.ValidarConectividad;
import com.example.fuelkontrol.service.MyBluetoothSerialService;
import com.example.fuelkontrol.util.Config;
import com.example.fuelkontrol.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class PanelActivity extends AppCompatActivity {

    private PanelActivity.MyServiceMessageHandler myServiceMessageHandler;
    protected MyBluetoothSerialService myBluetoothSerialService = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean mBoundService = false;
    private String mConnectedDeviceName = null;
    private String mStop = null;
    private EnhancedSharedPreferences sharedPref;
    private EditText edtEnviar;
    private TextView lblRecibido;


    private ImageView imgConnect;
    private Button btnEnviar;

    HeaderAdaptadorValesPendientes headerAdaptadorValesPendientes;
    ManejadorDB miDB;
    ValidarConectividad valCon;
    TextView edtUnidadB1, edtPapeletaB1, edtUnidadB2, edtPapeletaB2;

    String unidadIngresada;
    RecyclerView rcvListaValesPendientes;
    Button btnSeleccionarPapeleta;
    String unidad, papeleta, litros;

    //Array List para almacenar la consulta
    ArrayList<HeaderAdaptadorVales> arregloValesPendientes;
    final static String STOP = "01STOP";
    final static String HIDE = "01HIDE\n";
    final static String SULTS = "01SULTS 10\n";
    final static String XPERM = "01XPERM\n";
    final static String NPERM = "01NPERM\n";
    final static String GLITS = "01GLITS\n";
    String sultsRecibidos;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        Bundle unidadADespachar = getIntent().getExtras();
        if (unidadADespachar != null) {
            unidad = unidadADespachar.getString("unidadEnviada");
            papeleta = unidadADespachar.getString("papeletaEnviada");
            litros = unidadADespachar.getString("litrosEnviada");
        }
        edtUnidadB1 = findViewById(R.id.UnidadB1);
        edtPapeletaB1 = findViewById(R.id.PapeletasB1);
        edtUnidadB2 = findViewById(R.id.UnidadB2);
        edtPapeletaB2 = findViewById(R.id.PapeletasB2);
        lblRecibido = findViewById(R.id.lblRecibo);

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
                AlertDialog.Builder alert = new AlertDialog.Builder(PanelActivity.this);
                alert.setTitle("Fuel Kontrol");
                alert.setIcon(R.drawable.bg_fuel_kontrol);
                alert.setMessage("Ingresa unidad\uD83D\uDE9B.");
                EditText input = new EditText(PanelActivity.this);
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

                        if (valCon.validarConectividad(getApplicationContext()) == false) {
                            mostrarToast();
                        } else if (valCon.validarConectividad(getApplicationContext()) == true) {
                            try {

                                consultarValesPendientes(unidadIngresada);
                            } catch (Exception t) {

                                mostrarErrorServer();
                            }
                        }


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
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.createChannels();

        // check support

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Config.Mensaje(this, getString(R.string.text_no_bluetooth_adapter), false, false);
        } else {
            Intent intent = new Intent(getApplicationContext(), MyBluetoothSerialService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        /**
         * Creamos el hilo para responder a los siguientes estados
         * MESSAGE_STATE_CHANGE
         * MESSAGE_DEVICE_NAME
         * MESSAGE_TOAST
         */
        myServiceMessageHandler = new PanelActivity.MyServiceMessageHandler(this, this);
        inicializarControles();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        sharedPref = EnhancedSharedPreferences.getInstance(getApplicationContext(), getString(R.string.shared_preference_key));
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            //Esto se llama cuando la conexión con el servicio ha sido
            // establecido, dándonos el objeto de servicio que podemos usar para
            // interactuar con el servicio
            MyBluetoothSerialService.MySerialServiceBinder binder = (MyBluetoothSerialService.MySerialServiceBinder) service;
            myBluetoothSerialService = binder.getService(); //Obtenermos la vinculacion del servicio
            mBoundService = true; //Variable para saber que el servicio esta conectado
            myBluetoothSerialService.setMessageHandler(myServiceMessageHandler); //Seteamos el hilo principal
            myBluetoothSerialService.setStatusUpdatePoolInterval(
                    Long.parseLong(sharedPref.getString(getString(
                            R.string.preference_update_pool_interval),
                            String.valueOf(Constants.STATUS_UPDATE_INTERVAL)))); // indicamos con cuanta frecuencia va a realiza actualizaciones el servicio
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // Esto se llama cuando la conexión con el servicio ha sido
            // desconectado inesperadamente, es decir, su proceso se bloqueó.
            mBoundService = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //Activamos el bluetooth por default mediante un hilo
        if (!bluetoothAdapter.isEnabled()) {
            Thread thread = new Thread() {
                @Override
                public void run() {

                    try {
                        bluetoothAdapter.enable(); //Activamos el BT de forma forzada

                    } catch (RuntimeException e) {
                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_no_bluetooth_permission), true, true));
                    }
                }
            };
            thread.start();
        }

        //Preguntamos por el estado actual del servicio
        if (myBluetoothSerialService != null)
            onBluetoothStateChange(myBluetoothSerialService.getState());
    }

    private static class MyServiceMessageHandler extends Handler {

        private final WeakReference<PanelActivity> mActivity;
        private final Context mContext;

        MyServiceMessageHandler(Context context, PanelActivity activity) {
            mContext = context;
            mActivity = new WeakReference<>(activity);
        }

        //Recibimos los datos enviados
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    mActivity.get().onBluetoothStateChange(msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mActivity.get().mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Config.Mensaje(mContext, mActivity.get().getString(R.string.text_connected_to) + " " + mActivity.get().mConnectedDeviceName, false, false);
                    break;
                case Constants.ESTADO_HIDE:
                    mActivity.get().mConnectedDeviceName = msg.getData().getString(Constants.HIDE);
                    Config.Mensaje(mContext, mActivity.get().getString(R.string.text_connected_to) + " " + mActivity.get().mConnectedDeviceName, false, false);
                    break;
            }
        }
    }


    private void onBluetoothStateChange(int currentState) {

        switch (currentState) {
            case MyBluetoothSerialService.STATE_CONNECTED:
                //Esta conectado
                break;
            case MyBluetoothSerialService.STATE_CONNECTING:
                //Esta conectandose
                break;
            case MyBluetoothSerialService.STATE_LISTEN:
                //Recibiendo datos
                break;
            case MyBluetoothSerialService.STATE_NONE:
                //Indicar que no esta conectado el bluetooth
                break;
        }

    }

    private void inicializarControles() {
        btnEnviar = findViewById(R.id.btnEnviar);
        edtEnviar = findViewById(R.id.edtEnviar);
        imgConnect = findViewById(R.id.img_connect);
        imgConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) {
                    if (myBluetoothSerialService != null) {
                        if (myBluetoothSerialService.getState() == myBluetoothSerialService.STATE_CONNECTED) {
                            new AlertDialog.Builder(PanelActivity.this)
                                    .setTitle(R.string.text_disconnect)
                                    .setMessage(getString(R.string.text_disconnect_confirm))
                                    .setPositiveButton(getString(R.string.text_yes_confirm), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (myBluetoothSerialService != null)
                                                myBluetoothSerialService.disconnectService();
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.text_cancel), null)
                                    .show();

                        } else {
                            Intent serverIntent = new Intent(PanelActivity.this, VincularDispositivo.class);
                            startActivityForResult(serverIntent, Constants.CONNECT_DEVICE_SECURE);
                        }
                    } else {
                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_bt_service_not_running), true, true));
                    }

                } else {
                    Config.Mensaje(PanelActivity.this, getString(R.string.text_bt_not_enabled), false, false);
                }
            }
        });
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothSerialService != null && myBluetoothSerialService.getState() ==
                        MyBluetoothSerialService.STATE_CONNECTED) {
                    // txtRecibido.setText(hidee);
                    myBluetoothSerialService.serialWriteString(edtEnviar.getText().toString());
                    String str = myBluetoothSerialService.valor();
                    lblRecibido.setText(str);

                    if (str != null) {
                        switch (str) {

                            case "01HIDE OK\n\r":
                                Toast.makeText(PanelActivity.this, "" + str, Toast.LENGTH_LONG).show();
                                myBluetoothSerialService.serialWriteString("01SULTS 10\n");
                                break;
                            case "01SULTS OK01Litros: 10--> 1000.0 pulsos!\n\r":
                                myBluetoothSerialService.serialWriteString("01XPERM");
                                break;
                            case "01XPERM OK\n\r":
                                myBluetoothSerialService.serialWriteString("01GLITS");
                                lblRecibido.setText(str);
                                break;
                            case "01NPERM OK\n\r":
                                myBluetoothSerialService.serialWriteString("01GLITS");
                                lblRecibido.setText(str);
                                break;
                            default:
                                System.out.println("no coincide" + str);
                        }
                    } else {
                        myBluetoothSerialService.serialWriteString("01HIDE\n");
                    }
                } else {
                    Config.Mensaje(PanelActivity.this, "Debe conectar su bluetooth", false, false);
                }

            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CONNECT_DEVICE_INSECURE:
            case Constants.CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {

                    mConnectedDeviceName = Objects.requireNonNull(data.getExtras()).getString(VincularDispositivo.EXTRA_DEVICE_ADDRESS);
                    //almacenamos share preferences
                    sharedPref.edit().putString(getString(R.string.preference_last_connected_device), mConnectedDeviceName).apply();
                    Log.e("MI_DATO", "" + mConnectedDeviceName);
                    connectToDevice(mConnectedDeviceName);
                }
        }

    }

    private void connectToDevice(String macAddress) {
        if (macAddress == null) {
            //Si el nombre es nulo entonces volvemos a mostrar la lista de dispositivos para que se vuelva a conectar
            Intent serverIntent = new Intent(getApplicationContext(), VincularDispositivo.class);
            startActivityForResult(serverIntent, Constants.CONNECT_DEVICE_SECURE);
        } else {
            ;
            Intent intent = new Intent(getApplicationContext(), MyBluetoothSerialService.class);
            intent.putExtra(MyBluetoothSerialService.KEY_MAC_ADDRESS, macAddress);

            //Verificamos que sea la version 26(Oreo) a superior esto se debe
            // a las limitaciones por consumo de bateria que realizo los desarrolladores de google
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                getApplicationContext().startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBoundService) {
            myBluetoothSerialService.setMessageHandler(null);
            unbindService(serviceConnection);
            mBoundService = false;
        }

        stopService(new Intent(this, MyBluetoothSerialService.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUiToastEvent(UiToastEvent event) {
        Config.Mensaje(PanelActivity.this, event.getMessage(), event.getLongToast(), event.getIsWarning());
    }

    ;//2022-04-02 10:24:00+
    String insertDespacho = "INSERT INTO DESPACHO VALUES ('123456', 'T555', '1', '1', 12345,12345.1," +
            "1,1, '2022-24-02 00:00:00','" + obtenerFechaDespacho() + "','ROOT',00.00, 0.0,'$', '3', 3, 2, " +
            "'1', '" + obtenerFechaDespacho() + "',00,00, 0, 0.0, 1,0.0,0.0,1, 0,0, 'UNIDADINSERTADA',1,1,1, 0.0, '','', '', '', '',null,null,null,null,null,null,null,null,0,null,null,null,null,null,null,null,null,null,null)";
    //AÑO DIA MES";

    //String updatePapeleta="UPDATE DATOSDIESEL SET Paso=1 WHERE autobusid='"+edtUnidadB1.getText()+"' AND PapeletaId ='"+edtPapeletaB2.getText()+"'";
    public void consultarValesPendientes(String Unidad) {

        if (valCon.validarConectividad(getApplicationContext()) == false) {
            mostrarToast();
        } else if (valCon.validarConectividad(getApplicationContext()) == true) {
            try {


                miDB.CONN(PanelActivity.this);
                String obtenerVales = "SELECT PapeletaId, AutobusId, PapLitros, PapFemision from DATOSDIESEL where\n" +
                        " PapFemision BETWEEN '" + fechaConsultaPap() + "' AND '" +
                        fechaConsultaPap() + " 23:59:59' and paso=0 and AutobusId='" + Unidad + "' order by PapFemision desc;";
                Toast.makeText(getApplicationContext(), obtenerVales, Toast.LENGTH_LONG).show();
                if (valCon.validarConectividad(getApplicationContext()) == false) {
                    mostrarToast();
                } else if (valCon.validarConectividad(getApplicationContext()) == true) {
                    try {

                        Statement comm = miDB.CONN(this).createStatement();
                        ResultSet rs = comm.executeQuery(obtenerVales);
                        arregloValesPendientes = new ArrayList<>();

                        try {

                            while (rs.next()) {
                                arregloValesPendientes.add(new HeaderAdaptadorVales(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                                //Toast.makeText(getApplicationContext(), rs.getString("PAPFEMISION"), Toast.LENGTH_LONG).show();
                            }
                            if (String.valueOf(arregloValesPendientes.size()).isEmpty()) {
                                Toast.makeText(PanelActivity.this, "No se encontraron registros", Toast.LENGTH_LONG).show();
                            } else {
                                headerAdaptadorValesPendientes = new HeaderAdaptadorValesPendientes(arregloValesPendientes);
                                rcvListaValesPendientes.setAdapter(headerAdaptadorValesPendientes);
                            }

                        } catch (SQLException ec) {
                            Toast.makeText(PanelActivity.this, "Error :" + ec.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (SQLException ex) {
                        Toast.makeText(PanelActivity.this, "Error :" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception t) {

                Toast.makeText(PanelActivity.this, "Error en la conexion" + t + "   -", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void insertarRegistro(String sql) {
         try {
             Statement comm = miDB.CONN(PanelActivity.this).createStatement();
             ResultSet rs = comm.executeQuery(sql);
             if (rs.next()) {
             } else {
                 Toast.makeText(this.getApplicationContext(), "Error al ingresar.", Toast.LENGTH_LONG).show();
             }
         } catch (SQLException ex) {

             Toast.makeText(getApplicationContext(), "Ingreso.", Toast.LENGTH_LONG).show();


         }
     }
    public String obtenerFechaDespacho() {
        //2021-11-24 00:00:00.000
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public String obtenerFecha() {
        //2021-11-24 00:00:00.000
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public String fechaConsultaPap() {
        //2021-11-24 00:00:00.000
        //SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");//dd/MM/yyyy
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void mostrarToast() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_toast_layout, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(PanelActivity.this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public void mostrarErrorServer() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.error_de_acceso, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(PanelActivity.this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
