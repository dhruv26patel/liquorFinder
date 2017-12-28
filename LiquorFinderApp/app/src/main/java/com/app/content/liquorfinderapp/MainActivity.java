package com.app.content.liquorfinderapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
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

    private EditText editText;
    Button submitButton;
    ProgressBar progressBar;
    TextView textBox;
    String result;
    String strUrl = "https://www.facebook.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.EditTextName);
        submitButton = (Button)this.findViewById(R.id.button2);
        submitButton.setOnClickListener(submitListner);

    }


    private OnClickListener submitListner = new OnClickListener()
    {
        public void onClick(View v)
        {

            setData(editText.getText().toString());
            new RetriveData().execute();
            //setData(new RetriveData().doInBackground());
        }
    };

    protected void setData(String data){

        textBox = findViewById(R.id.textView);
        textBox.setText(data);

    }

    public class RetriveData extends AsyncTask<String, String, String> {
        ProgressBar progressBar = findViewById(R.id.progressBar2);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "The output is:" + result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String value = bf.readLine();
                Log.i("debug", value);
                result = value;


            }catch (Exception e){
                System.out.print(e);
            }
            return result;
        }
    }

}
