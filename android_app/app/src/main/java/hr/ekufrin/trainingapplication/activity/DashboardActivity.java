package hr.ekufrin.trainingapplication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hr.ekufrin.trainingapplication.R;
import hr.ekufrin.trainingapplication.adapter.NotificationAdapter;
import hr.ekufrin.trainingapplication.adapter.TrainingAdapter;
import hr.ekufrin.trainingapplication.model.Notification;
import hr.ekufrin.trainingapplication.model.TrainingSession;
import hr.ekufrin.trainingapplication.model.User;
import hr.ekufrin.trainingapplication.service.ApiClient;
import hr.ekufrin.trainingapplication.service.NotificationApi;
import hr.ekufrin.trainingapplication.service.TrainingApi;
import hr.ekufrin.trainingapplication.service.UserApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    private TextView userNameTextView, roleEmojiTextView;
    private RecyclerView recyclerView, recyclerViewNotifications;
    private TrainingAdapter adapter;
    private NotificationAdapter notificationAdapter;
    private List<TrainingSession> trainingList = new ArrayList<>();
    private List<Notification> notificationList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        userNameTextView = findViewById(R.id.userNameTextView);
        roleEmojiTextView = findViewById(R.id.roleEmojiTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        Button logoutButton = findViewById(R.id.logoutButton);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", null);
        String token = sharedPreferences.getString("auth_token", null);

        if (userEmail != null && token != null) {
            fetchUserDetails(userEmail, token);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adapter = new TrainingAdapter(trainingList);
        }
        notificationAdapter = new NotificationAdapter(this, notificationList, ApiClient.getClient(this).create(NotificationApi.class), token);

        recyclerView.setAdapter(adapter);
        recyclerViewNotifications.setAdapter(notificationAdapter);

        loadActiveTrainingSessions(token);
        loadUnreadNotifications(token, userEmail);

        logoutButton.setOnClickListener(v -> {
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadActiveTrainingSessions(token);
                loadUnreadNotifications(token, userEmail);
                handler.postDelayed(this, 60000);
            }
        };
        handler.postDelayed(refreshRunnable, 60000);
    }

    private void fetchUserDetails(String email, String token) {
        UserApi api = ApiClient.getClient(this).create(UserApi.class);
        Map<String, String> request = new HashMap<>();
        request.put("email",email);
        api.getUserDetails(token, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(DashboardActivity.this, "API greška: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("API ERROR", response.message());
                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(DashboardActivity.this, "Prazan odgovor!", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = response.body();
                userNameTextView.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());

                if ("Trainer".equals(user.getRole())) {
                    roleEmojiTextView.setText("🏋️‍♂️");
                } else {
                    roleEmojiTextView.setText("👤");
                }

                sharedPreferences.edit()
                        .putString("user_name", user.getFirstName() + " " + user.getLastName())
                        .putString("user_role", user.getRole())
                        .apply();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ON FAILURE: ", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void loadActiveTrainingSessions(String token) {
        TrainingApi api = ApiClient.getClient(this).create(TrainingApi.class);
        api.getActiveTrainingSessions(token).enqueue(new Callback<List<TrainingSession>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<TrainingSession>> call, Response<List<TrainingSession>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trainingList.clear();
                    trainingList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    if(response.code() == 401){
                        showSessionExpiredDialog();
                        return;
                    }
                    Toast.makeText(DashboardActivity.this, "Neuspjelo dohvaćanje trening sesija", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TrainingSession>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSessionExpiredDialog() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                Log.e("SESSION EXPIRED DIALOG", "Activity is not running, cannot show dialog.");
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
            builder.setTitle("Session Expired")
                    .setMessage("Your session has expired. Please log in again.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        sharedPreferences.edit().clear().apply();

                        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    });

            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private void loadUnreadNotifications(String token, String email) {
        NotificationApi api = ApiClient.getClient(this).create(NotificationApi.class);
        api.getUnreadNotifications(token, email).enqueue(new Callback<List<Notification>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body());
                    recyclerViewNotifications.setAdapter(notificationAdapter);
                    notificationAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DashboardActivity.this, "No unread notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
