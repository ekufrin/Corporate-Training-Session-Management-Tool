package hr.ekufrin.trainingapplication.adapter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import hr.ekufrin.trainingapplication.R;
import hr.ekufrin.trainingapplication.model.TrainingSession;

@RequiresApi(api = Build.VERSION_CODES.O)
public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {
    private final List<TrainingSession> trainingList;

    public TrainingAdapter(List<TrainingSession> trainingList) {
        this.trainingList = trainingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrainingSession session = trainingList.get(position);
        holder.nameTextView.setText("🏋️‍♂️ " + session.getName());
        holder.dateTextView.setText("📅 " + session.getDate());
        holder.durationTextView.setText("⏳ " + session.getDuration() + " min");
        holder.locationTextView.setText("📍 " + session.getLocation());
        holder.statusTextView.setText("🟢 " + session.getStatus());
    }

    @Override
    public int getItemCount() {
        return trainingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, dateTextView, durationTextView, locationTextView, statusTextView, participantsTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            participantsTextView = itemView.findViewById(R.id.participantsTextView);
        }
    }
}


