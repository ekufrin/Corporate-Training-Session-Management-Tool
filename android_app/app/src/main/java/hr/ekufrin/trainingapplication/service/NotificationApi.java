package hr.ekufrin.trainingapplication.service;


import java.util.List;

import hr.ekufrin.trainingapplication.model.Notification;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface NotificationApi {

    @GET("api/notification/unread")
    Call<List<Notification>> getUnreadNotifications(@Header("Authorization") String token, @Query("email") String email);

    @PUT("api/notification/mark-as-read")
    Call<Void> markNotificationAsRead(@Header("Authorization") String token, @Query("email") String email, @Query("notificationId") Long notificationId);
}

