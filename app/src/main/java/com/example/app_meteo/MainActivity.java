package com.example.app_meteo;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public String temperatura = "5";
    public String condizioni = "nuvoloso";
    public TextView testoCondizione;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testoCondizione = (TextView) findViewById(R.id.condizione);
        testoCondizione.setText(condizioni);
    }
}