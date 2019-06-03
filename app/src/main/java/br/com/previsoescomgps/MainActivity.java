package br.com.previsoescomgps;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView weatherRecyclerView;
    private WeatherAdapter adapter;
    private List<Weather> previsoes;
    private EditText locationEditText;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Double lat, lon;
    private static final int REQUEST_CODE_GPS = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        requestQueue = Volley.newRequestQueue(this);

        weatherRecyclerView = findViewById(R.id.weatherRecyclerView);
        previsoes = new ArrayList<>();
        adapter = new WeatherAdapter(previsoes, this);
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weatherRecyclerView.setAdapter(adapter);

//######################################################################################################################
        //implementação do listener que pega as posições do gps e chama do método resposável
        //por fazer a requisição e tratar o JSON
        locationManager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                String endereco =
                        getString(
                                R.string.web_service_url,
                                lat.toString(),
                                lon.toString(),
                                getString(
                                        R.string.api_key
                                )
                        );
                obtemPrevisoes(endereco);

            }
//########################################################################################################################

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };



    }

    public void obtemPrevisoes(String endereco){
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                endereco,
                null,
                (response) ->{
                    lidaComJSON(response);
                },
                (error) ->{
                    Toast.makeText(this,
                            getString(R.string.read_error),
                            Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }

    public void lidaComJSON (JSONObject json){
        lidaComJSON(json.toString());
    }

    public void lidaComJSON (String resultado){
        try {
            previsoes.clear();
            JSONObject json = new JSONObject(resultado);
            JSONArray list = json.getJSONArray("list");
            for (int i = 0; i < list.length(); i++){
                JSONObject previsaoDaVez = list.getJSONObject(i);
                long dt = previsaoDaVez.getLong("dt");
                JSONObject main = previsaoDaVez.getJSONObject("main");
                double temp_min =
                        main.getDouble("temp_min");
                double temp_max =
                        main.getDouble("temp_max");
                double humidity =
                        main.getDouble("humidity");
                JSONArray weather =
                        previsaoDaVez.getJSONArray("weather");
                JSONObject unico =
                        weather.getJSONObject(0);
                String description =
                        unico.getString("description");
                String icon =
                        unico.getString("icon");
                Weather w =
                        new Weather(
                                dt,
                                temp_min,
                                temp_max,
                                humidity,
                                description,
                                icon
                        );
                previsoes.add(w);
            }
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Toast.makeText(
                    this,
                    getString(R.string.read_error),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            locationManager.
                    requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1,
                            1,
                            locationListener
                    );
        }
        else{
            ActivityCompat.requestPermissions(
                    this,
                    new String []{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_GPS
            );
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS){
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1,
                            1,
                            locationListener
                    );
                }
            }
            else{
                Toast.makeText(this, "erro", Toast.LENGTH_SHORT).show();
            }
        }

    }

}