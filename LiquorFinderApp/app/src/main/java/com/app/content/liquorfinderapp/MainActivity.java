package com.app.content.liquorfinderapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private Button queryButton;
    private ProgressBar progressBar;
    private LocationManager locationManager;

    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;

    static final int REQUEST_LOCATION = 1;
    private String result;
    private final String strUrl = "https://data.cityofchicago.org/resource/nrmj-3kcf.json?doing_business_as_name=";
    double latti;
    private double longi;

    /**
     * Set up and bind views to their respective fields
     */
    protected void onCreate(Bundle savedInstanceState) {
        //Inflate views
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind views to fields
        inputText = (EditText) findViewById(R.id.emailText);
        queryButton = (Button) findViewById(R.id.queryButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mainListView = (ListView) findViewById( R.id.mainListView );

        //Set listeners
        queryButton.setOnClickListener(submitListener);
    }


    /**
     * Retrieve current location data using the google location service
     */
    void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null){

                latti = location.getLatitude();
                longi = location.getLongitude();
                Toast.makeText(MainActivity.this, "Lottitude: " + latti + " Longitute: " + longi, Toast.LENGTH_LONG).show();

            }
        }
    }

    /**
     * Request permission when location service is turned off
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                break;
        }
    }

    /**
     * Retrieve data on provided account number
     */
    private OnClickListener submitListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            new RetrieveData().execute();
            getLocation();
        }
    };

    /**
     * Parse JSON store data retrieved from api call
     */
    private void parseJSON(String data){
        try {
            //Obtain array which only has a json object in it
            JSONArray storeArray = new JSONArray(data);
            ArrayList<String> storetList = new ArrayList<String>();

            for( int i = 0; i < storeArray.length(); i++){

                JSONObject storeData = storeArray.getJSONObject(i);

                //Obtains individual data as required
                String businessName = storeData.optString("doing_business_as_name");
                String address = storeData.optString("address");
                String city = storeData.optString("city");
                String state = storeData.optString("state");
                String zip = storeData.optString("zip_code");
                String longitude = storeData.optString("longitude");
                String latitude = storeData.optString("latitude");

                Location storeLocation = new Location("storeLocation");
                storeLocation.setLatitude(Double.parseDouble(latitude));
                storeLocation.setLongitude(Double.parseDouble(longitude));

                Location currentLocation = new Location("currentLocation");
                currentLocation.setLatitude(latti);
                currentLocation.setLongitude(longi);

                double distance = currentLocation.distanceTo(storeLocation) / 1609.344;

                String fullAddress = (address + "\n" + city + ", " + state + " " + zip);


                String str = businessName + "\n" + fullAddress + "\n" +
                        "Distance: " + Math.round(distance * 100) / 100 + " miles";
                storetList.add(str);
            }
            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, storetList);
            mainListView.setAdapter( listAdapter );

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
            View view = mainListView;
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            //Hide progress bar and parse data
            progressBar.setVisibility(View.GONE);
            parseJSON(result);
        }
    }

}
