package com.hooapps.mirror;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends Activity {

    public TextView date, now, tdl, later;
    public LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get rid of the bar at the top
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //declaration of views
        date = (TextView) findViewById(R.id.date);
        now = (TextView) findViewById(R.id.now);
        later = (TextView) findViewById(R.id.later);
        tdl = (TextView) findViewById(R.id.todolist);
        ll = (LinearLayout)findViewById(R.id.linlay);

        new RetrieveCurWeather().execute();
        new RetrieveLaterWeather().execute();
        new RetrieveToDoList().execute();

        //Continuously calls the AsyncTasks to get data for weather and the to-do list. Also formats and displays the date
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String s = String.valueOf(android.text.format.DateFormat.format("EEEE", new Date()));
                                Date d = new Date();
                                String months[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                                Calendar calendar = new GregorianCalendar();
                                s += (", " + months[d.getMonth()] + " " + d.getDate());
                                date.setText(s);
                                new RetrieveCurWeather().execute();
                                new RetrieveLaterWeather().execute();
                                new RetrieveToDoList().execute();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    tdl.setText("error in the thread");
                }
            }
        };
        t.start();
    }


    //Get the current weather data for Charlottesville using the OpenWeatherMaps API
    class RetrieveCurWeather extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {


            try {
                //query every 10 seconds
                Thread.sleep(10000);
                Log.e("owmnow", "trying to access owm");

                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=Charlottesville,us&units=imperial&appid=0a6fd0bc3cbc93b442fa84a214d542b2");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            Log.i("INFO", response);
            try {
                JSONObject entireJO = new JSONObject(response);
                JSONArray weatherarr = entireJO.getJSONArray("weather");
                JSONObject weather = weatherarr.getJSONObject(0);
                String condition = weather.getString("description");
                JSONObject tempObj = entireJO.getJSONObject("main");
                String temp = tempObj.getString("temp");
                now.setText("Now: " + temp + " ºF " + condition);

            } catch (JSONException e) {
                now.setText(e.toString());
                e.printStackTrace();
            }

        }
    }

    //This AsyncTask gets the JSON data from OpenWeatherMaps API corresponding to the predicted weather for a few hours later
    class RetrieveLaterWeather extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("");
        }

        protected String doInBackground(Void... urls) {
            //String email = emailText.getText().toString();
            // Do some validation here


            try {
                //wait 10 seconds. Don't want to use up a ton of bandwidth
                Thread.sleep(10000);

                Log.e("owmlater", "trying to access owm");
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=Charlottesville,us&units=imperial&appid=0a6fd0bc3cbc93b442fa84a214d542b2");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            //progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            try {
                JSONObject entireJO = new JSONObject(response);
                //JSONObject data = entireJO.getJSONObject("object");
                JSONArray fiveday = entireJO.getJSONArray("list");
                JSONObject today = fiveday.getJSONObject(0);
                JSONArray weatherarr = today.getJSONArray("weather");
                JSONObject weather = weatherarr.getJSONObject(0);
                String condition = weather.getString("description");
                JSONObject tempObj = today.getJSONObject("main");
                String temp = tempObj.getString("temp");
                later.setText("Later: " + temp + " ºF " + condition);

            } catch (JSONException e) {
                later.setText(e.toString());
                e.printStackTrace();
            }

        }
    }

    //This AsyncTask gets JSON data from the firebase database that is updated and used by the to-do app I built
    class RetrieveToDoList extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {
            try {
                Thread.sleep(10000);
                Log.e("tdl", "trying to access tdl");
                URL url = new URL("https://to-do-list-cd6d1.firebaseio.com/.json?print=pretty");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }
        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }

            Log.i("INFO", response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject tasks = jsonObject.getJSONObject("tasks");
                JSONArray names = tasks.names();
                int len = tasks.length();
                String s = "To Do:\n";
                TextView todotv = (TextView)getLayoutInflater().inflate(R.layout.customtv, null);
                ll.removeAllViews();
                todotv.setText("To Do:");

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                params.weight = 1.0f;
                params.gravity = Gravity.RIGHT;
                todotv.setLayoutParams(params);

                ll.addView(todotv);

                for(int i = 0; i < len; i++) {
                    JSONObject j = tasks.getJSONObject(names.getString(i));
                    TextView tv = (TextView)getLayoutInflater().inflate(R.layout.customtv, null);
                    tv.setText(j.getString("payload"));


                    tv.setLayoutParams(params);

                    ll.addView(tv);
                }
            } catch (Exception e) {
                tdl.setText(e.toString());
                e.printStackTrace();
            }

        }
    }
}