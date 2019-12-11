package com.ad.wegovromania.models;

public class CityUser extends User {
    private String city;
    private boolean enabled;

    public CityUser() {
    }

    public CityUser(String firstName, String lastName, String phone, String city) {
        super(firstName, lastName, phone);
        this.city = city;
        this.enabled = false;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "CityUser{" +
                "city='" + city + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
