package com.ad.wegovromania.models;

import com.ad.wegovromania.util.Constants;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class GovSystem {

    private String name;
    private String description;
    private String institution;
    private String email;
    private String website;
    private Constants.GovSystemsStatus status;
    private @ServerTimestamp Date timestamp;

    // Necessary for deserializing objects sent from Firestore
    public GovSystem() {
    }

    public GovSystem(String name, String description, String institution, String email, String website) {
        this.name = name;
        this.description = description;
        this.institution = institution;
        this.email = email;
        this.website = website;
        this.status = Constants.GovSystemsStatus.Funcțional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
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
                ", description='" + description + '\'' +
                ", institution='" + institution + '\'' +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}
