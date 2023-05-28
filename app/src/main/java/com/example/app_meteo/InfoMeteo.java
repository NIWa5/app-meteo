package com.example.app_meteo;

import java.util.List;

public class InfoMeteo {
    public Coordinates coord;
    public List<Weather> weather;
    public String base;
    public MainWeatherData main;
    public int visibility;
    public Wind wind;
    public Clouds clouds;
    public long dt;
    public Sys sys;
    public int timezone;
    public int id;
    public String name;
    public int cod;

    public static class Coordinates {
        public double lon;
        public double lat;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class MainWeatherData {
        public double temp;
        public double temp_min;
        public double temp_max;
    }

    public static class Wind {
        public double speed;
        public int deg;
    }

    public static class Clouds {
        public int all;
    }

    public static class Sys {
        public int type;
        public int id;
        public String country;
        public long sunrise;
        public long sunset;
    }
}