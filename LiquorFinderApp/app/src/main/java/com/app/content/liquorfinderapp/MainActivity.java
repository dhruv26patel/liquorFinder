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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private TextView responseView;
    private Button queryButton;
    private ProgressBar progressBar;

    private String result;
    private final String strUrl = "https://data.cityofchicago.org/resource/nrmj-3kcf.json?account_number=";

    /**
     * Set up and bind views to their respective fields
     */
    protected void onCreate(Bundle savedInstanceState) {
        //Inflate views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind views to fields
        inputText = (EditText)findViewById(R.id.emailText);
        queryButton = (Button)findViewById(R.id.queryButton);
        responseView = (TextView) findViewById(R.id.responseView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Set listeners
        queryButton.setOnClickListener(submitListener);
    }

    /**
     * Retrieve data on provided account number
     */
    private OnClickListener submitListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            new RetrieveData().execute();
        }
    };

    /**
     * Parse JSON store data retrieved from api call
     */
    private void parseJSON(String data){
        try {
            //Obtain array which only has a json object in it
            JSONArray storeArray = new JSONArray(data);
            JSONObject storeData = storeArray.getJSONObject(0);


            //Obtains individual data as required
            String businessName = storeData.optString("doing_business_as_name");
            String address = storeData.optString("address");
            String city = storeData.optString("city");
            String state = storeData.optString("state");
            String zip = storeData.optString("zip_code");
            String longitude = storeData.optString("longitude");
            String latitude = storeData.optString("latitude");

            String fullAddress = (address + "\n" + city + ", " + state + " " + zip);

            responseView.setText(businessName + "\n"+ fullAddress + "\n" +
                    "Latitude: " + latitude + "\n" + "Longitude: " + longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Async task to make api call on a separate thread and communicate back
     * to the main thread
     */
    private class RetrieveData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Construct url and connect to api
                URL url = new URL(strUrl + inputText.getText().toString());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                //Obtain data from api and store into a string
                BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bf.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bf.close();
                String value =  stringBuilder.toString();

                //Store result of api call
                Log.i("debug", value);
                result = value;

            }catch (Exception e){
                System.out.print(e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            //Hide keyboard
            View view = responseView;
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            //Hide progress bar and parse data
            progressBar.setVisibility(View.GONE);
            responseView.setText(result);
            parseJSON(result);
        }
    }

}