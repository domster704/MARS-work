package com.amber.armtp.ui.report;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.amber.armtp.extra.Config;
import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SalesFragment extends Fragment {

    private Calendar DeliveryDateFrom;
    private Calendar DeliveryDateTo;
    private DBHelper dbHelper;
    private String tradeRepresentativeID = "";
    private String tpName;

    private Spinner contrsSpinner;

    private EditText dateFrom;
    private EditText dateTo;

    private CheckBox cbContr;

    private Toolbar toolbar;

    private static class DataForDetails {
        public CheckBox checkBox;
        public String name;

        public DataForDetails(CheckBox checkBox, String name) {
            this.checkBox = checkBox;
            this.name = name;
        }
    }

    public static class SpecificDataForSalesReportFragment {
        public String name;
        public String date;

        public SpecificDataForSalesReportFragment(String name, String date) {
            this.name = name;
            this.date = date;
        }
    }

    private DataForDetails[] dataForDetails;

    public SalesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            toolbar.setTitle("Отчёт");
            toolbar.setSubtitle(tpName);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences("apk_version", 0);
        dbHelper = new DBHelper(getActivity());
        tradeRepresentativeID = settings.getString("ReportTPId", "");
        tpName = dbHelper.getNameOfTpById(tradeRepresentativeID);

        dateFrom = getActivity().findViewById(R.id.dateFrom);
        dateTo = getActivity().findViewById(R.id.dateTo);
        fillDatePicker(dateFrom, dateTo);

        TextView startDateTv = getActivity().findViewById(R.id.startDate);
        startDateTv.setText(String.format("(Вы можете увидеть свои продажи с %s)", dbHelper.getStartDate()));

        dateFrom.setOnClickListener(v -> new DatePickerDialog(getActivity(), getDateSetListener(dateFrom, DeliveryDateFrom), DeliveryDateFrom.get(Calendar.YEAR), DeliveryDateFrom.get(Calendar.MONTH), DeliveryDateFrom.get(Calendar.DAY_OF_MONTH)).show());
        dateTo.setOnClickListener(v -> new DatePickerDialog(getActivity(), getDateSetListener(dateTo, DeliveryDateTo), DeliveryDateTo.get(Calendar.YEAR), DeliveryDateTo.get(Calendar.MONTH), DeliveryDateTo.get(Calendar.DAY_OF_MONTH)).show());

        cbContr = getActivity().findViewById(R.id.isBuyer);
        CheckBox cbGroup = getActivity().findViewById(R.id.isGoodsGroups);

        dataForDetails = new DataForDetails[]{
                new DataForDetails(cbContr, "CONTRS"),
                new DataForDetails(cbGroup, "GRUPS")
        };

        Button button = getActivity().findViewById(R.id.showReport);
        button.setOnClickListener(view -> showResultFragment(tpName));

        contrsSpinner = getActivity().findViewById(R.id.buyer);
        contrsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (l == 0)
                    return;
                cbContr.setChecked(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        cbContr.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) return;
            contrsSpinner.setSelection(0);
        });

        loadContrsInSalesSpinner();
        setFilterOnContrSpinner();


        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    private void fillDatePicker(EditText dateFrom, EditText dateTo) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        // Filling dateFromPicker by monday of current week
        Calendar calendarFrom = Calendar.getInstance();

        calendarFrom.set(Calendar.YEAR, calendarFrom.get(Calendar.YEAR));
        calendarFrom.set(Calendar.MONTH, calendarFrom.get(Calendar.MONTH));
        int mondayOfCurrentWeek = calendarFrom.get(Calendar.DAY_OF_MONTH) - (calendarFrom.get(Calendar.DAY_OF_WEEK) - 2);
        if (mondayOfCurrentWeek < 1) {
            calendarFrom.set(Calendar.MONTH, calendarFrom.get(Calendar.MONTH) - 1);
            calendarFrom.set(Calendar.DAY_OF_MONTH, calendarFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
            mondayOfCurrentWeek = calendarFrom.getActualMaximum(Calendar.DAY_OF_MONTH) - (calendarFrom.get(Calendar.DAY_OF_WEEK) - 2);
        }
        calendarFrom.set(Calendar.DAY_OF_MONTH, mondayOfCurrentWeek);
        DeliveryDateFrom = calendarFrom;

        dateFrom.setText(sdf.format(calendarFrom.getTime()));

        // Filling dateToPicker by sunday of this week
        Calendar calendarTo = Calendar.getInstance();
        calendarTo.set(Calendar.YEAR, calendarTo.get(Calendar.YEAR));
        calendarTo.set(Calendar.MONTH, calendarTo.get(Calendar.MONTH));
        int sundayOfCurrentWeek = calendarTo.get(Calendar.DAY_OF_MONTH) + (8 - (calendarTo.get(Calendar.DAY_OF_WEEK)));
        if (sundayOfCurrentWeek > calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            sundayOfCurrentWeek = sundayOfCurrentWeek - calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendarTo.set(Calendar.MONTH, calendarTo.get(Calendar.MONTH) + 1);
        }
        calendarTo.set(Calendar.DAY_OF_MONTH, sundayOfCurrentWeek);
        DeliveryDateTo = calendarTo;

        dateTo.setText(sdf.format(calendarTo.getTime()));
    }

    private DatePickerDialog.OnDateSetListener getDateSetListener(EditText editText, Calendar DeliveryDate) {
        return (view, year, monthOfYear, dayOfMonth) -> {
            DeliveryDate.set(Calendar.YEAR, year);
            DeliveryDate.set(Calendar.MONTH, monthOfYear);
            DeliveryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd.MM.yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

            editText.setText(sdf.format(DeliveryDate.getTime()));
        };
    }

    private boolean isAnyCheckBoxChecked() {
        for (DataForDetails details : dataForDetails) {
            if (details.checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> getAllChosenCheckBox() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (DataForDetails details : dataForDetails) {
            if (details.checkBox.isChecked()) {
                arrayList.add(details.name);
            }
        }
        return arrayList;
    }

    private void loadContrsInSalesSpinner() {
        getActivity().runOnUiThread(() -> {
            contrsSpinner.setAdapter(null);
            Cursor cursor = dbHelper.getContrList();
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.sales_contr_layout, cursor, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
            contrsSpinner.setAdapter(adapter);
        });

    }

    private void loadFilterContrsInSalesSpinner(String FindStr) {
        getActivity().runOnUiThread(() -> {
            contrsSpinner.setAdapter(null);
            Cursor cursor = dbHelper.getContrFilterList(FindStr);
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.sales_contr_layout, cursor, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
            contrsSpinner.setAdapter(adapter);
        });
    }

    private void setFilterOnContrSpinner() {
        EditText etContrFilter = getActivity().findViewById(R.id.contrFilterInSales);
        etContrFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String contrFilter = etContrFilter.getText().toString();
                if (contrFilter.length() != 0) {
                    loadFilterContrsInSalesSpinner(contrFilter);
                } else {
                    loadContrsInSalesSpinner();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void showResultFragment(String tpName) {
        if (!dbHelper.isTableExisted("REAL")) {
            Config.sout("Таблица REAL не существует, обновите базу данных", getContext());
            return;
        }

        String[] data = dbHelper.countSaleDataInRealTableById(tradeRepresentativeID,
                new String[]{dateFrom.getText().toString(),
                        dateTo.getText().toString()
                });
        String allSum = data[0];
        String allCount = data[1];
        if (isAnyCheckBoxChecked()) {
            ArrayList<String> args = getAllChosenCheckBox();
            SalesReportResultFragment fragment = new SalesReportResultFragment();

            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.frame, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.hide(this);

            Bundle bundle = new Bundle();
            bundle.putStringArrayList("details", args);
            bundle.putSerializable("specificData", getContrIdFromSpinner());
            bundle.putString("tradeRepresentative", tradeRepresentativeID);
            bundle.putString("wholeSum", allSum);
            bundle.putString("tpName", tpName);
            bundle.putStringArray("dateData", new String[]{
                    dateFrom.getText().toString(),
                    dateTo.getText().toString()
            });

            fragment.setArguments(bundle);
            fragmentTransaction.commit();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Продажи")
                    .setMessage(allSum + " руб.\n" + allCount + " шт.")
                    .setPositiveButton("Закрыть", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setCancelable(true)
                    .show();
        }
    }

    private SpecificDataForSalesReportFragment[] getContrIdFromSpinner() {
        Cursor cursor = (Cursor) contrsSpinner.getItemAtPosition(contrsSpinner.getSelectedItemPosition());
        return new SpecificDataForSalesReportFragment[]{
                new SpecificDataForSalesReportFragment("CONTRS", cursor.getString(cursor.getColumnIndex("CODE")))
        };
    }
}