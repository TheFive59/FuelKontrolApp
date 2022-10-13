package com.example.fuelkontrol.adaptadores;

public class HeaderAdaptadorVales {
    private String Papeleta;
    private String Unidad;
    private String LitrosPapeleta;
    private String Fecha;

    public HeaderAdaptadorVales(String papeleta, String unidad, String litrosPapeleta, String fecha) {
        Papeleta = papeleta;
        Unidad = unidad;
        LitrosPapeleta = litrosPapeleta;
        Fecha = fecha;
    }

    public String getPapeleta() {
        return Papeleta;
    }

    public void setPapeleta(String papeleta) {
        Papeleta = papeleta;
    }

    public String getUnidad() {
        return Unidad;
    }

    public void setUnidad(String unidad) {
        Unidad = unidad;
    }

    public String getLitrosPapeleta() {
        return LitrosPapeleta;
    }

    public void setLitrosPapeleta(String litrosPapeleta) {
        LitrosPapeleta = litrosPapeleta;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }
}
