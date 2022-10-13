package com.example.fuelkontrol;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.fuelkontrol.activity.LoginAjustes;
import com.example.fuelkontrol.databinding.ActivityFullscreenBinding;
import com.example.fuelkontrol.helper.DBHelper;
import com.example.fuelkontrol.helper.ManejadorDB;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.helper.ValidarConectividad;
import com.example.fuelkontrol.util.Utilidades;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executor;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Login extends AppCompatActivity {
    DBHelper dbHelper;
    Button btnIniciar;
    EditText edtContra, edtUsuario;
    CheckBox cmbRecordar;
    ImageButton mostrarContra, btnLeer;
    SwitchCompat swTipoAcceso;
    ManejadorDB miDB;
    ValidarConectividad valCon;
    Context thisActivity;
    private boolean esVisible;
    private String temporal;
    String shrPRecordarCredenciales = "CredencialesUsuario";
    String shrPRBiometrico = "Biometrico";
    String shrEstadoServidor;
    String shrBaseDatos;
    String shrEstadoUsuario;
    String shrEstadoContraseña;
    private int tipoUsuario;
    String validador = new String();
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        //Accion del boton
                        miDB = new ManejadorDB();
                        if (valCon.validarConectividad(getApplicationContext()) == false) {
                            mostrarToast();
                        } else if (valCon.validarConectividad(getApplicationContext()) == true) {
                            try {
                                validacionRegistrado(edtUsuario.getText().toString(), edtContra.getText().toString());
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                                mostrarErrorServer();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullscreenBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        thisActivity = Login.this;
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        validador = "USUARIORECORDADO";
        executor = ContextCompat.getMainExecutor(this);
        btnIniciar = (Button) findViewById(R.id.btnIniciar);
        edtContra = (EditText) findViewById(R.id.edtContra);
        edtUsuario = (EditText) findViewById(R.id.edtUsuario);
        cmbRecordar = (CheckBox) findViewById(R.id.cmbRecordar);
        btnLeer = (ImageButton) findViewById(R.id.btnLeer);
        swTipoAcceso = (SwitchCompat) findViewById(R.id.swOnOff);
        //Utilitarios.setDefaultsPreference("usuario","",this);
        shrPRecordarCredenciales = (Utilitarios.getDefaultsPreference("usuario", getApplicationContext()));
        shrPRBiometrico = (Utilitarios.getDefaultsPreference("huella", getApplicationContext()));
        shrEstadoServidor = (Utilitarios.getDefaultsPreference("servidor", getApplicationContext()));
        shrBaseDatos = (Utilitarios.getDefaultsPreference("basededatos", getApplicationContext()));
        shrEstadoUsuario = (Utilitarios.getDefaultsPreference("usuariobase", getApplicationContext()));
        shrEstadoContraseña = (Utilitarios.getDefaultsPreference("contraseñabase", getApplicationContext()));
        dbHelper = new DBHelper(this, "FUELKONTROLADMIN", null, 1);

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        biometricPrompt = new BiometricPrompt(Login.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Toast.makeText(LoginActivity.this,"Error"+errString,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                abrirInicio();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Acceso Fuel Kontrol")
                        .setSubtitle("Autenticación biometrica")
                        .setNegativeButtonText("Cancelar")
                        .build();
        if (db != null) {
            cargarDatos();
        }
        //Toast.makeText(this, shrPRecordarCredenciales, Toast.LENGTH_LONG).show();
        if (shrPRecordarCredenciales == "USUARIORECORDADO") {
            swTipoAcceso.setChecked(true);
            Intent intent = new Intent(this, MenuInicio_Activity.class);
            startActivity(intent);
            this.finish();

        } else if
        (shrEstadoServidor == null || shrBaseDatos == null || shrEstadoUsuario == null ||
                        shrEstadoContraseña == null) {
            Intent intent = new Intent(this, LoginAjustes.class);
            startActivity(intent);
            this.finish();
        }
        if (Utilitarios.getDefaultsPreference("huella", this) == "HUELLA"
                && Utilitarios.getDefaultsPreference("biometricovalido", this) == "DATOSCOMPLETOS") {
            swTipoAcceso.setChecked(true);
            biometricPrompt.authenticate(promptInfo);
        }

        valCon = new ValidarConectividad();
        miDB = new ManejadorDB();
        mVisible = true;
        mostrarContra = (ImageButton) findViewById(R.id.showPassword);
        mostrarContra.setBackgroundResource(R.drawable.hide_password);
        btnLeer.setBackgroundResource(R.drawable.huella_digital);

        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.btnIniciar.setOnTouchListener(mDelayHideTouchListener);
        swTipoAcceso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (swTipoAcceso.isChecked()) {
                    //Primera vez
                    if (Utilitarios.getDefaultsPreference("biometricovalido", Login.this) == "DATOSCOMPLETOS") {
                        biometricPrompt.authenticate(promptInfo);
                        Utilitarios.setDefaultsPreference("huella", "HUELLA",
                                Login.this);
                    } else {
                        Utilitarios.setDefaultsPreference("biometricovalido", "DATOSINCOMPLETOS",
                                Login.this);
                        android.app.AlertDialog.Builder alert = new android.app.
                                AlertDialog.Builder(thisActivity);
                        alert.setTitle("Fuel Kontrol");
                        alert.setIcon(R.drawable.bg_fuel_kontrol);
                        alert.setMessage("Configuración inicial.");
                        TextView input = new TextView(thisActivity);
                        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        input.setText("Para recordar el acceso con huella digital a Fuel Kontrol, es necesario" +
                                " ingresar usuario y contraseña por primera vez.");
                        input.setTextSize(18);
                        input.setTextColor(getResources().getColor(R.color.color_emitiendo_error));
                        alert.setView(input);
                        alert.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                return;
                            }
                        });
                        alert.show();
                    }
                } else {
                    android.app.AlertDialog.Builder alert = new android.app.
                            AlertDialog.Builder(thisActivity);
                    alert.setTitle("Fuel Kontrol");
                    alert.setIcon(R.drawable.bg_fuel_kontrol);
                    alert.setMessage("¿Está seguro que desea inhabilitar el acceso con huella?");
                    TextView input = new TextView(thisActivity);
                    input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    input.setText("Recuerde que al desactivar el interruptor de acceso con huella, deberá ingresar nuevamente su contraseña para ingresar a Fuel Kontrol.");
                    input.setTextSize(18);
                    input.setTextColor(getResources().getColor(R.color.color_emitiendo_error));
                    alert.setView(input);
                    alert.setPositiveButton("CERRAR SESION\uD83D\uDD13", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utilitarios.setDefaultsPreference("usuario", "USUARIONORECORDADO",
                                    Login.this);
                            Utilitarios.setDefaultsPreference("biometricovalido", "DATOSINCOMPLETOS",
                                    Login.this);
                            Utilitarios.setDefaultsPreference("huella", "NOHUELLA",
                                    Login.this);
                            Utilitarios.setDefaultsPreference("verificador", "", Login.this);
                            Utilitarios.setDefaultsPreference("verificador1", "", Login.this);
                            return;
                        }
                    });
                    alert.setNegativeButton("      ✘       ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alert.show();


                }
            }
        });
        edtContra.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0 || !s.toString().equals("")) {
                    show();
                    getSupportActionBar().hide();
                } else {
                    hide();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mostrarContra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!esVisible) {
                    edtContra.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    esVisible = true;
                } else {
                    edtContra.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    esVisible = false;
                    mostrarContra.setBackgroundResource(R.drawable.hide_password);
                }
            }
        });
        edtContra.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    miDB = new ManejadorDB();
                    if (valCon.validarConectividad(getApplicationContext()) == false) {
                        mostrarToast();
                    } else if (valCon.validarConectividad(getApplicationContext()) == true) {
                        try {
                            validacionRegistrado(edtUsuario.getText().toString(), edtContra.
                                    getText().toString());
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                            mostrarErrorServer();
                        }
                    }
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //guardar_estado_switch();
        //guardar_estado_boton();
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            //show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= 30) {
            //Para mostrar actionBar y barra de estado
            /*  mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            mContentView.getWindowInsetsController().show(WindowInsets.Type.navigationBars());*/
        } else {
            //Para mostrar actionBar y barra de estado
            /* mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);*/
        }
        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);

    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void validacionRegistrado(String nombreUsuario, String password) throws SQLException {
        temporal = null;
        ResultSet set = null;
        if (valCon.validarConectividad(getApplicationContext()) == false) {
            mostrarToast();
        } else if (valCon.validarConectividad(getApplicationContext()) == true) {

            try {
                if (miDB != null) {
                    try {
                        miDB.CONN(this);
                        String sql = "SELECT clave_usuario, contrasena, tipo_acceso from USUARIOS " +
                                "where CLAVE_USUARIO='" + nombreUsuario + "' and CONTRASENA='" +
                                password + "';";
                        Statement smt = miDB.CONN(this).createStatement();
                        set = smt.executeQuery(sql);
                        Toast.makeText(this, "Accediendo... ", Toast.LENGTH_SHORT).
                                show();
                    } catch (Exception ex) {
                        // mostrarErrorServer();
                    }
                    if (set.next()) {
                        if (shrPRecordarCredenciales.equals("USUARIORECORDADO")) {
                        }
                        Utilitarios.setDefaultsPreference("biometricovalido",
                                "DATOSCOMPLETOS", thisActivity);
                        if (swTipoAcceso.isChecked() && Utilitarios.getDefaultsPreference(
                                "biometricos", thisActivity) == "DATOSINCOMPLETOS") {
                            Utilitarios.setDefaultsPreference("huella", "HUELLA",
                                    Login.this);
                        }
                        tipoUsuario = set.getInt("tipo_acceso");
                        String tipoEnviar = Integer.toString(tipoUsuario).trim();
                        Utilitarios.setDefaultsPreference("tipousuarioapp",
                                tipoEnviar.trim(), thisActivity);

                        Utilitarios.setDefaultsPreference("usuarioapp", edtUsuario.getText().
                                toString(), thisActivity);
                        guardar_estado_boton();
                        abrirInicio();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
                        builder.setTitle("Error");
                        builder.setMessage("Verifique su información de acceso.");
                        builder.setPositiveButton("Aceptar", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            } catch (Exception ex) {
                mostrarErrorServer();
            }
        }
    }

    public void abrirInicio() {
        splash();
        Intent abrirInicio = new Intent(getApplicationContext(), MenuInicio_Activity.class);
        startActivity(abrirInicio);
        this.finish();
    }

    public void guardar_estado_switch() {
        if (swTipoAcceso.isChecked()) {
            Utilitarios.setDefaultsPreference("huella", "HUELLA",
                    Login.this);
            swTipoAcceso.setChecked(true);
        } else {
            Utilitarios.setDefaultsPreference("huella", "NOHUELLA",
                    Login.this);
            swTipoAcceso.setChecked(false);
        }
    }

    public void guardar_estado_boton() {
        if (cmbRecordar.isChecked()) {
            Utilitarios.setDefaultsPreference("usuario", "USUARIORECORDADO",
                    Login.this);
        } else {
            Utilitarios.setDefaultsPreference("usuario", "USUARIONORECORDADO",
                    Login.this);
        }
    }

    public void cargarDatos() {
        dbHelper = new DBHelper(this, "FUELKONTROLADMIN", null, 1);
        SQLiteDatabase conn = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(Utilidades.KEY_id, "1");
        v.put(Utilidades.KEY_ClaveUsuario, "root");
        v.put(Utilidades.KEY_ContraUsuario, "root");
        conn.insert(Utilidades.TABLE, Utilidades.KEY_id, v);
    }

    public void mostrarToast() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_toast_layout, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(Login.this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public void splash() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.splash, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(Login.this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public void mostrarErrorServer() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.error_de_acceso, (ViewGroup) findViewById(R.id.relativeLayout1));
        Toast toast = new Toast(Login.this);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

}