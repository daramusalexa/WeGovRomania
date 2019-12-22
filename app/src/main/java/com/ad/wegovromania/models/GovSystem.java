package com.ad.wegovromania.models;

import com.ad.wegovromania.util.Constants;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class GovSystem {

    private String name;
    private String phone;
    private String email;
    private String website;
    private Constants.GovSystemsStatus status;
    private @ServerTimestamp Date timestamp;

    // Necessary for deserializing objects sent from Firestore
    public GovSystem() {
    }

    public GovSystem(String name, String phone, String email, String website) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.website = website;
        this.status = Constants.GovSystemsStatus.On;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Constants.GovSystemsStatus getStatus() {
        return status;
    }

    public void setStatus(Constants.GovSystemsStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "GovSystem{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}
