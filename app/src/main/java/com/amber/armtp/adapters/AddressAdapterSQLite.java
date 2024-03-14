package com.amber.armtp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amber.armtp.R;

public class AddressAdapterSQLite extends SimpleCursorAdapter {
    private final Context context;
    public AddressAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Cursor cursor = getCursor();
        TextView tvDescr = view.findViewById(R.id.ColContrAddrDescr);

        if (position % 2 != 0) {
            tvDescr.setBackgroundColor(context.getResources().getColor(R.color.gridViewFirstColor));
        } else {
            tvDescr.setBackgroundColor(context.getResources().getColor(R.color.gridViewSecondColor));
        }
        tvDescr.setText(cursor.getString(2));

        return view;
    }
}