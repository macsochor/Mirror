package com.hooapps.mirror;

/**
 * Created by mac on 5/21/17.
 */

import com.hooapps.mirror.POJO.Weather;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {
    @GET("298d8ef1e53e0ff82a4b33abc5658e7d/38.029,-78.4767/?exclude=minutely,houry,alerts,flags")
    Call<Weather> getWeather();
}