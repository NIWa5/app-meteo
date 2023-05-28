package com.example.app_meteo;

import android.content.Context;
import android.location.*;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public String temperatura = "5";
    public String condizioni = "nuvoloso";
    public String posizione = "catanzaro";
    public TextView testoCondizione;
    private Geocoder geocoder;
    private LocationManager locationManager; private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testoCondizione = (TextView) findViewById(R.id.condizione);
        testoCondizione.setText(condizioni);
        this.geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                try{
                    List<Address> indirizzi=geocoder.getFromLocation(latitude, longitude, 1);
                    if (indirizzi != null){
                    Address indirizzo = indirizzi.get(0);
                    posizione = indirizzo.getAddressLine(0);
                        System.out.println(posizione);

                    }
                } catch(IOException porcoddio){


                    porcoddio.printStackTrace();
                }
            }

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
}