package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.R;
import com.amber.armtp.adapters.AddressAdapterSQLite;
import com.amber.armtp.adapters.ContractorAdapterSQLite;
import com.amber.armtp.adapters.TPAdapterSQLite;
import com.amber.armtp.auxiliaryData.CounterAgentInfo;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.TBUpdate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * Фрагмент "Шапка заказа"
 * Updated by domster704 on 27.09.2021
 */
public class OrderHeadFragment extends Fragment implements TBUpdate, View.OnClickListener {
    public static String TP_ID = "";
    public static String CONTR_ID = "";
    public static String PREVIOUS_CONTR_ID = "";
    public static String _ADDR = "";
    public static String CurContr = "0";
    private String _TP = "";
    private String _CONTR = "";
    private String _DATE = "";
    private String _COMMENT = "";
    private static final String APP_PREFERENCES_CONTR = "Contr";
    public static boolean isOrderEditedOrCopied = false;
    public static boolean isNeededToUpdateOrderTable = false;
    private boolean isCopiedLocal = false;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private android.support.v7.widget.Toolbar toolbar;
    public Spinner spinContr, spinAddress, TPList;
    public Calendar DeliveryDate;
    public EditText txtDate;
    public EditText edContrFilter;
    public EditText txtComment;
    public TextView spTp, spContr, spAddress;
    private DBHelper db;
    public Cursor myContr;
    public Cursor myAddress;
    public Cursor myTP;

    private final AdapterView.OnItemSelectedListener SelectedContr = new AdapterView.OnItemSelectedListener() {
        private TextView descriptionContr = null;
        private TextView descriptionContrHeader = null;
        private LinearLayout descriptionContrLayout = null;
        private ImageButton contrInfoButton;

        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            init();
            descriptionContr.setText("");

            String[] descriptionContrList = new String[]{"нет данных", "0.00 ₽"};
            String ItemID = myContr.getString(myContr.getColumnIndex("CODE"));
            CurContr = ItemID;
            if (!ItemID.equals("0") && !OrderHeadFragment.isOrderEditedOrCopied) {
                LoadContrAddress(ItemID);
            }

            String[] data = db.getDebetInfoByContrID(ItemID);
            String status = myContr.getString(myContr.getColumnIndex("STATUS"));
            String debt = data[0];

            if (!status.equals("")) {
                descriptionContrList[0] = status;
            }
            if (!debt.equals("")) {
                descriptionContrList[1] = String.format(Locale.ROOT, "%.2f", Float.parseFloat(debt.replace(",", "."))) + " ₽";
            }

            descriptionContr.setText(String.join(" | ", descriptionContrList));

            if (!ItemID.equals("0")) {
                descriptionContrLayout.setVisibility(View.VISIBLE);
                contrInfoButton.setVisibility(View.VISIBLE);
                changeStatusColor(descriptionContr, descriptionContrHeader, ItemID);
            } else {
                descriptionContrLayout.setVisibility(View.GONE);
                contrInfoButton.setVisibility(View.INVISIBLE);
            }
        }

