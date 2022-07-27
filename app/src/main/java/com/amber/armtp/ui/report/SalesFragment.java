package com.amber.armtp.ui.report;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.amber.armtp.Config;
import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.ui.SettingFragment;

import org.apache.commons.lang3.ArrayUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SalesFragment extends Fragment {

    private Calendar DeliveryDateFrom;
    private Calendar DeliveryDateTo;
    private DBHelper dbHelper;
    private String tradeRepresentativeID = ""; // IXXX26 I09601
    private String[] chosenCheckBoxInSalesFragment = null;
    private String[] dateInSalesFragment = null;

    private static class DataForDetails {
        public CheckBox checkBox;
        public String name;

        public DataForDetails(CheckBox checkBox, String name) {
            this.checkBox = checkBox;
            this.name = name;
        }
    }

    private DataForDetails[] dataForDetails;

    public SalesFragment() {
    }

    /**
     * Конструктор для передачи данных о выбранных чекбоксах и дате, чтобы при возвращении из SalesReportResultFragment эти чекбоксы и поля с датами были установлены.
     * Не использовал Bundle, так как переход на этот фрагмент происходит через посредника (ReportPageAdapter), где могу установить данные только через конструктор
     */
    @SuppressLint("ValidFragment")
    public SalesFragment(String[] chosenCB, String[] date) {
        chosenCheckBoxInSalesFragment = chosenCB;
        dateInSalesFragment = date;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences("apk_version", 0);
        tradeRepresentativeID = settings.getString("ReportTPId", "");

        dbHelper = new DBHelper(getActivity());
        String tpName = dbHelper.getNameOfTpById(tradeRepresentativeID);

        if (tradeRepresentativeID.equals("") || tpName.equals("")) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Непраивльный идентификатор")
                    .setMessage("Неправильно введен или отсутсвует ID торгового представителя")
                    .setCancelable(false)
                    .setPositiveButton("Ввести ID", (dialogInterface, i) -> {
                        SettingFragment fragment = new SettingFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                        Bundle bundle = new Bundle();
                        bundle.putIntArray("Layouts", new int[]{R.id.reportLayoutsMain});

                        fragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.frame, fragment);
                        fragmentTransaction.commit();
                    })
                    .show();
        }

        EditText dateFrom = getActivity().findViewById(R.id.dateFrom);
        EditText dateTo = getActivity().findViewById(R.id.dateTo);
        fillDatePicker(dateFrom, dateTo);

        dateFrom.setOnClickListener(v -> new DatePickerDialog(getActivity(), getDateSetListener(dateFrom, DeliveryDateFrom), DeliveryDateFrom.get(Calendar.YEAR), DeliveryDateFrom.get(Calendar.MONTH), DeliveryDateFrom.get(Calendar.DAY_OF_MONTH)).show());
        dateTo.setOnClickListener(v -> new DatePickerDialog(getActivity(), getDateSetListener(dateTo, DeliveryDateTo), DeliveryDateTo.get(Calendar.YEAR), DeliveryDateTo.get(Calendar.MONTH), DeliveryDateTo.get(Calendar.DAY_OF_MONTH)).show());

        dataForDetails = new DataForDetails[]{
                new DataForDetails(getActivity().findViewById(R.id.isBuyer), "CONTRS"),
                new DataForDetails(getActivity().findViewById(R.id.isGoodsGroups), "GRUPS")
        };

        Button button = getActivity().findViewById(R.id.showReport);
        button.setOnClickListener(view -> {
            if (!dbHelper.isTableExisted("REAL")) {
                Config.sout("Таблица REAL не существует, обновите базу данных");
                return;
            }

            String wholeSum = dbHelper.countSumInRealTableById(tradeRepresentativeID,
                    new String[]{dateFrom.getText().toString(),
                            dateTo.getText().toString()
                    });
            if (isAnyCheckBoxChecked()) {
                ArrayList<String> args = getAllChosenCheckBox();
                SalesReportResultFragment fragment = new SalesReportResultFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("details", args);
                bundle.putString("tradeRepresentative", tradeRepresentativeID);
                bundle.putString("wholeSum", wholeSum);
                bundle.putString("tpName", tpName);
                bundle.putStringArray("dateData", new String[]{
                        dateFrom.getText().toString(),
                        dateTo.getText().toString()
                });

                fragment.setArguments(bundle);
                fragmentTransaction.commit();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Сумма")
                        .setMessage(wholeSum)
                        .setPositiveButton("Закрыть", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .setCancelable(true)
                        .show();
            }
        });

        if (chosenCheckBoxInSalesFragment != null) {
            for (DataForDetails details : dataForDetails) {
                if (ArrayUtils.contains(chosenCheckBoxInSalesFragment, details.name)) {
                    details.checkBox.setChecked(true);
                }
            }
        }
        if (dateInSalesFragment != null) {
            dateFrom.setText(dateInSalesFragment[0]);
            dateTo.setText(dateInSalesFragment[1]);

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

            try {
                Date from = sdf.parse(dateInSalesFragment[0]);
                DeliveryDateFrom.setTime(from);

                Date to = sdf.parse(dateInSalesFragment[1]);
                DeliveryDateTo.setTime(to);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(tpName);
        toolbar.setTitle("Отчёт");
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
}