package com.ad.wegovromania.ui.adapters;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.CityUser;
import com.ad.wegovromania.models.User;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;

    private TextView mNameTextView;
    private TextView mCityTextView;
    private TextView mPhoneTextView;
    private TextView mRegisterDateTextView;
    private Switch mStatusSwitch;

    private List<CityUser> mUsers;
    private List<String> mUserIDs;

    private static final String TAG = "User Recycler Adapter";

    public UserRecyclerAdapter(List<CityUser> users) {
        mUsers = users;
    }

    @NonNull
    @Override
    public UserRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_item, parent, false);
        return new UserRecyclerAdapter.ViewHolder(view);
    }

    // Fill card with data from Firestore
    @Override
    public void onBindViewHolder(@NonNull final UserRecyclerAdapter.ViewHolder holder, final int position) {
        // Fill cards with data
        String firstName = mUsers.get(position).getFirstName();
        String lastName = mUsers.get(position).getLastName();
        mNameTextView.setText(String.format("%s %s", firstName, lastName));

        String city = mUsers.get(position).getCity();
        if(city != null) {
            mCityTextView.setText(city);
        }

        String phone = mUsers.get(position).getPhone();
        mPhoneTextView.setText(phone);

        long milliseconds = mUsers.get(position).getTimestamp().getTime();
        String date = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        mRegisterDateTextView.setText(date);

        // If user enabled
        boolean enabled = mUsers.get(position).isEnabled();
        if(enabled) {
            mStatusSwitch.setChecked(true);
        }

        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                setUser(mUsers.get(position), mUserIDs.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();
            mFirebaseUser = mAuth.getCurrentUser();

            mNameTextView = itemView.findViewById(R.id.nameTextView);
            mCityTextView = itemView.findViewById(R.id.cityTextView);
            mPhoneTextView = itemView.findViewById(R.id.phoneTextView);
            mRegisterDateTextView = itemView.findViewById(R.id.registerDateTextView);
            mStatusSwitch = itemView.findViewById(R.id.statusSwitch);
        }
    }

    // Set enabled / disabled
    public void setUser(User user, String userID) {
        user.setEnabled(!user.isEnabled());
        mFirestore.collection("Users").document(userID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    // Refresh fragment when something changes in the Recycler view
    public void updateUsers(List<CityUser> users, List<String> userIDs) {
        mUsers = users;
        mUserIDs = userIDs;
        notifyDataSetChanged();
    }
}
