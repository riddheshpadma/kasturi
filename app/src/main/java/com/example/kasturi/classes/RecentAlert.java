package com.example.kasturi.classes;

public class RecentAlert {
    private String title;
    private String time;
    private int iconResId;

    public RecentAlert(String title, String time, int iconResId) {
        this.title = title;
        this.time = time;
        this.iconResId = iconResId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public int getIconResId() {
        return iconResId;
    }
}