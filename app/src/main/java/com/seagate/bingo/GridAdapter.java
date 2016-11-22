package com.seagate.bingo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Muhammad Workstation on 16/11/2016.
 */

public class GridAdapter extends ArrayAdapter<Integer> {

    private Integer[] colNumbers;
    int resourceId,type;
    ViewHolder holder = null;

    public GridAdapter(Context context,Integer [] colNumbers,int resourceId,int type) {
        super(context, resourceId,colNumbers);
        this.colNumbers=colNumbers;
        this.resourceId=resourceId;
        this.type=type;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(resourceId,null,false);
            holder=new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        String txt=null;
        if (type==REF.MAIN_GRID) {
             txt = String.valueOf(colNumbers[alterP(position)]);

        }else {
            try {
                int index=alterPForText(position);
                txt=String.valueOf(colNumbers[index]);
            }catch (ArrayIndexOutOfBoundsException e){
                Log.i("asdasd","asdasd");
            }

        }
        holder.textView.setText(String.valueOf(txt));
        return convertView;
    }




    public static int revertP(int alterP){
        int position=-1;
        int columnsCounter=0;
        int counter=0;

        for (int i=0 ; i<5;i++){
            for (int j=0;j<5;j++){

                if (counter==alterP) {
                    position = (alterP*5)-(24*columnsCounter);
                    return position;
                }
                counter++;


            }
            columnsCounter++;
        }


        return position;

    }
    public static int alterP(int position){
        //this method to invert grid every column to be row
        int myPosition=-1;
        int columnsCounter;
        int counter=0;

        for (int i=0 ; i<5;i++){
            columnsCounter=0;
            for (int j=0;j<5;j++){

                if (counter==position) {
                    myPosition = ((position - columnsCounter) / 5) + (columnsCounter * 5);
                    return myPosition;
                }
                counter++;
                columnsCounter++;

            }
        }


        return myPosition;
    }

    public static int alterPForText(int position){
        //this method to invert grid every column to be row
        int myPosition=-1;
        int columnsCounter;
        int counter=0;

        for (int i=0 ; i<15;i++){
            columnsCounter=0;
            for (int j=0;j<5;j++){

                if (counter==position) {
                    myPosition = ((position - columnsCounter) / 5) + (columnsCounter * 15);
                    return myPosition;
                }
                counter++;
                columnsCounter++;

            }
        }


        return myPosition;
    }

    public static int revertPForText(int alterP){
        int position=-1;
        int columnsCounter=0;
        int counter=0;

        for (int i=0 ; i<5;i++){
            for (int j=0;j<15;j++){

                if (counter==alterP) {
                    position = (alterP*5)-(74*columnsCounter);
                    return position;
                }
                counter++;


            }
            columnsCounter++;
        }


        return position;

    }



    class ViewHolder{
        TextView textView;
    }

}
