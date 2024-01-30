package com.amber.armtp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amber.armtp.R;
public class ContractorAdapterSQLite extends SimpleCursorAdapter {
    public ContractorAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Cursor cursor = getCursor();
        String resDescr = cursor.getString(cursor.getColumnIndex("DESCR"));

        TextView tvDescr = view.findViewById(R.id.ColContrDescr);
        tvDescr.setText(resDescr);
        return view;
    }
}