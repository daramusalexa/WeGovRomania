package com.ad.wegovromania.ui.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.ui.activities.ImageActivity;
import com.ad.wegovromania.ui.activities.ReportDetailsActivity;
import com.ad.wegovromania.util.Constants;
import com.ad.wegovromania.util.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class ReportRecyclerAdapter extends RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private TextView mLocationTextView;
    private TextView mReportBodyTextView;
    private TextView mDateTextView;
    private ImageView[] mImageViews;
    private TextView mResolutionTextView;
    private TextView mResolutionTagTextView;

    private List<Report> mReports;
    private List<String> mReportIDs;

    private boolean mAdmin = false;
    private boolean mCity = false;

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

        // Load images into imageViews
        int counter = 0;
        int i = 0;
        for (String string : mReports.get(position).getImages()) {
            Glide.with(holder.itemView.getContext()).load(string).into(mImageViews[i++]);
            counter++;
        }

        // Hide imageViews that don't have images
        for(int j = 2; j > counter-1; j--) {
            mImageViews[j].setVisibility(View.INVISIBLE);
        }

        String resolution = mReports.get(position).getResolution();
        mResolutionTextView.setText(resolution);
        // Hide resolution tag if empty
        if(TextUtils.isEmpty(resolution)) {
            mResolutionTagTextView.setVisibility(View.GONE);
        }

        if(mAdmin || mCity) {
            // When user presses one of the Reports cards
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(holder.itemView.getContext(), ReportDetailsActivity.class);
                    intent.putExtra("REPORT_ID", mReportIDs.get(position));
                    holder.itemView.getContext().startActivity(intent);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mReports.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            mFirebaseUser = mAuth.getCurrentUser();

            mLocationTextView = itemView.findViewById(R.id.locationTextView);
            mReportBodyTextView = itemView.findViewById(R.id.reportBodyTextView);
            mDateTextView = itemView.findViewById(R.id.dateTextView);
            mImageViews = new ImageView[Constants.REPORT_IMAGEVIEWS_NUMBER];
            mImageViews[0] = itemView.findViewById(R.id.imageView1);
            mImageViews[1] = itemView.findViewById(R.id.imageView2);
            mImageViews[2] = itemView.findViewById(R.id.imageView3);
            mResolutionTextView = itemView.findViewById(R.id.resolutionTextView);
            mResolutionTagTextView = itemView.findViewById(R.id.resolutionTagTextView);

            // Get user info from database
            if (mFirebaseUser != null) {
                mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            // If user city is not null show Add Report Button
                            String city = documentSnapshot.getString("city");
                            if (city != null) {
                                mCity = true;
                                notifyDataSetChanged();
                            }
                            // If user is admin show Users Button
                            if (documentSnapshot.getBoolean("admin") != null) {
                                mAdmin = true;
                                notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

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

    // Refresh fragment when something changes in the Recycler view
    public void updateReports(List<Report> reports, List<String> reportIDs) {
        mReports = reports;
        mReportIDs = reportIDs;
        notifyDataSetChanged();
    }
}
