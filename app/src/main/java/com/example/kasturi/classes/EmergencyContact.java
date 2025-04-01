package com.example.kasturi.classes;

public class EmergencyContact {
    private String name;
    private String phone;

    public EmergencyContact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}