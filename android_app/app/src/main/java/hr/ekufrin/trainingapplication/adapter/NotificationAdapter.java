package hr.ekufrin.trainingapplication.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import hr.ekufrin.trainingapplication.R;
import hr.ekufrin.trainingapplication.model.Notification;
import hr.ekufrin.trainingapplication.service.NotificationApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context context;
    private List<Notification> notificationList;
    private NotificationApi api;
    private String token;
    private String userEmail;

    public NotificationAdapter(Context context, List<Notification> notificationList, NotificationApi api, String token) {
        this.context = context;
        this.notificationList = notificationList;
        this.api = api;
        this.token = token;

        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        this.userEmail = sharedPreferences.getString("user_email", "");
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Notification notification = notificationList.get(position);
        holder.messageTextView.setText(notification.getMessage());

        holder.markAsReadButton.setOnClickListener(v -> {
            api.markNotificationAsRead(token, userEmail, notification.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        notificationList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Marked as read", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to mark as read", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        Button markAsReadButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            markAsReadButton = itemView.findViewById(R.id.markAsReadButton);
        }
    }
}


