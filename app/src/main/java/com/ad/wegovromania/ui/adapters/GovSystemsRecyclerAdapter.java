package com.ad.wegovromania.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.GovSystem;
import com.ad.wegovromania.ui.activities.GovSystemDetailsActivity;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GovSystemsRecyclerAdapter extends RecyclerView.Adapter<GovSystemsRecyclerAdapter.ViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private TextView mNameTextView;
    private TextView mDescriptionTextView;
    private TextView mInstitutionTextView;
    private Button mWebsiteButton;
    private Button mSendEmailButton;
    private TextView mStatusTextView;

    private List<GovSystem> mGovSystems;
    private List<String> mGovSystemsIDs;

    private boolean mAdmin = false;

    private static final String TAG = "GovSystems Rec. Adapter";

    public GovSystemsRecyclerAdapter(List<GovSystem> govSystems) {
        mGovSystems = govSystems;
    }

    @NonNull
    @Override
    public GovSystemsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gov_systems_item, parent, false);
        return new GovSystemsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GovSystemsRecyclerAdapter.ViewHolder holder, final int position) {
        // Fill cards with data
        String name = mGovSystems.get(position).getName();
        mNameTextView.setText(name);

        String description = mGovSystems.get(position).getDescription();
        mDescriptionTextView.setText(description);

        String institution = mGovSystems.get(position).getInstitution();
        mInstitutionTextView.setText(institution);

        String status = String.valueOf(mGovSystems.get(position).getStatus());
        mStatusTextView.setText(status);
        mStatusTextView.setTextColor(holder.itemView.getResources().getColor(R.color.colorDarkGreen));
        if(status.equals(Constants.GovSystemsStatus.Nefuncțional.toString())) {
            mStatusTextView.setTextColor(holder.itemView.getResources().getColor(R.color.colorDarkRed));
        }


        final String website = mGovSystems.get(position).getWebsite();
        if (website == null || website.isEmpty()) {
            mWebsiteButton.setVisibility(View.INVISIBLE);
        }

        // When the user clicks the Website Button
        mWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(website); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // When the user clicks the Send Email Button
        mSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), GovSystemDetailsActivity.class);
                intent.putExtra("GOV_SYSTEM_ID", mGovSystemsIDs.get(position));
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // If user is admin allow long press
        if(mAdmin) {
            // When admin presses one of the Gov Systems cards
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(holder.itemView.getContext(), GovSystemDetailsActivity.class);
                    intent.putExtra("GOV_SYSTEM_ID", mGovSystemsIDs.get(position));
                    intent.putExtra("USER_TYPE", "admin");
                    holder.itemView.getContext().startActivity(intent);
                    return true;
                }
            });
        }
}

    @Override
    public int getItemCount() {
        return mGovSystems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            mFirebaseUser = mAuth.getCurrentUser();

            mNameTextView = itemView.findViewById(R.id.nameTextView);
            mDescriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            mInstitutionTextView = itemView.findViewById(R.id.institutionTextView);
            mStatusTextView = itemView.findViewById(R.id.statusTextView);

            mWebsiteButton = itemView.findViewById(R.id.websiteButton);
            mSendEmailButton = itemView.findViewById(R.id.sendEmailButton);

            // Get user info from database
            if (mFirebaseUser != null) {
                mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            // If user is admin show Status Switch
                            if (documentSnapshot.getBoolean("admin") != null) {
                                mAdmin = true;
                                notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

        }
    }

    // Refresh fragment when something changes in the Recycler view
    public void updateGovSystems(List<GovSystem> govSystems, List<String> govSystemsIDsIDs) {
        mGovSystems = govSystems;
        Log.e(TAG, govSystems.toString());
        mGovSystemsIDs = govSystemsIDsIDs;
        notifyDataSetChanged();
    }
}