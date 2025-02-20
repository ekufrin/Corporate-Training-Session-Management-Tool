package hr.ekufrin.trainingapplication.service;

import java.util.List;

import hr.ekufrin.trainingapplication.model.TrainingSession;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface TrainingApi {
    @GET("api/training-session/active")
    Call<List<TrainingSession>> getActiveTrainingSessions(@Header("Authorization") String token);
}

