package com.seagate.bingo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener, View.OnClickListener, OnInvitationReceivedListener {


    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;
    private String mRoomId,mMyId;
    private  ArrayList<Participant> mParticipantId;
    private EditText editText;
    private TextView textView;
    private Button button;
    private int gamerType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startGoogleApi();


        /*editText= (EditText) findViewById(R.id.editText);
        editText.setVisibility(View.INVISIBLE);
        textView= (TextView) findViewById(R.id.t1);
        textView.setVisibility(View.INVISIBLE);
        button= (Button) findViewById(R.id.sendMsg);
        button.setVisibility(View.INVISIBLE);
        button.setOnClickListener(this);*/


    }



    /*void createRoom() {
        Bundle bundle=RoomConfig.createAutoMatchCriteria(1,1,0);

        RoomConfig.Builder configBuilder = RoomConfig.builder(this);
        configBuilder.setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .setAutoMatchCriteria(bundle);
        Games.RealTimeMultiplayer.create(mGoogleApiClient,configBuilder.build());

        *//*Intent intent=Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient,1,3);
        startActivityForResult(intent,1000);*//*

    }*/

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) mGoogleApiClient.connect();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        switch (requestCode) {
            case SELECT_OPP:
                if (resultCode == RESULT_OK) {
                    handleInvitation(data);
                    gamerType=REF.GAME_HOST;
                }
                break;
            case RECEIVE_INVITE:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (resultCode == RESULT_OK) {
                        if (bundle != null) {
                            Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                            if (invitation != null && invitation.getInvitationId() != null) {
                                acceptInvitation(invitation.getInvitationId());
                                gamerType=REF.GAME_GUEST;
                            }
                        }
                    }
                }
                break;
            case RC_WAITING_ROOM:
                if (resultCode==RESULT_OK){
                    //start Game
                    Log.i("GAME","GAME STARTED");
                    progressDialog.show();


                }
        }
    }

    private void handleInvitation(Intent data) {
        ArrayList<String> invitess = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        RoomConfig.Builder configBuilder = RoomConfig.builder(this);
        configBuilder.setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this)
                .addPlayersToInvite(invitess);

        int minPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        Bundle autoMatch = null;
        if (minPlayers > 0 || maxPlayers > 0) {
            autoMatch = RoomConfig.createAutoMatchCriteria(minPlayers, maxPlayers, 0);
        }
        if (autoMatch != null) {
            configBuilder.setAutoMatchCriteria(autoMatch);
        }
        Games.RealTimeMultiplayer.create(mGoogleApiClient, configBuilder.build());


    }

    private void startGoogleApi() {
        Games.GamesOptions.Builder options = Games.GamesOptions.builder().setShowConnectingPopup(true);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API, options.build()).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up textView, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.newGame) {


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        /*if (bundle!=null){
            Invitation invitation=bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (invitation!=null && invitation.getInvitationId()!=null){
                acceptInvitation(invitation.getInvitationId());
            }
        }*/
    }

    private void acceptInvitation(String invitationId) {
        RoomConfig.Builder builder = RoomConfig.builder(this)
                .setInvitationIdToAccept(invitationId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        Games.RealTimeMultiplayer.join(mGoogleApiClient, builder.build());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        String msg = connectionResult.toString();
        Log.i("GAME", "connection failed");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 5555);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRoomCreated(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            //error
        }

        showWaitingRoom(room);
    }

    static final int RC_WAITING_ROOM = 2555;

    private void showWaitingRoom(Room room) {
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        progressDialog.show();

    }

    @Override
    public void onLeftRoom(int i, String s) {

    }


    @Override
    public void onRoomConnected(int i, Room room) {
        progressDialog.dismiss();
        mRoomId=room.getRoomId();
        mParticipantId=room.getParticipants();
        Toast.makeText(this,"Game started",Toast.LENGTH_LONG).show();

        editText.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);

        startGameFragment();


    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        byte[] msg=realTimeMessage.getMessageData();
        String text="";
        try {
            text = new String(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        textView.setText(text);

    }

    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {

    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {

    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    private static final int SELECT_OPP = 1000;
    private static final int RECEIVE_INVITE = 2000;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //new Game
            case R.id.send:
                // send invitation
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent
                        (mGoogleApiClient, 1, 1);
                startActivityForResult(intent, SELECT_OPP);
                break;
            case R.id.show:
                if (!mGoogleApiClient.isConnected()) {

                }
                Intent intent1 = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                startActivityForResult(intent1, RECEIVE_INVITE);
                break;
            case R.id.quick:
                /*startQuickPlay();*/
                startGameFragment();
                break;
            case R.id.sendMsg:
                byte[] msg=editText.getText().toString().getBytes();
                if (msg.length!=0) {
                    Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, msg,
                            mRoomId);
                }
        }
    }



    private void startGameFragment(){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        GameFragment gameFragment=new GameFragment();
        Bundle bundle=new Bundle();
        bundle.putInt(REF.GAMER_TYPE_KEY,gamerType);
        gameFragment.setArguments(bundle);
        transaction.add(R.id.content_mainm,gameFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void startQuickPlay() {
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        Intent intent1 = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
        startActivityForResult(intent1, RECEIVE_INVITE);
    }

    @Override
    public void onInvitationRemoved(String s) {

    }
}
