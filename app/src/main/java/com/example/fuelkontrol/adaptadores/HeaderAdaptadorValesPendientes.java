package com.example.fuelkontrol.adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelkontrol.R;
import com.example.fuelkontrol.helper.Utilitarios;
import com.example.fuelkontrol.prueba.MainActivity;

import java.util.ArrayList;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class HeaderAdaptadorValesPendientes extends RecyclerView.Adapter<HeaderAdaptadorValesPendientes.ViewHolder> {
    private static final String TAG = "HeaderAdapter";


    private ArrayList<HeaderAdaptadorVales> mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        Context contexto;
        TextView txtPapeleta;
        TextView txtUnidad;
        TextView txtLitrosPapeleta;
        TextView txtFecha;
        Button btnBomba1, btnBomba2;
        String unidad, papeleta, litros;
        TextView unidadDespachar1, papeletaDespachar1, litrosDespachar1,
                unidadDespachar2, papeletaDespachar2, litrosDespachar2;
        SwitchCompat swStatusd1, swStatusd2;

        //Referenciar los componentes que tenemos en los items
        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            contexto = v.getContext();
            txtPapeleta = (TextView) v.findViewById(R.id.txtPapeleta);
            txtUnidad = (TextView) v.findViewById(R.id.txtUnidad);
            txtLitrosPapeleta = (TextView) v.findViewById(R.id.txtLitrosPapeleta);
            txtFecha = (TextView) v.findViewById(R.id.txtFechaRegistro);
            btnBomba1 = (Button) v.findViewById(R.id.btnBomba1);
            btnBomba2 = (Button) v.findViewById(R.id.btnBomba2);
            //Enlazar al la actividad donde se va a usar el adapter
            unidadDespachar1 = (TextView) ((MainActivity) contexto).findViewById(R.id.UnidadB1);
            papeletaDespachar1 = (TextView) ((MainActivity) contexto).findViewById(R.id.PapeletasB1);
            litrosDespachar1 = (TextView) ((MainActivity) contexto).findViewById(R.id.LitrosB1);
            unidadDespachar2 = (TextView) ((MainActivity) contexto).findViewById(R.id.UnidadB2);
            papeletaDespachar2 = (TextView) ((MainActivity) contexto).findViewById(R.id.PapeletasB2);
            litrosDespachar2 = (TextView) ((MainActivity) contexto).findViewById(R.id.LitrosB2);
            swStatusd1 = (SwitchCompat) ((MainActivity) contexto).findViewById(R.id.swStatusD1);
            swStatusd2 = (SwitchCompat) ((MainActivity) contexto).findViewById(R.id.swStatusD2);
            String vP1 = Utilitarios.getDefaultsPreference("verificador", contexto);
            if(swStatusd1.isChecked()){
                btnBomba1.setEnabled(false);
            }else{
                btnBomba1.setEnabled(true);
            }
            if(swStatusd2.isChecked()){
                btnBomba2.setEnabled(false);
            }else{
                btnBomba2.setEnabled(true);
            }
          /*  if(vP1!=null){
            if (!vP1.equals("Despachando")) {
                btnBomba1.setEnabled(true);
            } else {
                btnBomba1.setEnabled(false);
            }
            }*/
            /*
            String vP1 = Utilitarios.getDefaultsPreference("verificador", contexto);
            if (!vP1.equals("Despachando")) {
                btnBomba1.setEnabled(true);
            } else {
                btnBomba1.setEnabled(false);
            }
            if (vP1.equals("xpermInicio")) {
                btnBomba1.setEnabled(true);
            } else {
                btnBomba1.setEnabled(false);
            }
            String vN = Utilitarios.getDefaultsPreference("verificador2", contexto);
            if (vN.equals("Despachando")) {
                btnBomba2.setEnabled(true);
            } else {
                btnBomba2.setEnabled(false);
            }

            if (vN.equals("xpermInicio")) {
                btnBomba2.setEnabled(true);
                btnBomba1.setEnabled(true);
            } else {
                btnBomba2.setEnabled(false);
                btnBomba2.setEnabled(false);
            }*/
            btnBomba1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contexto,"Iniciando despacho",Toast.LENGTH_LONG).show();
                    unidad = txtUnidad.getText().toString().substring(txtUnidad.getText().toString().lastIndexOf(":") + 1);
                    papeleta = txtPapeleta.getText().toString().substring(txtPapeleta.getText().toString().lastIndexOf(":") + 1);
                    litros = txtLitrosPapeleta.getText().toString().substring(txtLitrosPapeleta.getText().toString().lastIndexOf(":") + 1);
                    unidadDespachar1.setBackgroundResource(R.drawable.stylo_borde_despachando);
                    papeletaDespachar1.setBackgroundResource(R.drawable.stylo_borde_despachando);
                    litrosDespachar1.setBackgroundResource(R.drawable.stylo_borde_despachando);
                    unidadDespachar1.setText(unidad);
                    papeletaDespachar1.setText(papeleta);
                    litrosDespachar1.setText(litros);
                    swStatusd1.setChecked(true);

                }
            });

            btnBomba2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contexto, "Iniciando despacho", Toast.LENGTH_LONG).show();
                    unidad = txtUnidad.getText().toString().substring(txtUnidad.getText().toString().lastIndexOf(":") + 1);
                    papeleta = txtPapeleta.getText().toString().substring(txtPapeleta.getText().toString().lastIndexOf(":") + 1);
                    litros = txtLitrosPapeleta.getText().toString().substring(txtLitrosPapeleta.getText().toString().lastIndexOf(":") + 1);
                    unidadDespachar2.setBackgroundResource(R.drawable.stylo_borde_despachando);
                    papeletaDespachar2.setBackgroundResource(R.drawable.stylo_borde_despachando);
                    litrosDespachar2.setBackgroundResource(R.drawable.stylo_borde_despachando);
                    unidadDespachar2.setText(unidad);
                    papeletaDespachar2.setText(papeleta);
                    litrosDespachar2.setText(litros);
                    swStatusd2.setChecked(true);

                }
            });
            v.setOnClickListener(v1 -> {
            });

        }

    }


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public HeaderAdaptadorValesPendientes(ArrayList<HeaderAdaptadorVales> dataSet) {
        mDataSet = dataSet;

    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vales_pendientes, parent, false);

        return new ViewHolder(v);
    }

    // Se asignan los valores a cada atributo
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.txtPapeleta.setText("\uD83D\uDCC4: " + mDataSet.get(position).getPapeleta());
        viewHolder.txtUnidad.setText("Unidad: " + mDataSet.get(position).getUnidad());
        viewHolder.txtLitrosPapeleta.setText("Litros Papeleta: " + mDataSet.get(position).getLitrosPapeleta());
        viewHolder.txtFecha.setText(mDataSet.get(position).getFecha() + " \uD83D\uDCC5❌");
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
