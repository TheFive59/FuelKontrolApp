package com.example.fuelkontrol.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.fuelkontrol.util.Utilidades;

/**
 * Clase que nos permite realizar la creacion de la base de datos sqlite
 *
 * @author Juan Manuel Miranda
 * @version 2021.1
 * @since 1.0
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 2;

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, DB_VERSION);
    }

    /**
     * Método para la creación de la base de datos
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Utilidades.CREATE_TABLE_USUARIO);
        db.execSQL(Utilidades.CREATE_TABLE_DESPACHO);

        ContentValues v = new ContentValues();
        v.put(Utilidades.KEY_id, "2");
        v.put(Utilidades.KEY_ClaveUsuario, "root");
        v.put(Utilidades.KEY_ContraUsuario, "root");
        db.insert(Utilidades.TABLE, Utilidades.KEY_id, v);
    }

    /**
     * Método para eliminar base de datos si ya existe y volverla a crear
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS  " + Utilidades.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + Utilidades.TABLE_DESPACHO);
        onCreate(db);
    }
}
