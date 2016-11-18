package com.seagate.bingo;

import android.content.Context;
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


    public GridAdapter(Context context,Integer [] colNumbers) {
        super(context, R.layout.grid_item,colNumbers);
        this.colNumbers=colNumbers;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.grid_item,null,false);
            holder=new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        String txt= String.valueOf(colNumbers[alterP(position)]);
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

    class ViewHolder{
        TextView textView;
    }

}
