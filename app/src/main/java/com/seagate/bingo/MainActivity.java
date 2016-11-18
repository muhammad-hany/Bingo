package com.seagate.bingo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, View.OnClickListener {
    private Button[] buttons;
    private Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16, b17, b18, b19, b20, b21,
            b22, b23, b24, b25, refresh;
    private Session session;
    private TextView fileNameText;
    private Integer[] colNumbers;
    private ArrayList<Cell> cells;
    private DBHelper helper;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        defineViews();
        helper = new DBHelper(this);


    }

    private void defineViews() {
        fileNameText = (TextView) findViewById(R.id.filename);
        startSession();

        GridView gridView = (GridView) findViewById(R.id.grid);
        GridAdapter adapter = new GridAdapter(this, colNumbers);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(5);
        gridView.setHorizontalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setVerticalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout button = (LinearLayout) view.findViewById(R.id.linear);
                Cell cell = cells.get(GridAdapter.alterP(position));
                cell.setItClicked(!cell.isItClicked());
                if (cell.isItClicked()){
                    button.setBackgroundColor(Color.RED);
                }else {
                    button.setBackgroundColor(Color.WHITE);
                }
                helper.updateCell(cell);
                Log.v("tag", "value is" + cell.getValue() + " row id is " + cell.getCellId() + " column " + "number is " + cell.getCellColumn());

            }
        });



        Button callForBingo = (Button) findViewById(R.id.call);
        callForBingo.setOnClickListener(this);

        dialogBuilder();


    }

    private void startSession() {
        session = new Session(this, this);
        cells = session.getCells();
        colNumbers = new Integer[25];


        int j = 0;
        for (Cell cell : cells) {
            colNumbers[j] = cell.getValue();
            j++;
        }
        /*for (int i = 0; i < 25; i++) {
            if (i < 5) {
                //B
                buttons[i].setText(String.valueOf(session.getBCol().get(j)));
                colNumbers[i]=session.getBCol().get(j);
            } else if (i >= 5 && i < 10) {
                //i
                buttons[i].setText(String.valueOf(session.getICol().get(j)));
                colNumbers[i]=session.getICol().get(j);

            } else if (i >= 10 && i < 15) {
                //n
                buttons[i].setText(String.valueOf(session.getNCol().get(j)));
                colNumbers[i]=session.getNCol().get(j);
            } else if (i >= 15 && i < 20) {
                //g
                buttons[i].setText(String.valueOf(session.getGCol().get(j)));
                colNumbers[i]=session.getGCol().get(j);
            } else if (i >= 20 && i < 25) {
                //o
                buttons[i].setText(String.valueOf(session.getOCol().get(j)));
                colNumbers[i]=session.getOCol().get(j);
            }
            j++;
            if (j > 5) j = 0;
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up textView, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true);
        alertDialog=builder.create();
    }

    @Override
    protected void onPause() {
        super.onPause();
        session.stopSoundPool();
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


}
