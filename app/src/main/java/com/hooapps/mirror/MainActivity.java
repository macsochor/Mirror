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

import com.hooapps.mirror.POJO.Currently;
import com.hooapps.mirror.POJO.Hourly;
import com.hooapps.mirror.POJO.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    @BindView(R.id.date) TextView date;
    @BindView(R.id.now) TextView now;
    @BindView(R.id.later) TextView later;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Make it fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        update();

        Observable.interval(5, TimeUnit.MINUTES)
                .doOnNext(n -> update())
                .subscribe();
    }
    public void update(){
        final SimpleDateFormat sdf = new SimpleDateFormat("EEEE MMM dd");
        final SimpleDateFormat time = new SimpleDateFormat("mm:hh aa");
        APIService apiService = RetrofitClient.getClient("https://api.darksky.net/forecast/").create(APIService.class);
        apiService.getWeather().enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Log.e(TAG, String.valueOf(response.code()));
                if (response.isSuccessful()) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    Weather weather = response.body();
                    Currently currently = weather.getCurrently();
                    Hourly hourly = weather.getHourly();
                    Log.e("Hourly size", String.valueOf(hourly.getData().size()));
                    date.setText(sdf.format(new Date(System.currentTimeMillis())));
                    now.setText("Now: " + currently.getApparentTemperature() + "°F " + currently.getSummary());
                    later.setText("Later: " + hourly.getSummary() + "\n\n" + hourly.getData().get(4) + "\n" + hourly.getData().get(8));
                }
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Log.e(TAG, t.getMessage());
                later.setText("ERROR");
                now.setText(t.getMessage());
            }
        });
    }
}