package com.seagate.bingo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.seagate.bingo.REF.RC_WAITING_ROOM;

public class MainActivity extends AppCompatActivity implements RoomUpdateListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener, OnInvitationReceivedListener, GameFragment.GameListener {


    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;
    private String mRoomId, mMyId;
    private ArrayList<String> mParticipantId;
    private int gamerType;
    public boolean isItHost;
    private int activePlayers;
    private GameFragment gameFragment;
    protected boolean directLaunch;
    private SharedPreferences generalSetting;
    private TextView score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSatusBar();
        setContentView(R.layout.activity_main);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);*/
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content,new SplashFragment());
        transaction.commit();
        gameFragment=new GameFragment();
        gameFragment.setBingoClickListener(this);
        /*setSupportActionBar(toolbar);*/
        startGoogleApi();
        buildProgressDialog();
        defineViews();
        generalSetting =getSharedPreferences(REF.GENERAL_SETTING,MODE_PRIVATE);
        setStartPoints();
        updateScore();

        /*helper = new DBHelper(this);*/

    }

    private void hideSatusBar() {
        /*View decorView=getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setStartPoints() {
        if (!generalSetting.contains(REF.PLAYER_POINTS_KEY)){
            SharedPreferences.Editor editor= generalSetting.edit();
            editor.putInt(REF.PLAYER_POINTS_KEY,0);
            editor.apply();
        }

    }


    private void defineViews() {
        Button sendInvite = (Button) findViewById(R.id.send);
        sendInvite.setOnClickListener(this);
        Button showPendingInvite = (Button)findViewById(R.id.show);
        showPendingInvite.setOnClickListener(this);
        Button quickPlay = (Button) findViewById(R.id.quick);
        quickPlay.setOnClickListener(this);
        findViewById(R.id.prize).setOnClickListener(this);
        score= (TextView) findViewById(R.id.points);
    }

    private void startGoogleApi() {
        Games.GamesOptions.Builder options = Games.GamesOptions.builder().setShowConnectingPopup
                (true);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API, options.build()).addScope(Games.SCOPE_GAMES)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        switch (requestCode) {
            case RC_SELECT_OPP:
                if (resultCode == RESULT_OK) {
                    handleInvitation(data);
                    gamerType = REF.GAME_HOST;
                    isItHost=true;
                }
                break;
            case RC_RECEIVE_INVITE:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (resultCode == RESULT_OK) {
                        if (bundle != null) {
                            Invitation invitation = bundle.getParcelable(Multiplayer.EXTRA_INVITATION);
                            isItHost=false;
                            if (invitation != null && invitation.getInvitationId() != null) {
                                acceptInvitation(invitation.getInvitationId());
                                gamerType = REF.GAME_GUEST;

                            }
                        }


                    }
                }
                break;
            case RC_WAITING_ROOM:
                if (resultCode == RESULT_OK) {
                    //start Game
                    Log.i("GAME", "GAME STARTED");
                    callGameFragment();
                }
                break;
            case REF.RC_CONNECTION_FAILED_FIX:
                if (resultCode==RESULT_OK){
                    mGoogleApiClient.connect();
                }
                break;

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

    private void buildProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait");
        progressDialog.setIndeterminate(true);


    }


    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        Log.i("GOOGLE PLAY GAMES","GOOGLE API CONNECTED");


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
        Log.i("GOOGLE PLAY GAMES","GOOGLE API CONNECTION SUSPENDED");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        String msg = connectionResult.toString();
        Log.i("GOOGLE PLAY GAMES","GOOGLE API CONNECTION FAILED");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REF.RC_CONNECTION_FAILED_FIX);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onRoomCreated(int i, Room room) {
        Log.i("GOOGLE PLAY GAMES","ROOM CREATED");
        if (i == GamesStatusCodes.STATUS_OK) {
            showWaitingRoom(room);
        }

    }



    private void showWaitingRoom(Room room) {
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        Log.i("GOOGLE PLAY GAMES","ROOM JOINED");
    }

    @Override
    public void onLeftRoom(int i, String s) {
        Log.i("GOOGLE PLAY GAMES","ROOM LEFT");
    }


    @Override
    public void onRoomConnected(int i, Room room) {
        Log.i("GOOGLE PLAY GAMES","ROOM CONNECTED");
        progressDialog.dismiss();
        mRoomId = room.getRoomId();
        mParticipantId = room.getParticipantIds();
        Toast.makeText(this, "Game started", Toast.LENGTH_LONG).show();

        String myId =room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        isItHost=myId.equals(mParticipantId.get(0));


        if (isItHost){
            gameFragment=new GameFragment();
            gameFragment.setBingoClickListener(this);
            ArrayList<Integer> values=createCallerArray();
            Bundle args=new Bundle();
            args.putIntegerArrayList(REF.CALLER_ARRAY_KEY,values);
            gameFragment.setArguments(args);
            Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient,getBytesFromList(values),mRoomId);


        }



    }

    private ArrayList<Integer> createCallerArray() {
        ArrayList<Integer> callerArray = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            callerArray.add(i + 1);
        }
        Collections.shuffle(callerArray);

        return callerArray;

    }

    private void updateRoom(Room room){
        if (room!=null){
            mParticipantId=room.getParticipantIds();
        }

        /*if (activePlayers<1){
            session.stopSoundPool();
            Toast.makeText(this,"players has left room",Toast.LENGTH_LONG).show();
        }*/

    }

    private byte[] getBytesFromList(ArrayList<Integer> values){
        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        DataOutputStream dataOutputStream=new DataOutputStream(byteArrayOutputStream);
        for (Integer i:values){
            try {
                dataOutputStream.writeInt(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteArrayOutputStream.toByteArray();
    }




    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        Log.i("GOOGLE PLAY GAMES","ON RTL MESSAGE RECEIVED");
        byte[] msg = realTimeMessage.getMessageData();
        ArrayList<Integer> msgList=getListFromBytes(msg);


        if (msgList.size()==1){
            //winner status
           gameFragment.makeLoser();
        }else {
            if (!isItHost){
                gameFragment=new GameFragment();
                gameFragment.setBingoClickListener(this);
                Bundle args=new Bundle();
                args.putIntegerArrayList(REF.CALLER_ARRAY_KEY,msgList);
                gameFragment.setArguments(args);

            }
        }


    }

    private ArrayList<Integer> getListFromBytes(byte [] bytes){
        ArrayList<Integer> values=new ArrayList<>();
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream=new DataInputStream(byteArrayInputStream);
        try {
            while (dataInputStream.available()>0){
                values.add(dataInputStream.readInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    @Override
    public void onRoomConnecting(Room room) {
        Log.i("GOOGLE PLAY GAMES","ROOM CONNECTING");
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Log.i("GOOGLE PLAY GAMES","ROOM AUTO MATCHING");
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        Log.i("GOOGLE PLAY GAMES","PEER INVITED");
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        Log.i("GOOGLE PLAY GAMES","PEER DECLINED");
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        Log.i("GOOGLE PLAY GAMES","PEER JOINED");
        activePlayers=activePlayers+list.size();
        updateRoom(room);

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        Log.i("GOOGLE PLAY GAMES","PEER LEFT");
        ArrayList<String> ids=room.getParticipantIds();
        activePlayers=activePlayers-list.size();
        if (activePlayers<1){
            gameFragment.allPlayersLeft();
        }
        updateRoom(room);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        Log.i("GOOGLE PLAY GAMES","ON CONNECTED TO ROOM");
        updateRoom(room);
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.i("GOOGLE PLAY GAMES","ON DISCONNECTED FROM ROOM");
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        Log.i("GOOGLE PLAY GAMES","PEER CONNECTED");
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        Log.i("GOOGLE PLAY GAMES","PEER DISCONNECTED");
        updateRoom(room);
    }

    @Override
    public void onP2PConnected(String s) {
        Log.i("GOOGLE PLAY GAMES","PEER 2 PEER CONNECTED");
    }

    @Override
    public void onP2PDisconnected(String s) {
        Log.i("GOOGLE PLAY GAMES","PEER 2 PEER DISCONNECTED");
    }

    private static final int RC_SELECT_OPP = 1000;
    private static final int RC_RECEIVE_INVITE = 2000;

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
                startActivityForResult(intent, RC_SELECT_OPP);
                break;
            case R.id.show:

                Intent intent1 = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                startActivityForResult(intent1, RC_RECEIVE_INVITE);
                break;
            case R.id.quick:
                startQuickPlay();
                directLaunch=true;

                /*startHostSession();
                showGame();*/

                break;
            case R.id.prize:
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),6000);

            /*case R.id.button2:
                Games.Achievements.unlock(mGoogleApiClient,"CgkIzaas_JANEAIQAQ");
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),6000);*/
           /* case R.id.call:
                ByteBuffer byteBuffer=ByteBuffer.allocate(4);
                byteBuffer.putInt(REF.WINNER_CODE);
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient,byteBuffer.array(),mRoomId);
                session.stopSoundPool();
                showMenu();
                winnerDialog.setMessage("You Win !");
                winnerDialog.show();
                *//*if (helper.fetchForWinner(cells)) {
                    winnerDialog.setMessage("you have won !!!!");
                    winnerDialog.show();
                    session.stopSoundPool();
                    ByteBuffer byteBuffer=ByteBuffer.allocate(4);
                    byteBuffer.putInt(REF.WINNER_CODE);
                    Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient,byteBuffer.array(),mRoomId);
                }*//*
                break;*/


        }
    }




    public void startQuickPlay() {
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 7;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());




    }

    private void callGameFragment() {
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

        if (directLaunch) {
            transaction.add(R.id.content,gameFragment);
        }else {
            transaction.replace(R.id.content,gameFragment);
        }
        transaction.addToBackStack("first");
        transaction.commit();
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        Intent intent1 = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
        startActivityForResult(intent1, RC_RECEIVE_INVITE);
    }

    @Override
    public void onInvitationRemoved(String s) {

    }


    @Override
    protected void onPause() {
        super.onPause();

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
    protected void onStop() {
        super.onStop();
        if (mRoomId!=null /*&& session!=null*/) {
            leaveRoom();
        }
        if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    protected void leaveRoom() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient!=null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
        }
        /*session.stopSoundPool();*/
    }

    @Override
    public void onBackPressed() {

        int count=getSupportFragmentManager().getBackStackEntryCount();
        if (count==0) {
            super.onBackPressed();
        }else {
            leaveRoom();
            getSupportFragmentManager().beginTransaction().remove(gameFragment).commit();
        }
    }

    @Override
    public void onWinning() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(REF.WINNER_CODE);
        Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, byteBuffer.array(), mRoomId);
        checkForAchievements();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void updateScore(){
        int points=generalSetting.getInt(REF.PLAYER_POINTS_KEY,0);
        score.setText("Score : "+points+" pt");
    }



    private void checkForAchievements() {
        int points=generalSetting.getInt(REF.PLAYER_POINTS_KEY,0);
        String productKey=null;
        if (points>100 && points<200){
            // win product a
            productKey= REF.PRODUCT_A_KEY;
        }else if (points>200 && points<300){
            // win product b
            productKey= REF.PRODUCT_B_KEY;
        }else if (points>300 && points<400){
            // win product c
            productKey= REF.PRODUCT_C_KEY;
        }else if (points>400 && points<500){
            // win product d
            productKey= REF.PRODUCT_D_KEY;
        }else if (points>500){
            // in product e
            productKey=REF.PRODUCT_E_KEY;
        }
        if (productKey!=null) {
            Games.Achievements.unlock(mGoogleApiClient, productKey);
        }

    }
}
