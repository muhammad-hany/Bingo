package com.seagate.bingo;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment implements Session.SessionListener, View.OnClickListener {


    private Session session;
    private TextView fileNameText;
    private Integer[] colNumbers, allNumbers;
    private ArrayList<Cell> cells;
    private DBHelper helper;
    private AlertDialog winnerDialog;
    private GridView textGrid;
    private GridAdapter mainAdapter;
    private GridAdapter textAdapter;
    private GameListener mListener;
    private GridView gridView;

    public GameFragment() {
        // Required empty public constructor
    }


    public interface GameListener {
        void onWinning();
    }

    public void setBingoClickListener(GameListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_game, container, false);
        helper = new DBHelper(getContext());
        ArrayList<Integer> callerArray = getArguments().getIntegerArrayList(REF.CALLER_ARRAY_KEY);
        startGuestSession(callerArray);
        defineGameViews(view);
        session.startGameVoices();
        return view;
    }


    public void startGuestSession(ArrayList<Integer> values) {

        session = new Session(getContext(), this, values);
        cells = session.getCells();
        colNumbers = new Integer[25];
        allNumbers = new Integer[75];

        int j = 0;
        for (Cell cell : cells) {
            colNumbers[j] = cell.getValue();
            j++;
        }

        for (int i = 1; i <= 75; i++) {
            allNumbers[i - 1] = i;
        }


    }


    public void startHostSession() {
        session = new Session(getContext(), this);
        cells = session.getCells();
        colNumbers = new Integer[25];
        allNumbers = new Integer[75];

        int j = 0;
        for (Cell cell : cells) {
            colNumbers[j] = cell.getValue();
            j++;
        }

        for (int i = 1; i <= 75; i++) {
            allNumbers[i - 1] = i;
        }


    }


    private void defineGameViews(View view) {
        fileNameText = (TextView) view.findViewById(R.id.filename);
        gridView = (GridView) view.findViewById(R.id.grid);
        mainAdapter = new GridAdapter(getContext(), colNumbers, R.layout.grid_item, REF.MAIN_GRID);
        gridView.setAdapter(mainAdapter);
        gridView.setNumColumns(5);
        gridView.setHorizontalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setVerticalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout button = (LinearLayout) view.findViewById(R.id.linear);
                TextView textView = (TextView) view.findViewById(R.id.questionBody);
                Cell cell = cells.get(GridAdapter.alterP(position));

                if (cell.isItCalled()) {
                    cell.setItClicked(!cell.isItClicked());
                    if (cell.isItClicked()) {
                        button.setBackgroundColor(Color.RED);
                        textView.setTextColor(Color.WHITE);
                    } else {
                        button.setBackgroundColor(Color.WHITE);
                        textView.setTextColor(Color.BLACK);
                    }
                    helper.updateCell(cell);
                }
                Log.v("tag", "value is" + cell.getValue() + " row id is " + cell.getCellId() + " column " + "number is " + cell.getCellColumn());
            }
        });

        textGrid = (GridView) view.findViewById(R.id.textGrid);
        textAdapter = new GridAdapter(getContext(), allNumbers, R.layout.text_frid_item, REF.TEXT_GRID);
        textGrid.setAdapter(textAdapter);
        textGrid.setHorizontalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        textGrid.setVerticalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        textGrid.setNumColumns(5);

        Button callForBingo = (Button) view.findViewById(R.id.call);
        callForBingo.setOnClickListener(this);
        dialogBuilder();

    }

    private void dialogBuilder() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Round was over")
                .setMessage("You Lost")
                .setCancelable(false)
                .setPositiveButton("Play again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainActivity) getActivity()).directLaunch = false;
                        ((MainActivity) getActivity()).startQuickPlay();
                        ((MainActivity) getActivity()).leaveRoom();
                        getFragmentManager().beginTransaction().remove(GameFragment.this).commit();
                    }
                })
                .setNegativeButton("Main Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getFragmentManager().beginTransaction().remove(GameFragment.this).commit();
                    }
                });
        winnerDialog = builder.create();
    }

    public void startSound() {
        session.startGameVoices();
    }

    public ArrayList<Integer> getCallerArray() {
        return session.getCalledArray();
    }


    @Override
    public void onPlaySound(String name, int value) {
        StringBuilder builder = new StringBuilder(name);
        builder.insert(1, " ");
        fileNameText.setText(builder.toString().toUpperCase());
        for (Cell cell : cells) {
            if (cell.getValue() == (value)) {
                cell.setItCalled(true);
                break;
            }
        }
        int index = GridAdapter.revertPForText(value - 1);
        LinearLayout layout = (LinearLayout) textGrid.getChildAt(index);
        TextView textView = (TextView) layout.findViewById(R.id.questionBody);
        layout.setBackgroundResource(android.R.color.holo_red_light);
        textView.setTextColor(Color.WHITE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call:
                SharedPreferences preferences=getActivity().getSharedPreferences(REF.GENERAL_SETTING, Context.MODE_PRIVATE);
                int points=preferences.getInt(REF.PLAYER_POINTS_KEY,0);

                if (helper.fetchForWinner(cells)) {
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putInt(REF.PLAYER_POINTS_KEY,points+10);
                    editor.apply();
                    ((MainActivity)getActivity()).updateScore();
                    makeWinner();
                }




        }
    }

    private void makeWinner() {
        winnerDialog.setMessage("you have won !!!!");
        winnerDialog.show();
        session.stopSoundPool();
        mListener.onWinning();
        ((MainActivity) getActivity()).isItHost = false;
    }

    public void makeLoser() {
        winnerDialog.setMessage("You lost !");
        winnerDialog.show();
        session.stopSoundPool();
        ((MainActivity) getActivity()).isItHost = false;
    }

    protected void allPlayersLeft() {
        if (!winnerDialog.isShowing()) {
            winnerDialog.setMessage("All players have left the room !");
            winnerDialog.setTitle("Oops");
            session.stopSoundPool();
            winnerDialog.show();

        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (session != null) session.stopSoundPool();
    }
}
