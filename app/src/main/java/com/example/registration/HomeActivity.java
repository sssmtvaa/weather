package com.example.registration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.TemporalAdjuster;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final String URL = "https://www.example.com/data";
    private static final int REQUEST_CODE = 100 ;
    private EditText user_field;
    private TextView result_info;
    private Button main_btn;
    private ImageView iconImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

            }

            user_field = findViewById(R.id.user_field);
            main_btn = findViewById(R.id.main_btn);
            result_info = findViewById(R.id.result_info);
            iconImageView = findViewById(R.id.icon_image);



            main_btn.setOnClickListener(v -> {
                if (user_field.getText().toString().trim().equals(""))
                    Toast.makeText(HomeActivity.this, R.string.no_user_input, Toast.LENGTH_LONG).show();
                else {
                    String city = user_field.getText().toString();
                    String APIkey = "eda948d2db3469e6f3ae47706fefd9c3";
                    String ICON_URL = "http://openweathermap.org/img/w%s.png";
                    String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + APIkey + "&units=metric&lang=ru";

                    new GetURLData().execute(url);


                }
            });

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, выполните необходимые действия
            } else {
                // Разрешение не предоставлено, обработайте этот случай
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class GetURLData extends AsyncTask<String, String, String> {


//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            result_info.setText("Ожидайте....");
//            // Дополнительные действия перед выполнением запроса
//        }


        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
            @SuppressLint("SetTextI18n")
            protected void onPostExecute(String result) {
                super.onPostResume();

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    double temperature = jsonObject.getJSONObject("main").getDouble("temp");
                    String weather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    double humidity = jsonObject.getJSONObject("main").getDouble("humidity");
                    String temperatureWithCelsius = temperature + "\u00B0C";
                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    String icon = "";
                    String iconUrl = icon;
                    JSONArray Jarray = jsonObject.getJSONArray("weather");
                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject jsonObject1 = Jarray.getJSONObject(i);
                        icon = jsonObject1.getString("icon");
                    }
                    String iconurl = "http://openweathermap.org/img/w/" + icon + ".png";
                    Glide.with(HomeActivity.this)
                            .load(iconurl)
                            .override(200, 200)
                            .into(iconImageView);


                    result_info.setText("Температура: " + temperatureWithCelsius + "\n" +
                            "Погода: " + weather + "\n" +
                            "Влажность: " + humidity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
}


