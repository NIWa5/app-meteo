package com.example.app_meteo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{
    //API
    private String apiKey = "62dfc6b9853b50153e852fbfab0f4cf8";
    private String API_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather";

    //Strings
    public String temperatura = "5";
    public String condizioni = "nuvoloso";
    public String posizione = "catanzaro";

    //Text Views
    public TextView locationTextView;
    public TextView lastUpdatedView;

    public TextView temperatureView;
    public TextView minTempView;
    public TextView maxTempView;
    public TextView conditionView;

    public TextView windSpeedView;
    public TextView sunriseTimeView;
    public TextView sunsetTimeView;

    public ImageView weatherIconView;

    public RelativeLayout mainContentView;

    //Objs
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //Estraiamo dal file XML i vari text view (paragrafi) in modo che
        //possiamo modificarne il contenuto
        locationTextView = (TextView) findViewById(R.id.locazione);
        lastUpdatedView = (TextView) findViewById(R.id.ultimo_aggiornato);

        temperatureView = (TextView) findViewById(R.id.temperatura);

        minTempView = (TextView) findViewById(R.id.temp_min);
        maxTempView = (TextView) findViewById(R.id.temp_max);

        conditionView = (TextView) findViewById(R.id.condizione) ;

        windSpeedView = (TextView) findViewById(R.id.wind_speed);
        sunriseTimeView = (TextView) findViewById(R.id.sunrise_time);
        sunsetTimeView = (TextView) findViewById(R.id.sunset_time);

        mainContentView = (RelativeLayout) findViewById(R.id.main_content);

        initLocation();
    }

    private void initLocation(){
        //Inizializzazione del sistema GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){ // Controllo permessi del GPS
            //Se l'applicazione non ha i permesssi di accedere al GPS, chiediamo allo user di darceli
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        //Request location updates é un metodo che ci permette di specificare
        //quante volte vogliamo gli aggiornamenti in base ad un tempo in millisecondi
        //ed uno spostamento in metri. Ex: mi sposto di 1000 metri, il location manager
        //controllerá di nuovo la mia posizione.
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1000, this);

        //Per sicurezza, appena creiamo l'app prendiamo l'ultima posizione registrata.
        Location last_location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationTextView.setText(getAddressFromLocation(last_location));

        getWeatherFromAPI(getAddressFromLocation(last_location));
    }

    //quando la posizione cambia, questo blocco di codice verrá eseguito.
    @Override
    public void onLocationChanged(@NonNull Location location) {
        locationTextView.setText(getAddressFromLocation(location));
        getWeatherFromAPI(getAddressFromLocation(location));
    }

    //Questo metodo accetta una Location, ovvero una classe che contiene vari attributi, tra
    //i quali una latitudine, longitudine ecc...
    private String getAddressFromLocation(Location location){
        if(location == null) return "Lamezia Terme, IT";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault()); //Classe che ci permette di estrarre gli indirrizi

        //Il try and catch block
        try {
            //Il geocoder ci ritornerá una lista di indirizzi dopo che gli abbiamo fornito una latitudine e una longitudine.
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                //Prendiamo il primo indirizzo e ritorniamo la cittá e il codice della nazione.
                return addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryCode();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void getWeatherFromAPI(String location){
        //Ogni volta che chiamiamo questa funzione, vuol dire che
        //abbiamo fatto un update delle condizioni meteo
        Date localDateTime = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM HH:mm", Locale.US);
        String time = "Last updated at: " + dateFormat.format(localDateTime);

        //aggiorniamo quindi la data del "last update" nella schermata principale.
        lastUpdatedView.setText(time);

        //Recuperare i dati da una API é una funzione asincrona. Questo perché ci mette
        //in media qualche secondo. Il nostro processo principale non puó aspettare tanto tempo e
        //perció creiamo un altro thread, in modo che mentre le informazioni vengono ricavate
        //dal server, noi possiamo continuare ad usare l'app.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Creiamo un url ed una connessione al nostro server.
                    URL url = new URL(API_ENDPOINT + "?q=" + posizione + "&appid=" + apiKey);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();

                    //Se il codice di risposta (tipo 400, 404, 200 ecc..) é OK, ovvero 200
                    //vuol dire che abbiamo ricevuto una risposta.
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        //What the fuck
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        //Fino ad ora con una magia abbiamo trasformato la risposta del server
                        //in una stringa
                        String jsonResponse = response.toString();

                        //L'object mapper ci permette di associare un json ad una classe.
                        //Un file json ha una chiave ed un valore
                        //Esempio : {"nome" : "Francesco"}
                        //Anche una classe alla fine ha un funzionamento simile, avendo
                        //una variabile con un nome e un suo contenuto.
                        ObjectMapper objectMapper = new ObjectMapper();
                        //questa configurazione ci permette di non far crashare l'intera applicazione
                        //nel caso incontra qualche campo che non abbiamo messo nella classe
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        //il readValue() accetta una stringa JSON e una classe con tutti i valori da associare.
                        //Esempio: se noi abbiamo un json {"nome" : "Francesco"} avremo bisogno di una classe del tipo
                        // MiaClasse{
                        //      String nome;
                        // }
                        final InfoMeteo infoMeteo = objectMapper.readValue(jsonResponse, InfoMeteo.class);

                        if(infoMeteo != null){
                            //What the fuck pt.2
                            //Android studio ha richieste molto particolari purtroppo
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Temperature varie da Kelvin in Celsius
                                    String temp = String.valueOf((int)infoMeteo.main.temp -273) + " °C";
                                    String min_temp = "Min: " + String.valueOf((int)infoMeteo.main.temp_min - 273) +  " °C";
                                    String max_temp = "Max: " + String.valueOf((int)infoMeteo.main.temp_max - 273) +  " °C";

                                    //Temperatura del vento da m/s a km/h
                                    String wind_speed = String.valueOf(Math.round(infoMeteo.wind.speed * 3.6))+ " km/h";

                                    //Caso particolare: Il server in questo caso ci ritorna come data
                                    //i secondi passati non ricordo se dalla nascita di cristo oppure dal 1970
                                    //In ogni caso, la classe Date accetta un valore in millisecondi,
                                    //quindi prendiamo e moltiplichiamo tutto per mille
                                    Date sunrise_date = new Date(infoMeteo.sys.sunrise * 1000);
                                    Date sunset_date = new Date(infoMeteo.sys.sunset * 1000);

                                    //Formattazione della data in un formato leggibile tipo 17:30
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);

                                    String sunrise_time = dateFormat.format(sunrise_date);
                                    String sunset_time = dateFormat.format(sunset_date);

                                    temperatureView.setText(temp);
                                    minTempView.setText(min_temp);
                                    maxTempView.setText(max_temp);

                                    windSpeedView.setText(wind_speed);
                                    sunriseTimeView.setText(sunrise_time);
                                    sunsetTimeView.setText(sunset_time);

                                    String weather = infoMeteo.weather.get(0).main;
                                    conditionView.setText(weather);

                                    int bgResource = R.drawable.sunny_background;

                                    if(checkIfItIsNight(sunset_time)){
                                        bgResource = R.drawable.night_sky;
                                    }

                                    if(weather.toLowerCase().contains("cloud")){
                                        bgResource = R.drawable.cloudy_background;
                                    }
                                    if(weather.toLowerCase().contains("rain")){
                                        bgResource = R.drawable.rainy_background;
                                    }

                                    mainContentView.setBackgroundResource(bgResource);
                                }

                                //Evil string manipulation hack
                                //La funzione accetta una stringa del tipo "19:30"
                                //e ora capirai il perché. "19:30" é l'orario del tramonto
                                boolean checkIfItIsNight(String timeHHmm){
                                    //Prima ci prendiamo l'ora e il minuto del giorno attuali
                                    Calendar calendar = Calendar.getInstance();
                                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                                    int currentMinute = calendar.get(Calendar.MINUTE);

                                    //Per ottenere l'ora e il minuto del tramonto,
                                    //dividiamo 19:30 in un array di due stringhe,
                                    //ovvero 19 e 30. Li divideremo in base ai due punti
                                    String[] sunsetParts = timeHHmm.split(":");
                                    int sunsetHour = Integer.parseInt(sunsetParts[0]);
                                    int sunsetMinute = Integer.parseInt(sunsetParts[1]);

                                    //Boolean magico
                                    boolean isAfterSunset;

                                    if (currentHour > sunsetHour) {
                                        isAfterSunset = true;
                                    } else if (currentHour == sunsetHour) {
                                        isAfterSunset = currentMinute > sunsetMinute;
                                    } else {
                                        isAfterSunset = false;
                                    }

                                    return isAfterSunset;
                                }
                            });

                            return ;
                        }

                    } else {
                        Log.d("METEO_ERROR", String.valueOf(responseCode));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}