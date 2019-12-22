package com.ad.wegovromania.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ad.wegovromania.R;
import com.ad.wegovromania.models.Report;
import com.ad.wegovromania.ui.adapters.ReportRecyclerAdapter;
import com.ad.wegovromania.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActiveReportsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActiveReportsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActiveReportsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "Active Reports Frag.";
    private static boolean mAdmin = false;
    private static String mCity = null;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mFirebaseUser;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ReportRecyclerAdapter mReportRecyclerAdapter;
    private List<Report> mReports;
    private List<String> mReportIDs;

    public ActiveReportsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActiveReportsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActiveReportsFragment newInstance(String param1, String param2) {
        ActiveReportsFragment fragment = new ActiveReportsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mReports = new ArrayList<>();
        mReportIDs = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_reports, container, false);

        mFirebaseUser = mAuth.getCurrentUser();

        mProgressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.recyclerView);

        mProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mReportRecyclerAdapter = new ReportRecyclerAdapter(mReports);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mReportRecyclerAdapter);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUserInfo();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Set User City before loading reports
    public void setUserInfo() {
        // Get user info from database
        mFirestore.collection("Users").document(mFirebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    if (documentSnapshot.get(("admin")) != null) {
                        mAdmin = documentSnapshot.getBoolean("admin");
                    }
                    mCity = documentSnapshot.getString("city");
                    loadReports();
                }
            }
        });
    }

    // Load reports from Firestore
    public void loadReports() {
        mFirebaseUser = mAuth.getCurrentUser();
        // If user is admin get all pending reports
        if (mAdmin) {
            mFirestore.collection("Reports").whereEqualTo("status", Constants.ReportStatus.Pending).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        // Load reports
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mReports = task.getResult().toObjects(Report.class);
                            mReportIDs.add(document.getId());
                        }
                        mReportRecyclerAdapter.updateReports(mReports, mReportIDs);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }

                    Log.e(TAG, mReports.toString());
                }
            });
        }
        // If user is citizen get all pending reports
        else if (mCity == null) {
            mFirestore.collection("Reports").whereEqualTo("userId", mFirebaseUser.getUid()).whereEqualTo("status", Constants.ReportStatus.Pending).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        // Load reports
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mReports = task.getResult().toObjects(Report.class);
                            mReportIDs.add(document.getId());
                        }
                        mReportRecyclerAdapter.updateReports(mReports, mReportIDs);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    Log.e(TAG, mReports.toString());
                }
            });
            // If user is city get all pending reports
        } else {
            mFirestore.collection("Reports").whereEqualTo("city", mCity).whereEqualTo("status", Constants.ReportStatus.Pending).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        // Load reports
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mReports = task.getResult().toObjects(Report.class);
                            mReportIDs.add(document.getId());
                        }
                        mReportRecyclerAdapter.updateReports(mReports, mReportIDs);
                        mProgressBar.setVisibility(View.INVISIBLE);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }

                    Log.e(TAG, mReports.toString());
                }
            });
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
