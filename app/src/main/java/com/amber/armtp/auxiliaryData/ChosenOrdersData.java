package com.amber.armtp.auxiliaryData;

public class ChosenOrdersData {
    private int id;
    private boolean isChecked;
    private final String status;

    public ChosenOrdersData(int id, boolean isChecked, String status) {
        this.id = id;
        this.isChecked = isChecked;
        this.status = status;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}