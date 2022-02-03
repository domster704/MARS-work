package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * Фрагмент "Шапка заказа"
 * <p>
 * Updated by domster704 on 27.09.2021
 */
public class OrderHeadFragment extends Fragment {
    public GlobalVars glbVars;
    public static String CONTR_ID;
    public static String PREVIOUS_CONTR_ID = "";

    public static boolean isCopied = false;
    private String _TP = "";
    private String _CONTR = "";
    public static String _ADDR = "";
    private String _DATE = "";
    private String _COMMENT = "";

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;
    private android.support.v7.widget.Toolbar toolbar;

    public OrderHeadFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.order_head_frargment_main, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        glbVars.view = rootView;
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        System.out.println(1);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();

        if (getArguments() != null) {
            isCopied = getArguments().getBoolean("isCopied");
            _TP = getArguments().getString("TP");
            _CONTR = getArguments().getString("CONTR");
            _ADDR = getArguments().getString("ADDR");
            _DATE = getArguments().getString("DOC_DATE");
            _COMMENT = getArguments().getString("COMMENT");

            getArguments().remove("isCopied");
            getArguments().remove("TP");
            getArguments().remove("CONTR");
            getArguments().remove("ADDR");
            getArguments().remove("DOC_DATE");
            getArguments().remove("COMMENT");
        }

        GlobalVars.CurAc = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.edContrFilter = getActivity().findViewById(R.id.txtContrFilter);
        glbVars.txtComment = getActivity().findViewById(R.id.txtComment);

        glbVars.spinContr = getActivity().findViewById(R.id.SpinContr);
        glbVars.spinAddr = getActivity().findViewById(R.id.SpinAddr);
        glbVars.TPList = getActivity().findViewById(R.id.SpinTP);

        _setContrAndSum();

        glbVars.LoadTpList();
        glbVars.LoadContrList();

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = settings.edit();

        glbVars.edContrFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String ContrFilter = glbVars.edContrFilter.getText().toString();
                if (ContrFilter.length() != 0) {
                    glbVars.LoadFilteredContrList(ContrFilter);
                } else {
                    glbVars.LoadContrList();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        glbVars.DeliveryDate = Calendar.getInstance();

        glbVars.txtDate = getActivity().findViewById(R.id.txtDelivDate);

        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            glbVars.DeliveryDate.set(Calendar.YEAR, year);
            glbVars.DeliveryDate.set(Calendar.MONTH, monthOfYear);
            glbVars.DeliveryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd.MM.yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

            glbVars.txtDate.setText(sdf.format(glbVars.DeliveryDate.getTime()));
        };

