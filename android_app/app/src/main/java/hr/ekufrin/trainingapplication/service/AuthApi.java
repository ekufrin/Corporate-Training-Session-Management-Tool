package hr.ekufrin.trainingapplication.service;

import java.util.Map;

import hr.ekufrin.trainingapplication.model.LoginForm;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/login")
    @Headers("Content-Type: application/json")
    Call<Map<String,String>> login(@Body LoginForm loginForm);
}
