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
    private Integer[] colNumbers,allNumbers;
    private ArrayList<Cell> cells;
    private DBHelper helper;
    private AlertDialog alertDialog;
    private GridView textGrid;
    private GridAdapter mainAdapter;
    private GridAdapter textAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startSession();
        helper = new DBHelper(this);


    }

    private void defineViews() {
        fileNameText = (TextView) findViewById(R.id.filename);
        GridView gridView = (GridView) findViewById(R.id.grid);
        mainAdapter = new GridAdapter(this, colNumbers,R.layout.grid_item,REF.MAIN_GRID);
        gridView.setAdapter(mainAdapter);
        gridView.setNumColumns(5);
        gridView.setHorizontalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setVerticalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout button = (LinearLayout) view.findViewById(R.id.linear);
                TextView textView= (TextView) view.findViewById(R.id.textView);
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

        textGrid = (GridView) findViewById(R.id.textGrid);
        textAdapter =new GridAdapter(this,allNumbers,R.layout.text_frid_item,REF.TEXT_GRID);
        textGrid.setAdapter(textAdapter);
        textGrid.setHorizontalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        textGrid.setVerticalSpacing(GridView.STRETCH_SPACING_UNIFORM);
        textGrid.setNumColumns(5);

        Button callForBingo = (Button) findViewById(R.id.call);
        callForBingo.setOnClickListener(this);
        dialogBuilder();

    }

    private void startSession() {

        session = new Session(this, this);
        cells = session.getCells();
        colNumbers = new Integer[25];
        allNumbers=new Integer[75];

        int j = 0;
        for (Cell cell : cells) {
            colNumbers[j] = cell.getValue();
            j++;
        }

        for (int i=1;i<=75;i++){
            allNumbers[i-1]=i;
        }

        defineViews();
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
        if (id == R.id.newGame) {
            session.stopSoundPool();
            startSession();

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
        int index=GridAdapter.revertPForText(value-1);
        LinearLayout layout= (LinearLayout) textGrid.getChildAt(index);
        TextView textView= (TextView) layout.findViewById(R.id.textView);
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


    public String[] getTextForAdapter() {
        String[] textForAdapter=new String[75];
        for (int i=1;i<=75;i++){
            textForAdapter[i-1]=String.valueOf(GridAdapter.alterP(i));
        }
        return textForAdapter;
    }
}
