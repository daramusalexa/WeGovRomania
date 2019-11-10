package com.ad.wegovromania.ui.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.ui.activities.ImageActivity;
import com.ad.wegovromania.ui.activities.ReportDetailsActivity;
import com.ad.wegovromania.util.Constants;
import com.ad.wegovromania.util.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class ReportRecyclerAdapter extends RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder> {

    private TextView mLocationTextView;
    private TextView mReportBodyTextView;
    private TextView mDateTextView;
    private ImageView[] mImageViews;

    private List<Report> mReports;
    private List<String> mReportIDs;

    private static final String TAG = "Report Recycler Adapter";

    public ReportRecyclerAdapter(List<Report> reports) {
        mReports = reports;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reports_item, parent, false);
        return new ViewHolder(view);
    }

    // Fill card with data from Firestore
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        // Create LatLng from GeoPoint
        LatLng location = new LatLng(mReports.get(position).getLocation().getLatitude(), mReports.get(position).getLocation().getLongitude());
        List<Address> addresses = Utils.getAdresses(location, holder.itemView.getContext());
        String address = addresses.get(0).getAddressLine(0);
        mLocationTextView.setText(address);

        String reportBody = mReports.get(position).getReportBody();
        mReportBodyTextView.setText(reportBody);

        long milliseconds = mReports.get(position).getTimestamp().getTime();
        String date = DateFormat.format("MM/dd/yyyy HH:mm", new Date(milliseconds)).toString();
        mDateTextView.setText(date);

        int i = 0;
        for (String string : mReports.get(position).getImages()) {
            Glide.with(holder.itemView.getContext()).load(string).into(mImageViews[i++]);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(holder.itemView.getContext(), ReportDetailsActivity.class);
               intent.putExtra("REPORT_ID", mReportIDs.get(position));
               holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReports.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            mLocationTextView = itemView.findViewById(R.id.locationTextView);
            mReportBodyTextView = itemView.findViewById(R.id.reportBodyTextView);
            mDateTextView = itemView.findViewById(R.id.dateTextView);
            mImageViews = new ImageView[Constants.REPORT_IMAGEVIEWS_NUMBER];
            mImageViews[0] = itemView.findViewById(R.id.imageView1);
            mImageViews[1] = itemView.findViewById(R.id.imageView2);
            mImageViews[2] = itemView.findViewById(R.id.imageView3);

            // When user clicks on the Image Views
            for (final ImageView imageView : mImageViews) {
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Send bitmap from imageView to the Image Activity
                        imageView.buildDrawingCache();
                        Bitmap image = imageView.getDrawingCache();

                        Bundle extras = new Bundle();
                        extras.putParcelable("IMAGE", image);

                        Intent intent = new Intent(itemView.getContext(), ImageActivity.class);
                        intent.putExtras(extras);
                        itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
    }

    public void updateReports(List<Report> reports, List<String> reportIDs) {
        mReports = reports;
        mReportIDs = reportIDs;
        notifyDataSetChanged();
    }
}
