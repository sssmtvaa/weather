package com.example.registration;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WeatherWidget extends AppWidgetProvider {
    private static final String API_KEY = "eda948d2db3469e6f3ae47706fefd9c3";
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
            views.setViewVisibility(R.id.progress_bar, View.VISIBLE); // Показать индикатор загрузки

            Intent intent = new Intent(context, WeatherWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.update_button, pendingIntent);

            updateWidgetData(context, views, appWidgetIds);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                views.setViewVisibility(R.id.progress_bar, View.VISIBLE); // Показать индикатор загрузки

                updateWidgetData(context, views, appWidgetIds);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                for (int appWidgetId : appWidgetIds) {
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        }
    }

    private void updateWidgetData(final Context context, final RemoteViews views, final int[] appWidgetIds) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            updateWidget(context, views, appWidgetIds, location);
                            locationManager.removeUpdates(this);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}

                        @Override
                        public void onProviderEnabled(String provider) {}

                        @Override
                        public void onProviderDisabled(String provider) {}
                    });
                } else {
                    Toast.makeText(context, "Permission for location is not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateWidget(final Context context, final RemoteViews views, final int[] appWidgetIds, final Location location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    String apiUrl = WEATHER_URL + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&units=metric&appid=" + API_KEY;
                    Request request = new Request.Builder().url(apiUrl).build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            JSONObject weatherData = new JSONObject(responseBody.string());
                            final String locationName = "Казань: " + weatherData.getString("name");
                            final String temperature = "Температура: " + weatherData.getJSONObject("main").getDouble("temp") + "°C";
                            final String feelsLike = "Ощущается как: " + weatherData.getJSONObject("main").getDouble("feels_like") + "°C";
                            final String lastUpdate = "Последнее обновление: " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                            final String iconCode = weatherData.getJSONArray("weather").getJSONObject(0).getString("icon");
                            final String weatherIconUrl = getWeatherIconUrl(iconCode);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    views.setViewVisibility(R.id.progress_bar, View.GONE);
                                    views.setTextViewText(R.id.location_text_view, locationName);
                                    views.setTextViewText(R.id.temperature_text_view, temperature);
                                    views.setTextViewText(R.id.feels_like_text_view, feelsLike);
                                    views.setTextViewText(R.id.last_update_text_view, lastUpdate);

                                    AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.weather_icon_image_view, views, appWidgetIds);
                                    Glide.with(context.getApplicationContext())
                                            .asBitmap()
                                            .load(weatherIconUrl)
                                            .into(appWidgetTarget);

                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                                    ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
                                    appWidgetManager.updateAppWidget(thisWidget, views);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String getWeatherIconUrl(String iconCode) {
        return "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }
}


