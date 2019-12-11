package com.ad.wegovromania.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.ui.activities.ImageActivity;
import com.ad.wegovromania.util.Constants;
import com.ad.wegovromania.util.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private TextView mLocationTextView;
    private TextView mReportBodyTextView;
    private TextView mDateTextView;
    private ImageView[] mImageViews;

    public CustomInfoWindowAdapter(Context ctx) {
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.report_info_window, null);

        mLocationTextView = view.findViewById(R.id.locationTextView);
        mReportBodyTextView = view.findViewById(R.id.reportBodyTextView);
        mDateTextView = view.findViewById(R.id.dateTextView);
        mImageViews = new ImageView[Constants.REPORT_IMAGEVIEWS_NUMBER];
        mImageViews[0] = view.findViewById(R.id.imageView1);
        mImageViews[1] = view.findViewById(R.id.imageView2);
        mImageViews[2] = view.findViewById(R.id.imageView3);

        Report report = (Report) marker.getTag();

        LatLng location = null;
        if (report != null) {
            location = new LatLng(report.getLocation().getLatitude(), report.getLocation().getLongitude());
        }
        List<Address> addresses = Utils.getAdresses(location, context);
        String address = addresses.get(0).getAddressLine(0);
        mLocationTextView.setText(address);

        mReportBodyTextView.setText(report.getReportBody());
        long milliseconds = report.getTimestamp().getTime();
        String date = DateFormat.format("MM/dd/yyyy HH:mm", new Date(milliseconds)).toString();
        mDateTextView.setText(date);

        int i = 0;
        for (String string : report.getImages()) {
            Picasso.get().load(string).into(mImageViews[i++], new Callback() {
                @Override
                public void onSuccess() {
                    if (marker != null && marker.isInfoWindowShown()) {
                        // Toggle the marker's infoWindow
                        marker.hideInfoWindow();
                        marker.showInfoWindow();
                    }
                }
                @Override
                public void onError(Exception e) {
                }
            });
        }
        return view;
    }
}
