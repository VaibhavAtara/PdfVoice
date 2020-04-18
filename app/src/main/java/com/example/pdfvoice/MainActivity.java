package com.example.pdfvoice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView outputTextView;
    TextToSpeech textToSpeech;
    private static final int READ_REQUEST_CODE = 42;
    private static final String PRIMARY = "primary";
    private static final String LOCAL_STORAGE = "/storage/self/primary/";
    private static final String EXT_STORAGE = "/storage/7764-A034/";
    private static final String COLON = ":";
    private Intent intent;

    boolean speak=true,stop=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

      final FloatingActionButton fab = findViewById(R.id.fab);



        outputTextView = findViewById(R.id.output_text);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());
        //pdfView = (PDFView)findViewById(R.id.pdfview);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(speak) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(Intent.createChooser(intent, "Choose PDF"), READ_REQUEST_CODE);
                    fab.setImageResource(R.drawable.ic_pause);
                    speak=false;
                    stop=true;
                }

                else if(stop){
                textToSpeech.stop();
                fab.setImageResource(R.drawable.ic_add_circle);
                stop=false;
                speak=true;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(resultData != null) {
                Uri uri = resultData.getData();
//                Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
                Log.v("URI", uri.getPath());
                readPdfFile(uri.toString());
            }
        }
    }


    public void readPdfFile(String uri) {

       // pdfView.fromUri(Uri.parse(uri)).load();
        String fullPath;
        //convert from uri to full path
       if(Uri.parse(uri).getPath().contains(PRIMARY)) {
            fullPath = LOCAL_STORAGE + Uri.parse(uri).getPath().split(COLON)[1];
        }
        else {
            fullPath = EXT_STORAGE + Uri.parse(uri).getPath().split(COLON)[1];
        }

        String stringParser="";
        Toast.makeText(getApplicationContext(),"Full:"+Uri.parse(uri).getPath(),Toast.LENGTH_LONG).show();
        try {

            PdfReader pdfReader = new PdfReader(fullPath);

            for(int i=1;i<=pdfReader.getNumberOfPages();i++)
              stringParser += PdfTextExtractor.getTextFromPage(pdfReader, i).trim();
            pdfReader.close();
           outputTextView.setText(stringParser);
            Toast.makeText(getApplicationContext(),stringParser,Toast.LENGTH_LONG).show();
            textToSpeech.speak(stringParser, TextToSpeech.QUEUE_FLUSH,null, null);
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
