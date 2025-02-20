package hr.ekufrin.trainingapplication.service;

import java.util.Map;

import hr.ekufrin.trainingapplication.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserApi {
    @POST("api/user/details")
    Call<User> getUserDetails(@Header("Authorization") String token, @Body Map<String, String> email);
}

