package com.example.fuelkontrol.adaptadores;

public class Header {
    private String Papeleta;
    private String Usuario;
    private String Unidad;
    private String Odometro;
    private String LitrosPapeleta;
    private String LitrosSurtidos;
    private String Fecha;

    public Header(String papeleta, String usuario, String unidad, String odometro, String litrosPapeleta, String litrosSurtidos, String fecha) {
        Papeleta = papeleta;
        Usuario = usuario;
        Unidad = unidad;
        Odometro = odometro;
        LitrosPapeleta = litrosPapeleta;
        LitrosSurtidos = litrosSurtidos;
        Fecha = fecha;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getUnidad() {
        return Unidad;
    }

    public void setUnidad(String unidad) {
        Unidad = unidad;
    }

    public String getOdometro() {
        return Odometro;
    }

    public void setOdometro(String odometro) {
        Odometro = odometro;
    }

    public String getPapeleta() {
        return Papeleta;
    }

    public void setPapeleta(String papeleta) {
        Papeleta = papeleta;
    }

    public String getLitrosPapeleta() {
        return LitrosPapeleta;
    }

    public void setLitrosPapeleta(String litrosPapeleta) {
        LitrosPapeleta = litrosPapeleta;
    }

    public String getLitrosSurtidos() {
        return LitrosSurtidos;
    }

    public void setLitrosSurtidos(String litrosSurtidos) {
        LitrosSurtidos = litrosSurtidos;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }
}