        glbVars.txtDate.setOnClickListener(v -> {
            new DatePickerDialog(getActivity(), date, glbVars.DeliveryDate.get(Calendar.YEAR), glbVars.DeliveryDate.get(Calendar.MONTH), glbVars.DeliveryDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        int ContRowid = glbVars.db.GetContrRowID();

        SetSelectedContr(ContRowid);

        String stTP_ID = settings.getString("TP_ID", "0");

        int TPRowid = glbVars.db.GetTPRowID();
        int TPDefaultRowid = glbVars.db.GetTPByID(stTP_ID);

        if (glbVars.CheckTPLock()) {
            glbVars.TPList.setSelection(TPDefaultRowid);
        } else {
            assert stTP_ID != null;
            if (stTP_ID.equals("0")) {
                glbVars.TPList.setSelection(TPRowid);
            } else {
                if (TPRowid != TPDefaultRowid && TPRowid != 0) {
                    glbVars.TPList.setSelection(TPRowid);
                } else {
                    glbVars.TPList.setSelection(TPDefaultRowid);
                }
            }
        }

        String Comment = glbVars.db.GetComment();
        String DelivDate = glbVars.db.GetDeliveryDate();
        if (!Comment.equals("")) {
            glbVars.txtComment.setText(Comment, TextView.BufferType.EDITABLE);
        }

        if (!DelivDate.equals("")) {
            glbVars.txtDate.setText(DelivDate, TextView.BufferType.EDITABLE);
        }

        _setContrAndSum();

        if (isCopied) {
            int tpID = glbVars.db.GetTPByID(_TP);
            int contrID = glbVars.db.GetContrByID(_CONTR);

            glbVars.LoadContrListWithAddr(_ADDR);

            glbVars.TPList.setSelection(tpID);
            glbVars.spinContr.setSelection(contrID);

            String[] dateArray = _DATE.split("\\.");
            glbVars.DeliveryDate.set(Calendar.YEAR, Integer.parseInt(dateArray[2]));
            glbVars.DeliveryDate.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1);
            glbVars.DeliveryDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[0]));
            String myFormat = "dd.MM.yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

            glbVars.txtDate.setText(sdf.format(glbVars.DeliveryDate.getTime()));
            glbVars.txtComment.setText(_COMMENT);

            OrderHeadFragment.isCopied = false;
        }
    }

    public void SetSelectedContr(int ROWID) {
        for (int i = 0; i < glbVars.spinContr.getCount(); i++) {
            Cursor value = (Cursor) glbVars.spinContr.getItemAtPosition(i);
            int id = value.getInt(value.getColumnIndexOrThrow("_id"));
            if (ROWID == id) {
                glbVars.spinContr.setSelection(i);
                break;
            }
        }
    }

    public void goToFormOrderFragment() {
        toolbar.setTitle(R.string.form_order);
        Fragment fragment = new FormOrderFragment();
        fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
        fragmentTransaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_head_menu, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveHeader:
                _saveOrderData();
                return true;
            default:
                return true;
        }
    }

    private void _saveOrderData() {
        String ADDR_ID;

        glbVars.spTp = Objects.requireNonNull(getActivity()).findViewById(R.id.ColTPID);
        glbVars.spContr = getActivity().findViewById(R.id.ColContrID);
        glbVars.spAddr = getActivity().findViewById(R.id.ColContrAddrID);

        CONTR_ID = glbVars.spContr.getText().toString();

        String TP_ID = glbVars.spTp.getText().toString();

        String CurContr = glbVars.db.GetContrID();

        if (!CurContr.equals(CONTR_ID)) {
            _setContrAndSum();
        }

        ADDR_ID = glbVars.spAddr != null ? glbVars.spAddr.getText().toString() : "0";

        String DeliveryDate = glbVars.txtDate.getText().toString();
        String Comment = glbVars.txtComment.getText().toString();

        if (TP_ID.equals("0") || CONTR_ID.equals("0") || ADDR_ID.equals("0") || DeliveryDate.equals("")) {
            Toast.makeText(getActivity(), "Необходимо заполнить все обязательные поля шапки заказа", Toast.LENGTH_LONG).show();
            return;
        }

        editor.putString("TP_ID", TP_ID);
        editor.commit();

        if (PREVIOUS_CONTR_ID.equals("")) {
            PREVIOUS_CONTR_ID = CONTR_ID;
        } else if (!PREVIOUS_CONTR_ID.equals(CONTR_ID)) {
            PREVIOUS_CONTR_ID = CONTR_ID;
            glbVars.db.ResetNomenPrice(isCopied);
        }

        if (glbVars.db.insertOrder(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment)) {
            _setContrAndSum();
        } else {
            if (glbVars.db.UpdateOrderHead(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment)) {
                _setContrAndSum();
            } else {
                Toast.makeText(getActivity(), "Вы уже заполнили шапку заказа, либо не удалось обновить шапку заказа", Toast.LENGTH_LONG).show();
            }
        }

        goToFormOrderFragment();
    }

    private void _setContrAndSum() {
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        try {
            if (!OrderSum.equals("")) {
                if (ToolBarContr.trim().equals("")) {
                    toolbar.setSubtitle("Заказ на сумму " + OrderSum + " руб.");
                } else {
                    toolbar.setSubtitle(ToolBarContr + OrderSum + " руб.");
                }
            } else {
                toolbar.setSubtitle("");
            }
        } catch (Exception ignored) {
        }
    }
}
