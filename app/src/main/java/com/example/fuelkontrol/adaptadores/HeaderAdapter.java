package com.example.fuelkontrol.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.activity.ConsultasActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.ViewHolder> {
    private static final String TAG = "HeaderAdapter";

    private ArrayList<Header> mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        TextView txtFuelKontrol;
        TextView txtUsuario;
        TextView txtUnidad;
        TextView txtOdometro;
        TextView txtPapeleta;
        TextView txtLitrosPapeleta;
        TextView txtLitrosSurtidos;
        TextView txtFecha;
        CheckBox checkBox;
        String Usuario;
        String Unidad;
        String Odometro;
        String Papeleta;
        String LitrosPapeleta;
        String LitrosSurtido;
        String Fecha;


        TextView txtUsuario1;
        TextView txtUnidad2;
        TextView txtOdometro3;
        TextView txtPapeleta4;
        TextView txtLitrosPapeleta5;
        TextView txtLitrosSurtidos6;
        TextView txtFecha7;

        //Referenciar los componentes que tenemos en los items
        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            context = v.getContext();
            txtUsuario = (TextView) v.findViewById(R.id.txtUsuario);
            txtFuelKontrol = (TextView) v.findViewById(R.id.txtFuelKontrol);
            txtUnidad = (TextView) v.findViewById(R.id.txtUnidad);
            txtOdometro = (TextView) v.findViewById(R.id.txtOdometro);
            txtPapeleta = (TextView) v.findViewById(R.id.txtPapeleta);
            txtLitrosPapeleta = (TextView) v.findViewById(R.id.txtLitrosPapeleta);
            txtLitrosSurtidos = (TextView) v.findViewById(R.id.txtLitrosSurtidos);
            txtFecha = (TextView) v.findViewById(R.id.txtFechaRegistro);
            //((MainActivity) contexto).findViewById(R.id.swStatusD2);
            txtUsuario1 = ((ConsultasActivity) context).findViewById(R.id.Usurio1);
            txtUnidad2 = ((ConsultasActivity) context).findViewById(R.id.unidad1);
            txtOdometro3 = ((ConsultasActivity) context).findViewById(R.id.odometro1);
            txtPapeleta4 = ((ConsultasActivity) context).findViewById(R.id.Papeleta1);
            txtLitrosPapeleta5 = ((ConsultasActivity) context).findViewById(R.id.litrosPapeleta1);
            txtLitrosSurtidos6 = ((ConsultasActivity) context).findViewById(R.id.litrosdespacho1);
            txtFecha7 = ((ConsultasActivity) context).findViewById(R.id.fecha1);

            checkBox = ((ConsultasActivity) context).findViewById(R.id.chbImprimir);
            Usuario = txtUsuario.getText().toString();
            Unidad = txtUnidad.getText().toString();
            Odometro = txtOdometro.getText().toString();
            Papeleta = txtPapeleta.getText().toString();
            LitrosPapeleta = txtLitrosPapeleta.getText().toString();
            LitrosSurtido = txtLitrosSurtidos.getText().toString();
            Fecha = txtFecha.getText().toString();
            txtFuelKontrol.setText("F U E L  K O N T R O L  " + Calendar.getInstance().get(Calendar.YEAR) + "  ✓ ");

            v.setOnClickListener(v1 -> {
                txtUsuario1.setText(txtUsuario.getText().toString().substring(txtUsuario.getText().toString().lastIndexOf(":") + 1).trim());
                txtUnidad2.setText(txtUnidad.getText().toString().substring(txtUnidad.getText().toString().lastIndexOf(":") + 1).trim());
                txtOdometro3.setText(txtOdometro.getText().toString().substring(txtOdometro.getText().toString().lastIndexOf(":") + 1).trim());
                txtPapeleta4.setText(txtPapeleta.getText().toString().substring(txtPapeleta.getText().toString().lastIndexOf(":") + 1).trim());
                txtLitrosPapeleta5.setText(txtLitrosPapeleta.getText().toString().substring(txtLitrosPapeleta.getText().toString().lastIndexOf(":") + 1).trim());
                txtLitrosSurtidos6.setText(txtLitrosSurtidos.getText().toString().substring(txtLitrosSurtidos.getText().toString().lastIndexOf(":") + 1).trim());
                txtFecha7.setText(txtFecha.getText().toString());
                checkBox.setChecked(true);
            });

        }

    }


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public HeaderAdapter(ArrayList<Header> dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_header, parent, false);

        return new ViewHolder(v);
    }

    // Se asignan los valores a cada atributo
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Log.d(TAG, "Element " + position + " set.");
        // Get element from your dataset at this position and replace the contents of the view
        // with that element

        viewHolder.txtPapeleta.setText("\uD83D\uDCC4  V a l e : " + mDataSet.get(position).getPapeleta());
        viewHolder.txtUsuario.setText("\uD83D\uDC64  U s u a r i o :  " + mDataSet.get(position).getUsuario());
        viewHolder.txtUnidad.setText("\uD83D\uDE9B  U n i d a d : " + mDataSet.get(position).getUnidad());
        viewHolder.txtOdometro.setText(" ⚙  O d o m e t r o : " + mDataSet.get(position).getOdometro());
        viewHolder.txtLitrosPapeleta.setText("\uD83D\uDCC8   L i t r o s  V a l e : " + mDataSet.get(position).getLitrosPapeleta());
        viewHolder.txtLitrosSurtidos.setText("\uD83D\uDCC8   L i t r o s  S u r t i d o s : " + mDataSet.get(position).getLitrosSurtidos());
        viewHolder.txtFecha.setText(mDataSet.get(position).getFecha() + " \uD83D\uDCC5✔");
        /*   viewHolder.txtPapeleta.setText("\uD83D\uDCC4  V a l e : " + mDataSet.get(position).getPapeleta());
        viewHolder.txtUsuario.setText("\uD83D\uDC64  U s u a r i o :  " + mDataSet.get(position).getUsuario());
        viewHolder.txtUnidad.setText("\uD83D\uDE9B  U n i d a d : " + mDataSet.get(position).getUnidad());
        viewHolder.txtOdometro.setText(" ⚙  O d o m e t r o : " + mDataSet.get(position).getOdometro());
        viewHolder.txtLitrosPapeleta.setText("\uD83D\uDCC8   L i t r o s  V a l e : " + mDataSet.get(position).getLitrosPapeleta());
        viewHolder.txtLitrosSurtidos.setText("\uD83D\uDCC8   L i t r o s  S u r t i d o s : " + mDataSet.get(position).getLitrosSurtidos());
        viewHolder.txtFecha.setText(mDataSet.get(position).getFecha() + " \uD83D\uDCC5✔");*/

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
