package com.ad.wegovromania.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {

    private String firstName;
    private String lastName;
    private String phone;
    private boolean enabled;
    private @ServerTimestamp Date timestamp;

    // Necessary for deserializing objects sent from Firestore
    public User() {
    }

    public User(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.enabled = true;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", enabled=" + enabled +
                ", timestamp=" + timestamp +
                '}';
    }
}
