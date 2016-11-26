package com.seagate.bingo;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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


public class GameFragment extends Fragment implements View.OnClickListener, Session.SessionListener {

    private Session session;
    private TextView fileNameText;
    private Integer[] colNumbers, allNumbers;
    private ArrayList<Cell> cells;
    private DBHelper helper;
    private AlertDialog alertDialog;
    private GridView textGrid;
    private GridAdapter mainAdapter;
    private GridAdapter textAdapter;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new DBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_game, container, false);
        startSession(view);
        return view;
    }



    private void defineViews(View view) {
        fileNameText = (TextView) view.findViewById(R.id.filename);
        GridView gridView = (GridView) view.findViewById(R.id.grid);
        mainAdapter = new GridAdapter(getContext(), colNumbers, R.layout.grid_item, REF.MAIN_GRID);
        gridView.setAdapter(mainAdapter);
        gridView.setNumColumns(5);
        gridView.setHorizontalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setVerticalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout button = (LinearLayout) view.findViewById(R.id.linear);
                TextView textView = (TextView) view.findViewById(R.id.textView);
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

    private void startSession(View view) {

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

        defineViews(view);
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
        TextView textView = (TextView) layout.findViewById(R.id.textView);
        layout.setBackgroundResource(android.R.color.holo_red_light);
        textView.setTextColor(Color.WHITE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call:
                if (helper.fetchForWinner(cells)) {
                    alertDialog.setMessage("you have won !!!!");
                    alertDialog.show();
                    session.stopSoundPool();
                }
        }
    }

    private void dialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(true);
        alertDialog = builder.create();
    }
}
