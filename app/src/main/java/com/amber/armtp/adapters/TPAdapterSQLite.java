package com.amber.armtp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amber.armtp.R;

public class TPAdapterSQLite extends SimpleCursorAdapter {
    private final Context context;
    public TPAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
        super(context, layout, c, from, to, flag);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView tvDescr = view.findViewById(R.id.ColTPDescr);
        if (position % 2 != 0) {
            tvDescr.setBackgroundColor(context.getResources().getColor(R.color.gridViewFirstColor));
        } else {
            tvDescr.setBackgroundColor(context.getResources().getColor(R.color.gridViewSecondColor));
        }

        return view;
    }
}