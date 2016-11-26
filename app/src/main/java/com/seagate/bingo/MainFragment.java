package com.seagate.bingo;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;


public class MainFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;
    private String mRoomId,mMyId;
    private ArrayList<Participant> mParticipantId;
    private EditText editText;
    private TextView textView;
    private Button button;
    private int gamerType;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startGoogleApi();
        buildProgressDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main, container, false);
        defineViews(view);
        return view ;
    }

    private void defineViews(View view) {
        Button sendInvite = (Button) view.findViewById(R.id.send);
        sendInvite.setOnClickListener(this);
        Button showPendingInvite = (Button) view.findViewById(R.id.show);
        showPendingInvite.setOnClickListener(this);
        Button quickPlay = (Button) view.findViewById(R.id.quick);
        quickPlay.setOnClickListener(this);
    }

    private void startGoogleApi() {
        Games.GamesOptions.Builder options = Games.GamesOptions.builder().setShowConnectingPopup(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Games.API, options.build()).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    private void buildProgressDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("please wait");
        progressDialog.setIndeterminate(true);


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!mGoogleApiClient.isConnected()) mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
