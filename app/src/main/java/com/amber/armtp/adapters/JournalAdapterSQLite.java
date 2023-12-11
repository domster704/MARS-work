package com.amber.armtp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.extra.ExtraFunctions;
import com.amber.armtp.ui.JournalFragment;

import java.util.HashMap;
import java.util.Set;

public class JournalAdapterSQLite extends SimpleCursorAdapter {
    private static class ClickData {
        public int visibility;

        public ClickData(int visibility) {
            this.visibility = visibility;
        }
    }

    private final Context context;
    private final HashMap<Integer, ClickData> clickDataMap;

    public JournalAdapterSQLite(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        clickDataMap = new HashMap<Integer, ClickData>() {
            @NonNull
            @Override
            public String toString() {
                StringBuilder s = new StringBuilder();
                Set<Integer> a = clickDataMap.keySet();
                for (int i : a) {
                    s.append(i).append(" ");
                }
                return s.toString();
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Cursor cursor = getCursor();
        View view = super.getView(position, convertView, parent);
        LinearLayout layout = view.findViewById(R.id.expandedData);

        if (!clickDataMap.containsKey(position)) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(clickDataMap.get(position).visibility);
        }

        ImageView showData = view.findViewById(R.id.showData);
        TextView tvSum = view.findViewById(R.id.ColOrdSum);
        ExtraFunctions.updateTextFormatTo2DecAfterPoint(tvSum);

        if (cursor.getInt(cursor.getColumnIndex("OUTED")) == 1) {
            tvSum.setText(String.format("%s (-)", tvSum.getText().toString()));
        }

        TextView tvStatus = view.findViewById(R.id.ColOrdStatus);
        if (tvStatus.getText().toString().equals("Сохранён"))
            tvStatus.setTypeface(Typeface.DEFAULT_BOLD);

        if (JournalFragment.allOrders.size() != 0 && JournalFragment.allOrders.get(position).isChecked()) {
            view.setBackgroundColor(context.getResources().getColor(R.color.gridViewFirstColorJournal));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.gridViewSecondColor));
        }

        ClickData clickData = new ClickData(View.GONE);
        if (!isNeededToShowEnterIcon()) {
            showData.setVisibility(View.INVISIBLE);
            clickDataMap.put(position, clickData);
        } else {
            showData.setVisibility(View.VISIBLE);
            showData.setOnClickListener(view1 -> {
                if (layout.getVisibility() == View.GONE) {
                    layout.setVisibility(View.VISIBLE);
                    clickData.visibility = View.VISIBLE;
                } else {
                    layout.setVisibility(View.GONE);
                }
                clickDataMap.put(position, clickData);
            });
        }

        return view;
    }

    private boolean isNeededToShowEnterIcon() {
        Cursor cursor = getCursor();
        String[] data = new String[]{
                cursor.getString(cursor.getColumnIndex("COMMENT"))
        };
        for (String i : data) {
            if (!i.equals("")) {
                return true;
            }
        }
        return false;
    }
}