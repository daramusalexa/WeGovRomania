package com.ad.wegovromania.models;

public class CityUser extends User {
    private String city;

    public CityUser() {
    }

    public CityUser(String firstName, String lastName, String phone, String city) {
        super(firstName, lastName, phone);
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "CityUser{" +
                "city='" + city + '\'' +
                '}';
    }
}