        private void changeStatusColor(TextView tvStatus, TextView tvStatusHeader, String contrId) {
            switch (db.getContrFlagByID(contrId)) {
                case 0:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusOk));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusOk));
                    break;
                case 1:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusWarn));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusWarn));
                    break;
                case 2:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusBad));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusBad));
                    break;
            }
        }

        private void init() {
            descriptionContr = getActivity().findViewById(R.id.description);
            descriptionContrHeader = getActivity().findViewById(R.id.descriptionHeader);
            descriptionContrLayout = getActivity().findViewById(R.id.layoutOfContrStatus);
            contrInfoButton = getActivity().findViewById(R.id.userCardInfoButton);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public OrderHeadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.order_head_frargment_main, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null && getArguments().size() != 0) {
            isOrderEditedOrCopied = isCopiedLocal = getArguments().getBoolean("isOrderEditedOrCopied");
            _TP = getArguments().getString("TP");
            _CONTR = getArguments().getString("CONTR");
            _ADDR = getArguments().getString("ADDR");
            _DATE = getArguments().getString("DELIVERY_DATE");
            _COMMENT = getArguments().getString("COMMENT");

            getArguments().clear();
        }

        db = new DBHelper(getActivity().getApplicationContext());
        FormOrderFragment.TypeOfPrice = db.getPriceType(CONTR_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar = getActivity().findViewById(R.id.toolbar);
        edContrFilter = getActivity().findViewById(R.id.txtContrFilter);
        txtComment = getActivity().findViewById(R.id.txtComment);

        spinContr = getActivity().findViewById(R.id.SpinContr);
        spinAddress = getActivity().findViewById(R.id.SpinAddr);
        TPList = getActivity().findViewById(R.id.SpinTP);

        Button returnMoneyButton = getActivity().findViewById(R.id.returnMoneyButton);
        returnMoneyButton.setOnClickListener(this);

        ImageButton userCardInfoButton = getActivity().findViewById(R.id.userCardInfoButton);
        userCardInfoButton.setOnClickListener(this);
        edContrFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String ContrFilter = edContrFilter.getText().toString();
                if (ContrFilter.length() != 0) {
                    LoadFilteredContrList(ContrFilter);
                } else {
                    LoadContrList();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        DeliveryDate = Calendar.getInstance();
        txtDate = getActivity().findViewById(R.id.txtDelivDate);

        final DatePickerDialog.OnDateSetListener date = (view_, year, monthOfYear, dayOfMonth) -> {
            DeliveryDate.set(Calendar.YEAR, year);
            DeliveryDate.set(Calendar.MONTH, monthOfYear);
            DeliveryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd.MM.yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

            txtDate.setText(sdf.format(DeliveryDate.getTime()));
        };
        txtDate.setOnClickListener(v -> new DatePickerDialog(getActivity(), date, DeliveryDate.get(Calendar.YEAR), DeliveryDate.get(Calendar.MONTH), DeliveryDate.get(Calendar.DAY_OF_MONTH)).show());
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoadTpList();
        LoadContrList();

        int contrRowId = db.getContrRowID();
        SetSelectedContrByRowId(contrRowId);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = settings.edit();
        String stTP_ID = settings.getString("TP_ID", "0");

        int TPRowId = db.getTPRowID();
        int TPDefaultRowId = db.GetTPByID(stTP_ID);

        if (CheckTPLock()) {
            TPList.setSelection(TPDefaultRowId);
        } else {
            assert stTP_ID != null;
            if (stTP_ID.equals("0")) {
                TPList.setSelection(TPRowId);
            } else {
                if (TPRowId != TPDefaultRowId && TPRowId != 0) {
                    TPList.setSelection(TPRowId);
                } else {
                    TPList.setSelection(TPDefaultRowId);
                }
            }
        }

        putAlreadyFilledFields();

        if (isOrderEditedOrCopied) {
            int tpID = db.GetTPByID(_TP);
            int contrID = db.GetContrByID(_CONTR);

            TPList.setSelection(tpID, false);
            spinContr.setSelection(contrID, false);

            String[] dateArray = _DATE.split("\\.");
            DeliveryDate.set(Calendar.YEAR, Integer.parseInt(dateArray[2]));
            DeliveryDate.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1);
            DeliveryDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[0]));
            String myFormat = "dd.MM.yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

            txtDate.setText(sdf.format(DeliveryDate.getTime()));
            txtComment.setText(_COMMENT);

            isOrderEditedOrCopied = false;
        }

        setContrAndSumValue(db, toolbar, FormOrderFragment.isSales);
    }

    private void putAlreadyFilledFields() {
        String comment = db.GetComment();
        String deliverDate = db.GetDeliveryDate();
        String tpID = settings.getString("TP_ID", "0");
        String contrID = settings.getString("CONTR_ID", "0");


        if (!comment.equals("")) {
            txtComment.setText(comment, TextView.BufferType.EDITABLE);
        }

        if (!deliverDate.equals("")) {
            txtDate.setText(deliverDate, TextView.BufferType.EDITABLE);
        }

        if (TPList != null) {
            SetSelectedSpinnerByCode(TPList, tpID);
        }

        if (spinContr != null) {
            SetSelectedSpinnerByCode(spinContr, contrID);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.returnMoneyButton) {
            String currentText = txtComment.getText().toString();
            if (currentText.equals("")) {
                txtComment.setText(getResources().getString(R.string.takeOutMoney));
            } else {
                txtComment.setText(String.format("%s, %s", currentText, getResources().getString(R.string.takeOutMoney).toLowerCase()));
            }
        } else if (view.getId() == R.id.userCardInfoButton) {
            CounterAgentInfo counterAgentInfo = db.getCounterAgentInfo(CurContr);
            CounterAgentDialogFragment dialogFragment = new CounterAgentDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("counterAgentInfo", counterAgentInfo);

            dialogFragment.setArguments(bundle);
            dialogFragment.show(getFragmentManager(), "counterAgentDialogFragment");
        }
    }

    public void SetSelectedSpinnerByCode(Spinner spinner, String code) {
        try {
            for (int i = 0; i < spinner.getCount(); i++) {
                Cursor value = (Cursor) spinner.getItemAtPosition(i);
                String id = value.getString(value.getColumnIndexOrThrow("CODE"));
                if (code.equals(id)) {
                    spinner.setSelection(i, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetSelectedContrByRowId(int rowId) {
        try {
            for (int i = 0; i < spinContr.getCount(); i++) {
                Cursor value = (Cursor) spinContr.getItemAtPosition(i);
                int id = value.getInt(value.getColumnIndexOrThrow("_id"));
                if (rowId == id) {
                    spinContr.setSelection(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        String AddressId;

        spTp = Objects.requireNonNull(getActivity()).findViewById(R.id.ColTPID);
        spContr = getActivity().findViewById(R.id.ColContrID);
        spAddress = getActivity().findViewById(R.id.ColContrAddrID);

        CONTR_ID = spContr.getText().toString();
        CONTR_ID = CONTR_ID.trim();

        AddressId = spAddress != null ? spAddress.getText().toString() : "0";

        String DeliveryDate = txtDate.getText().toString();
        String Comment = txtComment.getText().toString();
        TP_ID = spTp.getText().toString();

        if (TP_ID.equals("0") || CONTR_ID.equals("0") || AddressId.equals("0") || DeliveryDate.equals("")) {
            Toast.makeText(getActivity(), "Необходимо заполнить все обязательные поля шапки заказа", Toast.LENGTH_LONG).show();
            return;
        }

        editor.putString("TP_ID", TP_ID);
        editor.putString("CONTR_ID", CONTR_ID);
        editor.commit();

        FormOrderFragment.isContrIdDifferent = true;
        System.out.println(PREVIOUS_CONTR_ID + " " + CONTR_ID + " " + isCopiedLocal + " " + !PREVIOUS_CONTR_ID.equals(CONTR_ID));
        if (PREVIOUS_CONTR_ID.equals("") || PREVIOUS_CONTR_ID == null || CONTR_ID == null || CONTR_ID.equals("")) {
            PREVIOUS_CONTR_ID = CONTR_ID;
            DBHelper.pricesMap.clear();
        } else if (!PREVIOUS_CONTR_ID.equals(CONTR_ID)) {
            PREVIOUS_CONTR_ID = CONTR_ID;
            updateNomenPrice(isCopiedLocal);
        }

        FormOrderFragment.TypeOfPrice = db.getPriceType(CONTR_ID);

        if (db.insertOrder(TP_ID, CONTR_ID, AddressId, DeliveryDate, Comment)) {
            setContrAndSumValue(db, toolbar, FormOrderFragment.isSales);
        } else {
            if (db.UpdateOrderHead(TP_ID, CONTR_ID, AddressId, DeliveryDate, Comment)) {
                setContrAndSumValue(db, toolbar, FormOrderFragment.isSales);
            } else {
                Toast.makeText(getActivity(), "Вы уже заполнили шапку заказа, либо не удалось обновить шапку заказа", Toast.LENGTH_LONG).show();
            }
        }
//        glbVars.putAllPrices();
        closeAllCursorsInHeadOrder();
        goToFormOrderFragment();
    }

    //    @AsyncUI
    public void LoadTpList() {
        if (myTP != null) {
            myTP.close();
        }
        myTP = db.getTpList();
        TPAdapterSQLite adapter = new TPAdapterSQLite(getActivity(), R.layout.tp_layout, myTP, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColTPID, R.id.ColTPDescr}, 0);
        TPList.setAdapter(adapter);
    }

    //    @AsyncUI
    public void LoadContrList() {
        spinContr.setAdapter(null);
        if (myContr != null) {
            myContr.close();
        }
        myContr = db.getContrList();
        ContractorAdapterSQLite adapter = new ContractorAdapterSQLite(getActivity(), R.layout.contr_layout, myContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    public void LoadFilteredContrList(String FindStr) {
        getActivity().runOnUiThread(() -> {
            spinContr.setAdapter(null);
            if (myContr != null) {
                myContr.close();
            }
            myContr = db.getContrFilterList(FindStr);
            ContractorAdapterSQLite adapter = new ContractorAdapterSQLite(getActivity(), R.layout.contr_layout, myContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
            spinContr.setAdapter(adapter);
            spinContr.setOnItemSelectedListener(SelectedContr);
        });
    }

    public void LoadContrAddress(String ContID) {
        getActivity().runOnUiThread(() -> {
            if (myAddress != null) {
                myAddress.close();
            }
            myAddress = db.getContrAddress(ContID);
            AddressAdapterSQLite adapter = new AddressAdapterSQLite(getActivity(), R.layout.addr_layout, myAddress, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrAddrID, R.id.ColContrAddrDescr}, 0);
            spinAddress.setAdapter(adapter);

            String AddrID = db.GetContrAddr();
            if (!AddrID.equals("0")) {
                SetSelectedSpinnerByCode(spinAddress, AddrID);
            } else if (!OrderHeadFragment._ADDR.equals("")) {
                SetSelectedSpinnerByCode(spinAddress, OrderHeadFragment._ADDR);
            }
        });
    }

    public Boolean CheckTPLock() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        return settings.getBoolean("TP_LOCK", false);
    }

    public void closeAllCursorsInHeadOrder() {
        if (myAddress != null) {
            myAddress.close();
        }
        if (myContr != null) {
            myContr.close();
        }
        if (myTP != null) {
            myTP.close();
        }
    }

    public void updateNomenPrice(boolean isCopied) {
//        new Thread(() -> new ProgressBarShower(getContext()).setFunction(() -> {
        db.ResetNomenPrice(isCopied);
//            return null;
//        }).start());
    }
}
