package com.app.content.liquorfinderapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText emailText;
    Button queryButton;
    ProgressBar progressBar;
    String result;
    String strUrl = "https://data.cityofchicago.org/resource/nrmj-3kcf.json?account_number=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailText = (EditText)findViewById(R.id.emailText);
        queryButton = (Button)this.findViewById(R.id.queryButton);
        queryButton.setOnClickListener(submitListner);

    }


    private OnClickListener submitListner = new OnClickListener()
    {
        public void onClick(View v)
        {
            new RetriveData().execute();
        }
    };


    public class RetriveData extends AsyncTask<String, String, String> {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView responseView = findViewById(R.id.responseView);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            View view = this.responseView;
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            progressBar.setVisibility(View.GONE);
            responseView.setText(result);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strUrl + emailText.getText().toString());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bf.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bf.close();
                String value =  stringBuilder.toString();


                Log.i("debug", value);
                result = value;


            }catch (Exception e){
                System.out.print(e);
            }
            return result;
        }
    }

}
