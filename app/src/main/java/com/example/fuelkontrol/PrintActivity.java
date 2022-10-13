package com.example.fuelkontrol;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.fuelkontrol.helper.Utilitarios;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class PrintActivity extends AppCompatActivity {
    File ruta_sd;
    File carpeta_FuelKontrol;
    File archivo;
    Uri uri;
    // declaring width and height
    // for our PDF file.
    int pageHeight = 500;
    int pagewidth = 400;

    // creating a bitmap variable
    // for storing our images
    Bitmap bmp, scaledbmp;

    // constant code for runtime permissions
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        ruta_sd = Environment.getExternalStorageDirectory();

        carpeta_FuelKontrol = new File(ruta_sd.getAbsolutePath(), "FuelKontrol");
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo_fuel);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 100, 95, false);

        // below code is used for
        // checking our permissions.
        if (checkPermission()) {
        } else {
            requestPermission();
        }
        generatePDF();
        Button btnGenerar = (Button) findViewById(R.id.btnGenerar);
        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();
                //Toast.makeText(PrintActivity.this, "Se creo tu archivo pdf", Toast.LENGTH_SHORT).show();

            }

        });
    }

    private void generatePDF() {
        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        Paint paint = new Paint();
        Paint title = new Paint();

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight,
                1).create();

        // below line is used for setting
        // start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.

        //canvas.drawBitmap(scaledbmp, 30, 25, paint);
        //canvas.drawText("Fuel Kontrol " + Calendar.getInstance().get(Calendar.YEAR), 209, 80, title);

        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15);

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.fondo));

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
        canvas.drawBitmap(scaledbmp, 30, 50, paint);
        canvas.drawText("T  i  c  k  e  t    d  e    d  e  s  p  a  c  h  o  .", 90, 40, title);
        canvas.drawText("FUEL KONTROL " + Calendar.getInstance().get(Calendar.YEAR), 140, 80, title);
        canvas.drawText(Utilitarios.getDefaultsPreference("empresaajustes", this), 140, 120, title);
        canvas.drawText("Vale", 100, 190, title);
        canvas.drawText(": TR525", 220, 190, title);

        canvas.drawText("Usuario", 100, 220, title);
        canvas.drawText(": PRUEBA APP", 220, 220, title);

        canvas.drawText("Odometro", 100, 250, title);
        canvas.drawText(": 1.0", 220, 250, title);

        canvas.drawText("Litros" + " lts", 100, 280, title);
        canvas.drawText(": 100.00" + " lts", 220, 280, title);

        canvas.drawText("Litros surtidos", 100, 310, title);
        canvas.drawText(": 100.00" + " lts", 220, 310, title);

        canvas.drawText("Fecha despacho", 100, 340, title);
        canvas.drawText(": 2022-02-18 15:27", 220, 340, title);
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Creación", 130, 420, title);
        canvas.drawText(": 2022-02-18 15:27", 220, 420, title);

        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.fondo));
        title.setTextSize(15);

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.CENTER);
        //canvas.drawText("FK", 396, 560, title);

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of
        // our PDF file and its path

        if (!carpeta_FuelKontrol.exists()) {
            carpeta_FuelKontrol.mkdir();
        }
        try {
            // after creating a file name we will
            // write our PDF file to that location.
            if (!carpeta_FuelKontrol.exists()) {
                carpeta_FuelKontrol.mkdir();
            } else {
                archivo = new File(ruta_sd.getAbsolutePath() + "/FuelKontrol", "Archivo3.pdf");
                pdfDocument.writeTo(new FileOutputStream(archivo));
                //compartir();
                shareTicket();
            }
            // below line is to print toast message
            // on completion of PDF generation.
            //Toast.makeText(PrintActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(PrintActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        pdfDocument.close();
    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permiso Otorgado..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    void shareTicket() {
        archivo = new File(ruta_sd.getAbsolutePath(), "FuelKontrol/Archivo3.pdf");
        uri = FileProvider.getUriForFile(PrintActivity.this, "com.example.fuelkontrol.fileprovider", archivo);
        Intent intent = ShareCompat.IntentBuilder.from(PrintActivity.this)
                .setType("application/pdf")
                .setStream(uri)
                .setChooserTitle("Compartir a través")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PrintActivity.this.startActivity(intent);
    }
}
