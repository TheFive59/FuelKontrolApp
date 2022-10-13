package com.example.fuelkontrol.util;

/**
 * Clase que contiene las variables que se usan para la creacion de la tabla.
 *
 * @author Juan Manuel Miranda
 * @version 2021.1
 * @since 1.0
 */

public class Utilidades {
    final String DB_NAME = "FUELKONTROLADMIN.sqlite";
    //Nombre de la tabla
    public static final String TABLE = "USUARIO";
    public static final String TABLE_DESPACHO = "DESPACHO";
    //Nombre des las columnas de la tabla usuarios
    public static final String KEY_id = "id";
    public static final String KEY_ClaveUsuario = "Clave";
    public static final String KEY_ContraUsuario = "Contra";
    public static final String KEY_Papeleta = "Papeleta";
    public static final String KEY_Unidad = "Unidad";
    public static final String KEY_Manguera = "Manguera";
    public static final String KEY_LitrosPapeleta = "LitrosPapeleta";
    public static final String KEY_LitrosSurtidos = "LitrosSurtidos";
    public static final String KEY_FechaDespacho = "FechaDespacho";
    public static final String KEY_UsuarioApp = "UsuarioApp";
    public static final String KEY_Empresa = "Empresa";
    public static final String KEY_Referencia = "Referencia";
    public static final String KEY_FolioDespacho = "FolioDespacho";
    public static final String KEY_Estado = "Estado";
    //Query para la creación de la tabla
    public static final String CREATE_TABLE_USUARIO = "CREATE TABLE " + TABLE + " (" + KEY_id + " INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, " + Utilidades.KEY_ClaveUsuario + " TEXT, " +
            Utilidades.KEY_ContraUsuario + " TEXT )";

    public static final String CREATE_TABLE_DESPACHO = "CREATE TABLE " + TABLE_DESPACHO + " ("
            + Utilidades.KEY_Papeleta + " TEXT, " + Utilidades.KEY_Unidad + " TEXT , " + Utilidades.KEY_Manguera + " INTEGER, " +
            Utilidades.KEY_LitrosPapeleta + " REAL, " + Utilidades.KEY_LitrosSurtidos + " REAL, " + Utilidades.KEY_FechaDespacho +
            " TEXT, " + Utilidades.KEY_UsuarioApp + " TEXT, " + Utilidades.KEY_Empresa + " TEXT, " + Utilidades.KEY_Referencia +
            " TEXT, " + Utilidades.KEY_FolioDespacho + " TEXT, " + Utilidades.KEY_Estado + " INTEGER )";

}
