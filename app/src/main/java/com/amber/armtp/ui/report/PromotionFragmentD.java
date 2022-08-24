package com.amber.armtp.ui.report;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.amber.armtp.Config;
import com.amber.armtp.R;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.dbHelpers.DBHelper;

import java.util.Locale;

public class PromotionFragmentD extends Fragment {

    private DBHelper dbHelper;

    public PromotionFragmentD() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promotion, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = new DBHelper(getActivity());

        TextView tvPreShow = getActivity().findViewById(R.id.tvPreShow);
        tvPreShow.setOnClickListener(view -> {
//            try {
                if (!dbHelper.isTableExisted("ACTION")) {
                    Config.sout("Таблица ACTION не существует, обновите базу данных");
                    return;
                }
//            } catch (Exception e) {
//                System.out.println(e);
//            }

            view.setVisibility(View.GONE);
            getActivity().findViewById(R.id.layoutWithActionTable).setVisibility(View.VISIBLE);
            showTable();
        });
    }

    private void showTable() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                SharedPreferences settings = getActivity().getSharedPreferences("apk_version", 0);
                String tradeRepresentativeID = settings.getString("ReportTPId", "");

                GridView gridView = getActivity().findViewById(R.id.actionGridView);

                ActionAdapter adapter = new ActionAdapter(getActivity(), R.layout.action_result_layout, getActionCursor(tradeRepresentativeID),
                        new String[]{"ACTION", "DATAN", "DATAK", "VAL", "PLN"},
                        new int[]{R.id.actionDesc, R.id.actionDateStart, R.id.actionDateEnd, R.id.ActionFactValue, R.id.ActionPlanValue},
                        0);

                gridView.setAdapter(adapter);
            }
        });
    }

    private Cursor getActionCursor(String torgID) {
        return new DBHelper(getActivity()).getReadableDatabase().rawQuery("SELECT ROWID as _id, [ACTION], DATAN, DATAK, PLN, ISKOL, CASE WHEN ISKOL=1 THEN KOL ELSE SUMMA END AS 'VAL' FROM [ACTION] WHERE TORG_PRED=?", new String[]{torgID});
    }


    class ActionAdapter extends SimpleCursorAdapter {
        public ActionAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();

            TextView tvPercent = view.findViewById(R.id.ActionPercent);
            TextView tvFact = view.findViewById(R.id.ActionFactValue);

            float percent = cursor.getFloat(cursor.getColumnIndex("VAL")) / cursor.getFloat(cursor.getColumnIndex("PLN")) * 100;
            tvPercent.setText(String.format(Locale.ROOT, "%.2f", percent));

            if (cursor.getInt(cursor.getColumnIndex("ISKOL")) == 0) {
                tvFact.setText(String.format(Locale.ROOT, "%.2f", cursor.getFloat(cursor.getColumnIndex("VAL"))));
            } else {
                tvFact.setText(tvFact.getText());
            }

            int backgroundColor;
            if (position % 2 != 0) {
                backgroundColor = ContextCompat.getColor(getContext(), R.color.gridViewFirstColor);
            } else {
                backgroundColor = ContextCompat.getColor(getContext(), R.color.gridViewSecondColor);
            }
            view.setBackgroundColor(backgroundColor);

            return view;
        }
    }
}