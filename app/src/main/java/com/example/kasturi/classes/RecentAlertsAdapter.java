package com.example.kasturi.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kasturi.R;
import com.example.kasturi.classes.RecentAlert;
import java.util.List;

public class RecentAlertsAdapter extends RecyclerView.Adapter<RecentAlertsAdapter.AlertViewHolder> {

    private final List<RecentAlert> recentAlerts;

    public RecentAlertsAdapter(List<RecentAlert> recentAlerts) {
        this.recentAlerts = recentAlerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        RecentAlert alert = recentAlerts.get(position);
        holder.alertTitle.setText(alert.getTitle());
        holder.alertTime.setText(alert.getTime());
        holder.alertIcon.setImageResource(alert.getIconResId());
    }

    @Override
    public int getItemCount() {
        return recentAlerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView alertTitle, alertTime;
        ImageView alertIcon;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            alertTitle = itemView.findViewById(R.id.alertTitle);
            alertTime = itemView.findViewById(R.id.alertTime);
            alertIcon = itemView.findViewById(R.id.alertIcon);
        }
    }
}