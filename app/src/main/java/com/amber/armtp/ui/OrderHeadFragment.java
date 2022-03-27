package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
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
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.TBUpdate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * Фрагмент "Шапка заказа"
 * <p>
 * Updated by domster704 on 27.09.2021
 */
public class OrderHeadFragment extends Fragment implements TBUpdate {
    public static String CONTR_ID;
    public static String PREVIOUS_CONTR_ID = "";
    public static String _ADDR = "";
    public static boolean isCopied = false;
    public GlobalVars glbVars;
    private String _TP = "";
    private String _CONTR = "";
    private String _DATE = "";
    private String _COMMENT = "";

    private SharedPreferences.Editor editor;
    private android.support.v7.widget.Toolbar toolbar;
    private boolean isCopiedLocal = false;
    private static final String APP_PREFERENCES_CONTR = "Contr";

    public OrderHeadFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.order_head_frargment_main, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        glbVars.CurView = rootView;
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        GlobalVars.CurFragmentContext = getActivity();

        if (getArguments() != null && getArguments().size() != 0) {
            isCopied = isCopiedLocal = getArguments().getBoolean("isCopied");
            _TP = getArguments().getString("TP");
            _CONTR = getArguments().getString("CONTR");
            _ADDR = getArguments().getString("ADDR");
            _DATE = getArguments().getString("DOC_DATE");
            _COMMENT = getArguments().getString("COMMENT");

            getArguments().clear();
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

        _checkAndSetContrIDAfterDestroying();
        glbVars.toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.edContrFilter = getActivity().findViewById(R.id.txtContrFilter);
        glbVars.txtComment = getActivity().findViewById(R.id.txtComment);

        glbVars.spinContr = getActivity().findViewById(R.id.SpinContr);
        glbVars.spinAddr = getActivity().findViewById(R.id.SpinAddr);
        glbVars.TPList = getActivity().findViewById(R.id.SpinTP);

        setContrAndSum(glbVars);

        glbVars.LoadTpList();
        glbVars.LoadContrList();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

        glbVars.txtDate.setOnClickListener(v -> new DatePickerDialog(getActivity(), date, glbVars.DeliveryDate.get(Calendar.YEAR), glbVars.DeliveryDate.get(Calendar.MONTH), glbVars.DeliveryDate.get(Calendar.DAY_OF_MONTH)).show());

        int contrRowId = glbVars.db.GetContrRowID();

        SetSelectedContr(contrRowId);

        String stTP_ID = settings.getString("TP_ID", "0");

        int TPRowId = glbVars.db.GetTPRowID();
        int TPDefaultRowId = glbVars.db.GetTPByID(stTP_ID);

        if (glbVars.CheckTPLock()) {
            glbVars.TPList.setSelection(TPDefaultRowId);
        } else {
            assert stTP_ID != null;
            if (stTP_ID.equals("0")) {
                glbVars.TPList.setSelection(TPRowId);
            } else {
                if (TPRowId != TPDefaultRowId && TPRowId != 0) {
                    glbVars.TPList.setSelection(TPRowId);
                } else {
                    glbVars.TPList.setSelection(TPDefaultRowId);
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

        setContrAndSum(glbVars);

        if (isCopied) {
            int tpID = glbVars.db.GetTPByID(_TP);
            int contrID = glbVars.db.GetContrByID(_CONTR);

            glbVars.LoadContrList();

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

    public void SetSelectedContr(int rowId) {
        for (int i = 0; i < glbVars.spinContr.getCount(); i++) {
            Cursor value = (Cursor) glbVars.spinContr.getItemAtPosition(i);
            int id = value.getInt(value.getColumnIndexOrThrow("_id"));
            if (rowId == id) {
                glbVars.spinContr.setSelection(i);
                break;
            }
        }
    }

    public void goToFormOrderFragment() {
        toolbar.setTitle(R.string.form_order);
        Fragment fragment = new FormOrderFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
        fragmentTransaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_head_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.saveHeader) {
            try {
                _saveOrderData();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREFERENCES_CONTR, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PREVIOUS_CONTR_ID", PREVIOUS_CONTR_ID);
        editor.putString("CURRENT_CONTR_ID", CONTR_ID);
        editor.apply();
    }

    private void _saveOrderData() throws InterruptedException {
        String ADDR_ID;

        glbVars.spTp = Objects.requireNonNull(getActivity()).findViewById(R.id.ColTPID);
        glbVars.spContr = getActivity().findViewById(R.id.ColContrID);
        glbVars.spAddr = getActivity().findViewById(R.id.ColContrAddrID);

        CONTR_ID = glbVars.spContr.getText().toString();
        ADDR_ID = glbVars.spAddr != null ? glbVars.spAddr.getText().toString() : "0";

        String DeliveryDate = glbVars.txtDate.getText().toString();
        String Comment = glbVars.txtComment.getText().toString();
        String TP_ID = glbVars.spTp.getText().toString();

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
            glbVars.db.ResetNomenPrice(isCopiedLocal);
        }

        if (glbVars.db.insertOrder(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment)) {
            setContrAndSum(glbVars);
        } else {
            if (glbVars.db.UpdateOrderHead(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment)) {
                setContrAndSum(glbVars);
            } else {
                Toast.makeText(getActivity(), "Вы уже заполнили шапку заказа, либо не удалось обновить шапку заказа", Toast.LENGTH_LONG).show();
            }
        }
        glbVars.resetCurData();
        glbVars.putAllPrices();

        goToFormOrderFragment();
    }

    private void _checkAndSetContrIDAfterDestroying() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(APP_PREFERENCES_CONTR, Context.MODE_PRIVATE);
        if (sharedPreferences == null)
            return;
        String newPreviousContrID = sharedPreferences.getString("PREVIOUS_CONTR_ID", "");
        String newContrID = sharedPreferences.getString("CURRENT_CONTR_ID", "");

        PREVIOUS_CONTR_ID = !newPreviousContrID.equals("") ? newPreviousContrID : PREVIOUS_CONTR_ID;
        CONTR_ID = !newContrID.equals("") ? newContrID : CONTR_ID;
    }
}
