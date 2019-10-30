package com.ad.wegovromania.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Date;
import java.util.List;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private TextView mLocationTextView;
    private TextView mReportBodyTextView;
    private TextView mDateTextView;

    public CustomInfoWindowAdapter(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.report_info_window, null);

        mLocationTextView = view.findViewById(R.id.locationTextView);
        mReportBodyTextView = view.findViewById(R.id.reportBodyTextView);
        mDateTextView = view.findViewById(R.id.dateTextView);

        Report report = (Report) marker.getTag();

        LatLng location = new LatLng(report.getLocation().getLatitude(), report.getLocation().getLongitude());
        List<Address> addresses = Utils.getAdresses(location, context);
        String address = addresses.get(0).getAddressLine(0);
        mLocationTextView.setText(address);
        Log.e("hel", report.toString());

        mReportBodyTextView.setText(report.getReportBody());
        long milliseconds = report.getTimestamp().getTime();
        String date = DateFormat.format("MM/dd/yyyy HH:mm", new Date(milliseconds)).toString();
        mDateTextView.setText(date);

        return view;
    }
}
