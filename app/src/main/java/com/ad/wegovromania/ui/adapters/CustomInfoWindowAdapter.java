package com.ad.wegovromania.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.util.Constants;
import com.ad.wegovromania.util.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private TextView mLocationTagTextView;
    private TextView mLocationTextView;
    private TextView mReportBodyTagTextView;
    private TextView mReportBodyTextView;
    private TextView mDateTextView;
    private LinearLayout mImageViewsLinearLayout;
    private ImageView[] mImageViews;
    private TextView mResolutionTagTextView;
    private TextView mResolutionTextView;


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

        mLocationTagTextView = view.findViewById(R.id.locationTagTextView);
        mLocationTextView = view.findViewById(R.id.locationTextView);
        mReportBodyTagTextView = view.findViewById(R.id.reportBodyTagTextView);
        mReportBodyTextView = view.findViewById(R.id.reportBodyTextView);
        mDateTextView = view.findViewById(R.id.dateTextView);
        mImageViewsLinearLayout = view.findViewById(R.id.imageViewsLinearLayout);
        mImageViews = new ImageView[Constants.REPORT_IMAGEVIEWS_NUMBER];
        mImageViews[0] = view.findViewById(R.id.imageView1);
        mImageViews[1] = view.findViewById(R.id.imageView2);
        mImageViews[2] = view.findViewById(R.id.imageView3);
        mResolutionTagTextView = view.findViewById(R.id.resolutionTagTextView);
        mResolutionTextView = view.findViewById(R.id.resolutionTextView);


        Report report = (Report) marker.getTag();
        if(report != null) {
            LatLng location = new LatLng(report.getLocation().getLatitude(), report.getLocation().getLongitude());
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
                        if (marker.isInfoWindowShown()) {
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

            String resolution = report.getResolution();
            mResolutionTextView.setText(resolution);
            // Hide resolution tag if empty
            if(TextUtils.isEmpty(resolution)) {
                mResolutionTagTextView.setVisibility(View.GONE);
            }
        }
        else {
            mLocationTagTextView.setText(marker.getTitle());
            List<Address> addresses = Utils.getAdresses(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), context);
            String address = addresses.get(0).getAddressLine(0);
            mLocationTextView.setText(address);

            mReportBodyTagTextView.setVisibility(View.GONE);
            mResolutionTagTextView.setVisibility(View.GONE);
            mResolutionTextView.setVisibility(View.GONE);
            mDateTextView.setVisibility(View.GONE);
            mImageViewsLinearLayout.setVisibility(View.GONE);
        }
        return view;
    }
}