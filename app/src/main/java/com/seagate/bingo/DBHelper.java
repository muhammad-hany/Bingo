package com.seagate.bingo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Muhammad Workstation on 17/11/2016.
 */

public class DBHelper extends SQLiteOpenHelper {


    private static final String TEXT_TYPE = " BOOLEAN";
    private static final String COMMA_SEP = ",";
    private static final String MAKE_NEW_DB = "CREATE TABLE " + REF.TABLE_NAME + " (" +
            REF.ID + "  INTEGER PRIMARY KEY" + COMMA_SEP +
            REF.COL_1 + TEXT_TYPE + COMMA_SEP +
            REF.COL_2 + TEXT_TYPE + COMMA_SEP +
            REF.COL_3 + TEXT_TYPE + COMMA_SEP +
            REF.COL_4 + TEXT_TYPE + COMMA_SEP +
            REF.COL_5 + TEXT_TYPE + " )";

    private SQLiteDatabase database;


    public DBHelper(Context context) {
        super(context, REF.DB_NAME, null, 1);
        database = this.getWritableDatabase();
        makeStandardValues(database);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MAKE_NEW_DB);
    }

    public void makeStandardValues(SQLiteDatabase db) {
        db.execSQL("delete from " + REF.TABLE_NAME);
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < 5; i++) {
            String colName;
            for (int j = 1; j <= 5; j++) {
                colName = "col_" + String.valueOf(j);
                contentValues.put(colName, false);
            }
            db.insert(REF.TABLE_NAME, null, contentValues);
            contentValues.clear();
        }


    }

    public void updateCell(Cell cell){
        ContentValues contentValues=new ContentValues();
        contentValues.put("col_"+cell.getCellColumn(),true);
        database.update(REF.TABLE_NAME,contentValues, REF.ID+" = ?",new String[]{String.valueOf(cell
                .getCellId
                ())});
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }




    public boolean fetchForWinner(ArrayList<Cell> cells){
        ArrayList<Cell> winnerCells=new ArrayList<>();
        Cursor cursor=database.query(REF.TABLE_NAME,null,null,null,null,null,
                null);
        int entry;
        int count=0;
        Cell cell;
        ArrayList <Integer> rowValues=new ArrayList<>();
        while (cursor.moveToNext()){
            entry = -1;
            rowValues.clear();
            winnerCells.clear();
            for (int i=1 ; i<=5;i++) {

                entry = cursor.getInt(cursor.getColumnIndex("col_" + i));
                cell=cells.get(GridAdapter.revertP(count));
                if (entry == 0) {
                    break;
                }else if (entry==1 && cell.isItCalled() && cell.isItClicked()){
                    rowValues.add(1);
                    winnerCells.add(cell);
                }
                count++;
            }
            if (rowValues.size()==5 && winnerCells.size()==5){
                return true;
            }

        }
        count=0;

        for (int i=1;i<=5;i++){
            rowValues.clear();
            winnerCells.clear();
            String colName="col_"+i;
            cursor=database.query(REF.TABLE_NAME,new String[]{colName},null,null,null,null,null);
            while (cursor.moveToNext()){

                entry=cursor.getInt(cursor.getColumnIndex(colName));
                cell=cells.get(count);
                if (entry==0){
                    break;
                }else if (entry==1 && cell.isItCalled() && cell.isItClicked()){
                    rowValues.add(1);
                    winnerCells.add(cell);
                }
                if (rowValues.size()==5 && winnerCells.size()==5){
                    return true;
                }
                count++;
            }
        }

        cursor.close();

        return false;
    }
}
