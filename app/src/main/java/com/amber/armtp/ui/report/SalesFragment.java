package com.amber.armtp.ui.report;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.amber.armtp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SalesFragment extends Fragment {
    private final Calendar DeliveryDate = Calendar.getInstance();
    private static String tradeRepresentativeID = "IXXX26";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EditText dateFrom = getActivity().findViewById(R.id.dateFrom);
        EditText dateTo = getActivity().findViewById(R.id.dateTo);
        fillDatePicker(dateFrom, dateTo);

        dateFrom.setOnClickListener(v -> new DatePickerDialog(getActivity(), getDateSetListener(dateFrom), DeliveryDate.get(Calendar.YEAR), DeliveryDate.get(Calendar.MONTH), DeliveryDate.get(Calendar.DAY_OF_MONTH)).show());
        dateTo.setOnClickListener(v -> new DatePickerDialog(getActivity(), getDateSetListener(dateTo), DeliveryDate.get(Calendar.YEAR), DeliveryDate.get(Calendar.MONTH), DeliveryDate.get(Calendar.DAY_OF_MONTH)).show());

        dataForDetails = new DataForDetails[] {
                new DataForDetails(getActivity().findViewById(R.id.isBuyer), "CONTRS"),
                new DataForDetails(getActivity().findViewById(R.id.isGoodsGroups), "GRUPS")
        };

        Button button = getActivity().findViewById(R.id.showReport);
        button.setOnClickListener(view -> {
            if (isAnyCheckBoxChecked()) {
                ArrayList<String> args = getAllChosenCheckBox();
                ReportResultFragment fragment = new ReportResultFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment);

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("details", args);
                bundle.putString("tradeRepresentative", tradeRepresentativeID);

                fragment.setArguments(bundle);
                fragmentTransaction.commit();
            } else {
                // TODO:
            }
        });
    }

    private void fillDatePicker(EditText dateFrom, EditText dateTo) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

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

        dateFrom.setText(sdf.format(calendarFrom.getTime()));

        // Filling dateToPicker by last day of this month
        Calendar calendarTo= Calendar.getInstance();
        calendarTo.set(Calendar.YEAR, calendarTo.get(Calendar.YEAR));
        calendarTo.set(Calendar.MONTH, calendarTo.get(Calendar.MONTH));
        calendarTo.set(Calendar.DAY_OF_MONTH, calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH));

        dateTo.setText(sdf.format(calendarTo.getTime()));
    }

    private DatePickerDialog.OnDateSetListener getDateSetListener(EditText editText) {
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
        for (DataForDetails details: dataForDetails) {
            if (details.checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> getAllChosenCheckBox() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (DataForDetails details: dataForDetails) {
            if (details.checkBox.isChecked()) {
                arrayList.add(details.name);
            }
        }
        return arrayList;
    }
}