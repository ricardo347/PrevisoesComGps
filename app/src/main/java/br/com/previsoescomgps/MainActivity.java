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
import android.util.Log;
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
import com.google.gson.Gson;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
                    //lidaComJSON(response);
                    lidaComJSONGson(response.toString());
                },
                (error) ->{
                    Toast.makeText(this,
                            getString(R.string.read_error),
                            Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(req);
    }

    /*##########################################################################################################
    Implementação da biblioteca Gson
    usei a requisição normal, e todo o tratamento do JSON foi feito com a biblioteca, criando as classes modelo
    do objeto JSON

    */
    public void lidaComJSONGson(String resultado){
        Gson gson = new Gson();
        Previsao previsao = gson.fromJson(resultado, Previsao.class);

        for (int i = 0; i < previsao.getList().size();  i++){
            Weather w = new Weather(
                    previsao.getList().get(i).getDt(),
                    previsao.getList().get(i).getMain().getTemp_min(),
                    previsao.getList().get(i).getMain().getTemp_max(),
                    previsao.getList().get(i).getMain().getHumidity(),
                    previsao.getList().get(i).getWeather().get(0).getDescription(),//unico
                    previsao.getList().get(i).getWeather().get(0).getIcon()//unico
                    );
            previsoes.add(w);
            adapter.notifyDataSetChanged();
        }

    }




    public class Previsao{
        String message;
        String cnt;
        List<Lista> list;


        public List<Lista> getList() {
            return list;
        }

        public void setList(List<Lista> list) {
            this.list = list;
        }
    }

    public class Lista{
        long dt;
        Main main;
        List<Tempo> weather;

        public long getDt() {
            return dt;
        }

        public void setDt(long dt) {
            this.dt = dt;
        }

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public List<Tempo> getWeather() {
            return weather;
        }

        public void setWeather(List<Tempo> weather) {
            this.weather = weather;
        }

    }

    public class Main{
        double temp_min;
        double temp_max;
        double humidity;

        public double getTemp_min() {
            return temp_min;
        }

        public void setTemp_min(double temp_min) {
            this.temp_min = temp_min;
        }

        public double getTemp_max() {
            return temp_max;
        }

        public void setTemp_max(double temp_max) {
            this.temp_max = temp_max;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }
    }
    public class Tempo{
        String description;
        String icon;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

//###################################################################################################

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