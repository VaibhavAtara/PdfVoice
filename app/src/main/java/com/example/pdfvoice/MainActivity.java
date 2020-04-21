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
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    TextView outputTextView;
    TextView TotalPages;
    TextToSpeech textToSpeech;
    EditText pageNo;
    int page_No=1;
    Uri uri;
    PdfReader pdfReader;
    Button previousPage,nextPage;
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
        pageNo = (EditText)findViewById(R.id.pageNo);
        previousPage =(Button)findViewById(R.id.previousPage);
        nextPage = (Button)findViewById(R.id.nextPage);
        TotalPages = (TextView)findViewById(R.id.TotalPages);



        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        textToSpeech.stop();
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

        pageNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                  if(uri!=null && editable.length()!=0)
                       readPdfFile(uri.toString());



            }
        });

        previousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uri==null)
                {
                    Toast.makeText(getApplicationContext(),"No pdf selected",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Integer.parseInt(pageNo.getText().toString())!=1) {
                    pageNo.setText(String.valueOf((Integer.parseInt(pageNo.getText().toString()) - 1)));
                    readPdfFile(uri.toString());
                }
                else{
                    Toast.makeText(getApplicationContext(),"This is First page!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(uri==null)
                {
                    Toast.makeText(getApplicationContext(),"No pdf selected",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Integer.parseInt(pageNo.getText().toString())!=pdfReader.getNumberOfPages()) {
                    pageNo.setText(String.valueOf((Integer.parseInt(pageNo.getText().toString()) + 1)));
                    readPdfFile(uri.toString());
                }
                else {
                    Toast.makeText(getApplicationContext(),"This is last page!!!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(resultData != null) {
                uri = resultData.getData();
                //Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
                readPdfFile(uri.toString());
            }
        }
    }


    public void readPdfFile(String uri) {

        String fullPath;
        String stringParser="";

      /* if(Uri.parse(uri).getPath().contains(PRIMARY)) {
            fullPath = LOCAL_STORAGE + Uri.parse(uri).getPath().split(COLON)[1];
        }
        else {
            fullPath = EXT_STORAGE + Uri.parse(uri).getPath().split(COLON)[1];
        }
        */

        Toast.makeText(getApplicationContext(),"Full:"+Uri.parse(uri).getPath(),Toast.LENGTH_LONG).show();
        try {

            pdfReader = new PdfReader("/storage/emulated/0/Download/And.pdf");
            page_No = Integer.parseInt(pageNo.getText().toString());

            if(page_No <=pdfReader.getNumberOfPages())
              stringParser = PdfTextExtractor.getTextFromPage(pdfReader, page_No).trim();


            TotalPages.setText("Total Pages : "+page_No+"/"+pdfReader.getNumberOfPages());

            pdfReader.close();
            outputTextView.setText(stringParser);

            Toast.makeText(getApplicationContext(),stringParser,Toast.LENGTH_LONG).show();
            textToSpeech.stop();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        textToSpeech.stop();
    }
}
