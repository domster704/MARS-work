package com.amber.armtp.ui.report;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportResultFragment extends Fragment {
    private String tradeRepresentative;
//    private Map<String, Integer> fields = new HashMap<String, Integer>() {{
//        put("_id", R.id.reportResultPos);
//        put("CONTRS", R.id.contr);
//        put("GRUPS", R.id.groups);
//        put("SUMMA", R.id.sum);
//    }};

    private Map<String, ViewWidthByName> headersName;

    private static class ViewWidthByName {
        public String name;
        public int id;
        public float width;

        public ViewWidthByName(String name, int id, float width) {
            this.name = name;
            this.id = id;
            this.width = width;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_result, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        headersName = new HashMap<String, ViewWidthByName>() {{
            put("_id", new ViewWidthByName("NN",  R.id.reportResultPos, getResources().getDimension(R.dimen.reportResultSmallFieldWidth)));
            put("CONTRS", new ViewWidthByName("Производитель", R.id.contr, getResources().getDimension(R.dimen.reportResultBigFieldWidth)));
            put("GRUPS", new ViewWidthByName("Название группы", R.id.groups, getResources().getDimension(R.dimen.reportResultBigFieldWidth)));
            put("SUMMA", new ViewWidthByName("Сумма", R.id.sum, getResources().getDimension(R.dimen.reportResultMediumFieldWidth)));
        }};

        if (getArguments() == null) {
            return;
        }

        ArrayList<String> details = getArguments().getStringArrayList("details");
        tradeRepresentative = getArguments().getString("tradeRepresentative");

        Cursor cursor = getResultCursor(details);

//        details.add(0, "_id");
        details.add("SUMMA");

        String[] chosenColumnsInCursor = details.toArray(new String[0]);
        ViewWidthByName[] chosenHeadersInHeadersLayout = new ViewWidthByName[details.size()];
        int[] chosenViewsInXML = new int[details.size()];
        for (int i = 0; i < details.size(); i++) {
            if (!headersName.containsKey(details.get(i)))
                continue;
            chosenViewsInXML[i] = headersName.get(details.get(i)).id;
            chosenHeadersInHeadersLayout[i] = headersName.get(details.get(i));
        }

        GridView gridView = getActivity().findViewById(R.id.reportResultGrid);
        ReportResultAdapter adapter = new ReportResultAdapter(getActivity(), R.layout.report_result_dynamic_layout, cursor, chosenColumnsInCursor, chosenViewsInXML, 0);
        gridView.setAdapter(adapter);

        fillHeaderLayout(chosenHeadersInHeadersLayout);
    }

    private class ReportResultAdapter extends SimpleCursorAdapter {
        private final String[] chosenHeaders;

        public ReportResultAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            chosenHeaders = from;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();

            TextView tvSum = view.findViewById(R.id.sum);
            float sum = Float.parseFloat(cursor.getString(cursor.getColumnIndex("SUMMA")).replace(",", "."));
            tvSum.setText(String.format(Locale.ROOT, "%.2f", sum));

            for (String i : chosenHeaders) {
                TextView textView = view.findViewById(headersName.get(i).id);
                textView.setLayoutParams(new LinearLayout.LayoutParams((int) headersName.get(i).width, (int) getResources().getDimension(R.dimen.heightOfReportResultViews)));
            }
            return view;
        }
    }

    private Cursor getResultCursor(ArrayList<String> arrayList) {
        StringBuilder sqlRequest = new StringBuilder(", ");
        for (String i : arrayList) {
            sqlRequest.append("TRIM(").append(i).append(".DESCR) as ").append(i).append(",");
        }
        String res = sqlRequest.substring(0, sqlRequest.length() - 1);

        StringBuilder groupByReq = new StringBuilder();
        for (String i : arrayList) {
            groupByReq.append(i).append(",");
        }
        String resGroupBy = groupByReq.substring(0, groupByReq.length() - 1);

        String joinSqlReq = "";
        for (String i : arrayList) {
            joinSqlReq += "JOIN " + i + " ON " + i + ".CODE=REAL." + i + " ";
        }

        DBHelper dbHelper = new DBHelper(getActivity());
        return dbHelper.getReadableDatabase().rawQuery("SELECT REAL.ROWID as _id, SUM(SUMMA) as SUMMA " + res + " FROM REAL " + joinSqlReq + " WHERE TORG_PRED=? GROUP BY _id, " + resGroupBy + " ORDER BY SUMMA DESC", new String[]{tradeRepresentative});
    }

    private static class HeaderView extends android.support.v7.widget.AppCompatTextView {

        public HeaderView(Context context, ViewWidthByName data) {
            super(context);
            setLayoutParams(new LinearLayout.LayoutParams((int) data.width, ViewGroup.LayoutParams.MATCH_PARENT));
            setGravity(Gravity.CENTER);
            setTextSize(Dimension.SP, 16);
            setTextColor(getResources().getColor(R.color.black));
            setText(data.name);
        }
    }

    private void fillHeaderLayout(ViewWidthByName[] headersList) {

        LinearLayout layout = getActivity().findViewById(R.id.headersLayout);

        for (ViewWidthByName header : headersList) {
            TextView tv = new HeaderView(getContext(), header);
            layout.addView(tv);
        }
    }
}