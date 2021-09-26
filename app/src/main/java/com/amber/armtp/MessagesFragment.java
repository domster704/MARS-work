package com.amber.armtp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MessagesFragment extends Fragment {
    Button btMarkAsRead;
    Calendar CalBDate, CalEDate;
    EditText txtBDate, txtEDate;
    Button btOrderFilter, btUpdateSMS;
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    String currentDate = sdf.format(new Date());
    public GlobalVars glbVars;

    public MessagesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.messages_fragment, container, false);
        glbVars.view = rootView;
        return rootView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.smsList = getActivity().findViewById(R.id.listSMS);
        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        btMarkAsRead = getActivity().findViewById(R.id.btMarkAsRead);
        txtBDate = getActivity().findViewById(R.id.txtBDate);
        txtEDate = getActivity().findViewById(R.id.txtEDate);
        btOrderFilter = getActivity().findViewById(R.id.btShowOrders);
        btUpdateSMS = getActivity().findViewById(R.id.btUpdateSMS);

        toolbar.setSubtitle("");

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String formattedDate = df.format(c.getTime());

        CalBDate = Calendar.getInstance();
        CalEDate = Calendar.getInstance();

        txtBDate.setText(formattedDate);
        txtEDate.setText(formattedDate);

        final DatePickerDialog.OnDateSetListener Bdate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                CalBDate.set(Calendar.YEAR, year);
                CalBDate.set(Calendar.MONTH, monthOfYear);
                CalBDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                txtBDate.setText(sdf.format(CalBDate.getTime()));
            }
        };


        txtBDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), Bdate, CalBDate.get(Calendar.YEAR), CalBDate.get(Calendar.MONTH), CalBDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final DatePickerDialog.OnDateSetListener Edate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                CalEDate.set(Calendar.YEAR, year);
                CalEDate.set(Calendar.MONTH, monthOfYear);
                CalEDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                txtEDate.setText(sdf.format(CalEDate.getTime()));
            }

        };

        txtEDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), Edate, CalEDate.get(Calendar.YEAR), CalEDate.get(Calendar.MONTH), CalEDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btOrderFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BDate = txtBDate.getText().toString();
                String EDate = txtEDate.getText().toString();
                if (!BDate.equals("") && !EDate.equals("")) {
                    glbVars.LoadSms(BDate, EDate);
                } else {
                    Toast.makeText(getActivity(), "Обязательно необходимо указать период", Toast.LENGTH_LONG).show();
                }
            }
        });

        btUpdateSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glbVars.getNewSMS();
            }
        });

        glbVars.smsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                glbVars.SmsDB.execSQL("UPDATE MSGS SET IS_NEW = 0 WHERE ROW_ID=" + id);
                glbVars.SMSadapter.notifyDataSetChanged();
                glbVars.cur_sms.requery();
                Integer count = glbVars.getSMSCount();
                ShortcutBadger.applyCount(getActivity(), count);
            }
        });

        glbVars.LoadSms(currentDate, currentDate);
    }

}
