package com.example.fuelkontrol.helper;

import android.content.Context;
import android.net.ConnectivityManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ManejadorDB extends Utilitarios {
    //Variable que almacena la Direccion ip.
    String ip = null;
    //Variable que almacena el Controlador JTDS.
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    //Variable que almacena el Nombre de base de datos.
    String db = "0";
    //Variable que almacena el Nombre de usuario.
    String us = "0";
    //Variable que almacena la contraseña de la base de datos.
    String password = "0";
    public Connection CONN(Context context) {
                ip = (Utilitarios.getDefaultsPreference("servidor",context));
        db = (Utilitarios.getDefaultsPreference("basededatos",context));
        us = (Utilitarios.getDefaultsPreference("usuariobase",context));
        password = (Utilitarios.getDefaultsPreference("contraseñabase",context));
        Connection conn = null;
        String ConnURL;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + us + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException | ClassNotFoundException se) {
        }
        return conn;
    }
    public Connection test(String s,String bd,String u,String p) {

        Connection testt = null;
        String ConnURL;
        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + s + ";"
                    + "databaseName=" + bd + ";user=" + u + ";password="
                    + p + ";";
            testt = DriverManager.getConnection(ConnURL);
        } catch (SQLException | ClassNotFoundException se) {
        }
        return testt;
    }

}

