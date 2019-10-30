package com.ad.wegovromania.models;

import com.ad.wegovromania.util.Constants;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;

public class Report {

    private GeoPoint location;
    private String type;
    private String city;
    private String reportBody;
    private ArrayList<String> images;
    private Constants.Status status;
    private String userId;
    private @ServerTimestamp Date timestamp;

    // Necessary for deserializing objects sent from Firestore
    public Report() {
    }

    public Report(GeoPoint location, String type, String city, String reportBody, String userId) {
        this.location = location;
        this.type = type;
        this.city = city;
        this.reportBody = reportBody;
        this.status = Constants.Status.Pending;
        this.userId = userId;
        this.images = new ArrayList<>();
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getReportBody() {
        return reportBody;
    }

    public void setReportBody(String reportBody) {
        this.reportBody = reportBody;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public Constants.Status getStatus() {
        return status;
    }

    public void setStatus(Constants.Status status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Report{" +
                "location=" + location +
                ", type='" + type + '\'' +
                ", city='" + city + '\'' +
                ", reportBody='" + reportBody + '\'' +
                ", images=" + images +
                ", status=" + status +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
