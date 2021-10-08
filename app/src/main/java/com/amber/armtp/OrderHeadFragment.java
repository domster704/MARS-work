package com.amber.armtp;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * Фрагмент "Шапка заказа"
 */
public class OrderHeadFragment extends Fragment {
    public GlobalVars glbVars;
    Menu mainMenu;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    android.support.v7.widget.Toolbar toolbar;

    public OrderHeadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.order_head_fragment, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        glbVars.view = rootView;
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.edContrFilter = getActivity().findViewById(R.id.txtContrFilter);
        glbVars.txtComment = getActivity().findViewById(R.id.txtComment);
        glbVars.btSave = getActivity().findViewById(R.id.btSaveHeader);
        glbVars.btClear = getActivity().findViewById(R.id.btClearOrder);

        glbVars.spinContr = getActivity().findViewById(R.id.SpinContr);
        glbVars.spinAddr = getActivity().findViewById(R.id.SpinAddr);
        glbVars.TPList = getActivity().findViewById(R.id.SpinTP);

        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle("Заказ на сумму " + OrderSum);

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
        glbVars.DeliveryTime = Calendar.getInstance();

        glbVars.txtDate = getActivity().findViewById(R.id.txtDelivDate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                glbVars.DeliveryDate.set(Calendar.YEAR, year);
                glbVars.DeliveryDate.set(Calendar.MONTH, monthOfYear);
                glbVars.DeliveryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd.MM.yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                glbVars.txtDate.setText(sdf.format(glbVars.DeliveryDate.getTime()));
            }

        };

        glbVars.txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(Objects.requireNonNull(getActivity()), date, glbVars.DeliveryDate.get(Calendar.YEAR), glbVars.DeliveryDate.get(Calendar.MONTH), glbVars.DeliveryDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        glbVars.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ADDR_ID;

                glbVars.spTp = getActivity().findViewById(R.id.ColTPID);
                glbVars.spContr = getActivity().findViewById(R.id.ColContrID);
                glbVars.spAddr = getActivity().findViewById(R.id.ColContrAddrID);

                String TP_ID = glbVars.spTp.getText().toString();
                String CONTR_ID = glbVars.spContr.getText().toString();
                String CurContr = glbVars.db.GetContrID();

                if (!CurContr.equals(CONTR_ID)) {
                    glbVars.db.resetContrSales();
                    setContrAndSum();
                }

                if (glbVars.spAddr != null) {
                    ADDR_ID = glbVars.spAddr.getText().toString();
                } else {
                    ADDR_ID = "0";
                }

                String DeliveryDate = glbVars.txtDate.getText().toString();
                String Comment = glbVars.txtComment.getText().toString();

                if (TP_ID.equals("0") || CONTR_ID.equals("0") || ADDR_ID.equals("0") || DeliveryDate.equals("")) {
                    Toast.makeText(getActivity(), "Необходимо заполнить все обязательные поля шапки заказа", Toast.LENGTH_LONG).show();
                    return;
                }

                editor.putString("TP_ID", TP_ID);
                editor.commit();

                if (glbVars.db.CheckTPAccess(TP_ID) > 0) {
                    editor.putBoolean("TP_LOCK", true);
                    editor.commit();
                }

                if (glbVars.db.insertOrder(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment, "", 0, 0, 0L)) {
                    glbVars.db.resetContrSales();
                    setContrAndSum();
                } else {
                    if (glbVars.db.updateOrderHead(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment, "", 0, 0, 0L)) {
                        glbVars.db.resetContrSales();
                        setContrAndSum();
                    } else {
                        Toast.makeText(getActivity(), "Вы уже заполнили шапку заказа, либо не удалось обновить шапку заказа", Toast.LENGTH_LONG).show();
                    }
                }

                goToFormOrderFragment();
            }
        });

        glbVars.btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (glbVars.db.ClearOrderHeader()) {
                    glbVars.spinContr.setSelection(0);
                    glbVars.spinAddr.setAdapter(null);
                    glbVars.txtComment.setText("");
                    glbVars.txtDate.setText("");
                    glbVars.edContrFilter.setText("");
                    glbVars.db.resetContrSales();
                    setContrAndSum();
                    Toast.makeText(getActivity(), "Шапка заказа успешно очищена", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Не удалось очистить шапку заказа", Toast.LENGTH_LONG).show();
                }
            }
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

        setContrAndSum();

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
        Fragment fragment = new FormOrderFragment();
        fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
        fragmentTransaction.commit();
    }

    private void setContrAndSum() {
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum.substring(2) + " руб.");
    }
}
