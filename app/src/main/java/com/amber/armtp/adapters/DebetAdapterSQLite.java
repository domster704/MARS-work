package com.amber.armtp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.extra.ExtraFunctions;

public class DebetAdapterSQLite extends SimpleCursorAdapter {
    private Context context;
    public DebetAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView[] tvArray = new TextView[]{
                view.findViewById(R.id.ColDebetContr),
                view.findViewById(R.id.ColDebetStatus),
                view.findViewById(R.id.ColDebetCredit),
                view.findViewById(R.id.ColDebetA7),
                view.findViewById(R.id.ColDebetA14),
                view.findViewById(R.id.ColDebetA21),
                view.findViewById(R.id.ColDebetA28),
                view.findViewById(R.id.ColDebetA35),
                view.findViewById(R.id.ColDebetA42),
                view.findViewById(R.id.ColDebetA49),
                view.findViewById(R.id.ColDebetA56),
                view.findViewById(R.id.ColDebetA63),
                view.findViewById(R.id.ColDebetA64),
                view.findViewById(R.id.ColDebetDolg),
                view.findViewById(R.id.ColDebetOTG30),
                view.findViewById(R.id.ColDebetOPL30),
                view.findViewById(R.id.ColDebetKOB),
        };

        if (position % 2 != 0) {
            view.setBackgroundColor(context.getResources().getColor(R.color.gridViewFirstColor));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.gridViewSecondColor));
        }

        for (TextView v : tvArray) {
            ExtraFunctions.updateTextFormatTo2DecAfterPoint(v);
        }

        return view;
    }

}