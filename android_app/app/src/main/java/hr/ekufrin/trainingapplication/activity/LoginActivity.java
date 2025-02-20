package hr.ekufrin.trainingapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

import hr.ekufrin.trainingapplication.R;
import hr.ekufrin.trainingapplication.model.LoginForm;
import hr.ekufrin.trainingapplication.service.ApiClient;
import hr.ekufrin.trainingapplication.service.AuthApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if(TextUtils.isEmpty(email.trim())){
            emailEditText.setError("Please enter email");
            return;
        }
        if(TextUtils.isEmpty(password)){
            passwordEditText.setError("Please enter password");
            return;
        }

        AuthApi api = ApiClient.getClient(this).create(AuthApi.class);
        Call<Map<String, String>> call = api.login(new LoginForm(email, password));

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.headers().get("Authorization");
                    if (token != null) {
                        saveToken(token,email);
                    }

                    String message = response.body().get("message");
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                    goToDashboard();
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong credentials!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API", "Error: " + t.getMessage());
            }
        });
    }


    private void saveToken(String token, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", email);
        editor.putString("auth_token", token);
        editor.apply();
    }

    private void goToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}