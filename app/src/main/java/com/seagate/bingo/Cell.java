package com.seagate.bingo;

/**
 * Created by Muhammad Workstation on 11/11/2016.
 */

public class Cell {

    private int cellId;
    private int cellColumn;
    private int value;
    private boolean isItClicked=false,isItCalled=false;

    public Cell(int cellId, int cellColumn ,int value,boolean isItClicked,boolean isItCalled){
        this.cellId =cellId;
        this.cellColumn =cellColumn;
        this.value=value;
        this.isItClicked=isItClicked;
        this.isItCalled=isItCalled;
    }

    public boolean isItClicked() {
        return isItClicked;
    }

    public void setItClicked(boolean itClicked) {
        isItClicked = itClicked;
    }

    public boolean isItCalled() {
        return isItCalled;
    }

    public void setItCalled(boolean itCalled) {
        isItCalled = itCalled;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getCellColumn() {
        return cellColumn;
    }

    public void setCellColumn(int cellColumn) {
        this.cellColumn = cellColumn;
    }
}
