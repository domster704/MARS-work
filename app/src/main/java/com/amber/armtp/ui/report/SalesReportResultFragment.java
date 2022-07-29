package com.amber.armtp.ui.report;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SalesReportResultFragment extends Fragment {
    private String tradeRepresentative;

    private Map<String, ViewWidthByName> headersName;

    private String[] chosenCheckBoxInSalesFragment;
    private String[] dateInSalesFragment;
    private SalesFragment.SpecificDataForSalesReportFragment[] specificData;

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

    public static class SentDataToSalesFragment implements Serializable {
        public String[] chosenCheckBox;
        public String[] dateInSalesFragment;
        public SalesFragment.SpecificDataForSalesReportFragment[] specificData;

        public SentDataToSalesFragment(String[] chosenCheckBox, String[] dateInSalesFragment, SalesFragment.SpecificDataForSalesReportFragment[] specificData) {
            this.chosenCheckBox = chosenCheckBox;
            this.dateInSalesFragment = dateInSalesFragment;
            this.specificData = specificData;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_report_result, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        headersName = new HashMap<String, ViewWidthByName>() {{
            put("_id", new ViewWidthByName("NN", R.id.reportResultPos, getResources().getDimension(R.dimen.reportResultSmallFieldWidth)));
            put("CONTRS", new ViewWidthByName("Покупатель", R.id.contr, getResources().getDimension(R.dimen.reportResultBigFieldWidth)));
            put("GRUPS", new ViewWidthByName("Группа", R.id.groups, getResources().getDimension(R.dimen.reportResultBigFieldWidth)));
            put("SUMMA", new ViewWidthByName("Сумма", R.id.sum, getResources().getDimension(R.dimen.reportResultMediumFieldWidth)));
        }};

        if (getArguments() == null) {
            return;
        }

        ArrayList<String> details = getArguments().getStringArrayList("details");
        chosenCheckBoxInSalesFragment = details.toArray(new String[0]);

        specificData = (SalesFragment.SpecificDataForSalesReportFragment[]) getArguments().getSerializable("specificData");

        String[] dateData = getArguments().getStringArray("dateData");
        dateInSalesFragment = dateData.clone();
        tradeRepresentative = getArguments().getString("tradeRepresentative");

        Cursor cursor = null;
        try {
            cursor = getResultCursor(details, dateData, specificData);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        details.add(0, "_id");
        details.add("SUMMA");

//        float sumOfWidth = 0;

        String[] chosenColumnsInCursor = details.toArray(new String[0]);
        ViewWidthByName[] chosenHeadersInHeadersLayout = new ViewWidthByName[details.size()];
        int[] chosenViewsInXML = new int[details.size()];
        for (int i = 0; i < details.size(); i++) {
            if (!headersName.containsKey(details.get(i)))
                continue;
            chosenViewsInXML[i] = headersName.get(details.get(i)).id;
            chosenHeadersInHeadersLayout[i] = headersName.get(details.get(i));
//            sumOfWidth += headersName.get(details.get(i)).width;
        }

        GridView gridView = getActivity().findViewById(R.id.reportResultGrid);
//        gridView.setLayoutParams(new RelativeLayout.LayoutParams((int) sumOfWidth, -1));
        ReportResultAdapter adapter = new ReportResultAdapter(getActivity(), R.layout.report_result_dynamic_layout, cursor, chosenColumnsInCursor, chosenViewsInXML, 0);
        gridView.setAdapter(adapter);

        fillHeaderLayout(chosenHeadersInHeadersLayout);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getArguments().getString("tpName"));
        toolbar.setSubtitle("Итого: " + getArguments().getString("wholeSum"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sales_report_result_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.backToSales) {
            SentDataToSalesFragment data = new SentDataToSalesFragment(
                    chosenCheckBoxInSalesFragment,
                    dateInSalesFragment,
                    specificData);

            Bundle bundle = new Bundle();
            bundle.putSerializable("dataToSalesFragment", data);

            Fragment fragment = new ReportFragment();
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, fragment)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

            TextView tvPosition = view.findViewById(R.id.reportResultPos);
            TextView tvSum = view.findViewById(R.id.sum);
            TextView tvContr = view.findViewById(R.id.contr);
            TextView tvGroup = view.findViewById(R.id.groups);

            TextView[] listTVThatNeededToChangePadding = new TextView[]{tvContr, tvGroup};
            float scale = getResources().getDisplayMetrics().density;
            int paddingValue = 10;
            for (TextView tv : listTVThatNeededToChangePadding) {
                if (!tv.getText().toString().equals("")) {
                    tv.setPadding((int) scale * paddingValue, tv.getPaddingTop(), tv.getPaddingRight(), tv.getPaddingBottom());
                }
            }

            tvPosition.setText(String.valueOf(position + 1));

            double sum = cursor.getDouble(cursor.getColumnIndex("SUMMA"));
            tvSum.setText(String.format(Locale.ROOT, "%.2f", sum));

            for (String i : chosenHeaders) {
                TextView textView = view.findViewById(headersName.get(i).id);
                textView.setLayoutParams(new LinearLayout.LayoutParams((int) headersName.get(i).width, (int) getResources().getDimension(R.dimen.heightOfReportResultViews)));
            }

            int backgroundColor;
            if (position % 2 != 0) {
                backgroundColor = getResources().getColor(R.color.gridViewFirstColor);
            } else {
                backgroundColor = getResources().getColor(R.color.gridViewSecondColor);
            }
            view.setBackgroundColor(backgroundColor);

            return view;
        }
    }

    private Cursor getResultCursor(ArrayList<String> arrayList, String[] dateData, SalesFragment.SpecificDataForSalesReportFragment[] specificData) throws ParseException {
        // Выбираем поля из БД взмависимости от выбранных чекбоксов
        StringBuilder sqlRequest = new StringBuilder(", ");
        for (String i : arrayList) {
            sqlRequest.append("TRIM(").append(i).append(".DESCR) as ").append(i).append(",");
        }
        String res = sqlRequest.substring(0, sqlRequest.length() - 1);

        // Создаём запрос для группировки данных
        StringBuilder groupByReq = new StringBuilder();
        for (String i : arrayList) {
            groupByReq.append(i).append(",");
        }
        String resGroupBy = groupByReq.substring(0, groupByReq.length() - 1);

        // Создаём запрос для объединения таблиц, чтобы можно было взять поле DESCR
        StringBuilder joinSqlReq = new StringBuilder();
        for (String i : arrayList) {
            joinSqlReq.append("JOIN ").append(i).append(" ON ").append(i).append(".CODE=REAL.").append(i).append(" ");
        }

        // Создаём запрос для конкретных id
        StringBuilder specificSqlReq = new StringBuilder();
        if (specificData.length != 0) {
            for (SalesFragment.SpecificDataForSalesReportFragment i : specificData) {
                if (i.date.equals("0")) continue;
                specificSqlReq.append(" AND ").append("REAL.").append(i.name).append("='").append(i.date).append("'");
            }
        }

        String df = getFormatedData(dateData[0].split("\\."));
        String dt = getFormatedData(dateData[1].split("\\."));

//        System.out.println("SELECT REAL.ROWID as _id, CAST((substr(data, 7, 4) || '' || substr(data, 4, 2) || '' || substr(data, 1, 2)) as INTEGER) as convertedData, SUM(SUMMA) as SUMMA " + res + " FROM REAL " + joinSqlReq + " WHERE TORG_PRED='" + tradeRepresentative + "' AND (convertedData >= '" + df + "' and convertedData <= '" + dt + "') GROUP BY " + resGroupBy + " ORDER BY SUMMA DESC");
//        Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT REAL.ROWID as _id, CAST((substr(data, 7, 4) || '' || substr(data, 4, 2) || '' || substr(data, 1, 2)) as INTEGER) as convertedData, SUM(SUMMA) as SUMMA " + res + " FROM REAL " + joinSqlReq + " WHERE TORG_PRED=? AND (convertedData >= '" + df + "' and convertedData <= '" + dt + "') GROUP BY _id, " + resGroupBy + " ORDER BY SUMMA DESC",
//                new String[]{tradeRepresentative});
//        c.moveToNext();
//        System.out.println(c.getDouble(c.getColumnIndex("SUMMA")));
//        Config.printCursor(c);
        //        SELECT CAST((substr(data, 7, 4) || '' || substr(data, 4, 2) || '' || substr(data, 1, 2)) as INTEGER) as x from REAL WHERE x>= 220715 and x<=220816 ORDER BY x DESC

        DBHelper dbHelper = new DBHelper(getActivity());
        return dbHelper.getReadableDatabase().rawQuery("SELECT REAL.ROWID as _id, CAST((substr(data, 7, 4) || '' || substr(data, 4, 2) || '' || substr(data, 1, 2)) as INTEGER) as x, SUM(SUMMA) as SUMMA " + res + " FROM REAL " + joinSqlReq + " WHERE TORG_PRED=? AND (x >= " + df + " and x <= " + dt + ") " + specificSqlReq + " GROUP BY " + resGroupBy + " ORDER BY SUMMA DESC",
                new String[]{tradeRepresentative});
    }

    private String getFormatedData(String[] date) {
        String[] dateF = date;
        ArrayUtils.swap(dateF, 0, 2);
        dateF[0] = dateF[0].substring(2);
        return StringUtils.join(dateF, "");
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
            HeaderView tv = new HeaderView(getContext(), header);
            layout.addView(tv);
        }
    }
}