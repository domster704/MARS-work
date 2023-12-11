package com.amber.armtp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.extra.ExtraFunctions;

public class JournalDetailsAdapterSQLite extends SimpleCursorAdapter {
    private final Context context;
    public JournalDetailsAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Cursor cursor = getCursor();
        View view = super.getView(position, convertView, parent);
        TextView tvQty = view.findViewById(R.id.ColOrdDtQty);

        TextView tvSum = view.findViewById(R.id.ColOrdDtSum);
        TextView tvPrice = view.findViewById(R.id.ColOrdDtPrice);
        ExtraFunctions.updateTextFormatTo2DecAfterPoint(tvSum);
        ExtraFunctions.updateTextFormatTo2DecAfterPoint(tvPrice);

        if (position % 2 != 0) {
            view.setBackgroundColor(context.getResources().getColor(R.color.gridViewFirstColor));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.gridViewSecondColor));
        }

        if (cursor.getInt(cursor.getColumnIndex("IS_OUTED")) == 1) {
            int outQTY = cursor.getInt(cursor.getColumnIndex("OUT_QTY"));
            int QTY = cursor.getInt(cursor.getColumnIndex("QTY"));
            tvQty.setText(QTY + "(" + (QTY - outQTY) + ")");
        }

        return view;
    }
}
