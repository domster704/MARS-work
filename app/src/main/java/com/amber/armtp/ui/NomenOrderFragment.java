package com.amber.armtp.ui;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amber.armtp.extra.Config;
import com.amber.armtp.ImageAdapter;
import com.amber.armtp.PhotoDotsAdapter;
import com.amber.armtp.ProgressBarLoading;
import com.amber.armtp.R;
import com.amber.armtp.adapters.NomenAdapterSQLite;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.extra.ExtraFunctions;
import com.amber.armtp.extra.PhotoDownloadingRunnable;
import com.amber.armtp.interfaces.TBUpdate;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Updated by domster704 on 02.12.2023
 */
public class NomenOrderFragment extends Fragment implements TBUpdate {
    public static boolean isSales;
    public boolean isMultiSelect = false;

    protected DBHelper db;
    protected DBAppHelper dbApp;
    protected DBOrdersHelper dbOrders;
    public Cursor myNom = null;
    public GridView nomenList;
    protected View rootView;

    protected android.support.v7.widget.Toolbar toolbar;
    protected Handler photoThreadhandler;
    protected NomenAdapterSQLite NomenAdapter, PreviewZakazAdapter;
    protected String CurSGI = "0", CurGroup = "0", CurWCID = "0", CurFocusID = "0", CurSearchName = "";
    public static String kod5 = "";
    public int MultiQty = 0;
//    public float Discount = 0;
    protected boolean isNeededToSelectRowAfterGoToGroup = false;

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);

        db = new DBHelper(getActivity().getApplicationContext());
        dbApp = new DBAppHelper(getActivity().getApplicationContext());
        dbOrders = new DBOrdersHelper(getActivity().getApplicationContext());

        toolbar = getActivity().findViewById(R.id.toolbar);

        nomenList = getActivity().findViewById(R.id.listContrs);

        setContrAndSumValue(db, toolbar, isSales);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.form_order_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        this.rootView = rootView;

        return rootView;
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        resetCurData();

        photoThreadhandler = new Handler(message -> {
            Bundle bundle = message.getData();
            switch (message.what) {
                case PhotoDownloadingRunnable.MESSAGE_UPDATE_DB:
                    String fileName = bundle.getString("fileName");
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE FOTO=? or FOTO2=?", new Object[]{fileName, fileName});
                    break;
                case PhotoDownloadingRunnable.MESSAGE_SHOW_PRODUCTS:
                    String[] fileNames = bundle.getStringArray("fileNames");
                    String kod5 = bundle.getString("kod5");
                    getActivity().runOnUiThread(() -> showProductPhoto(fileNames, kod5));
                    break;
            }
            return false;
        });

        Config.hideKeyBoard(getActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setContrAndSumValue(db, toolbar, isSales);
    }

    public void downloadAndShowPhotos(final String[] fileNames, String kod5, boolean isForced) {
        ProgressBarLoading.pgThread = new Thread(new PhotoDownloadingRunnable(fileNames, kod5, isForced, getActivity(), photoThreadhandler));
        ProgressBarLoading.pgThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showProductPhoto(String[] photoFileName, String kod5) {
        AlertDialog alertPhoto;
        String photoDir = ExtraFunctions.getPhotoDir(getContext());
        photoFileName = Arrays.stream(photoFileName).filter(Objects::nonNull).toArray(String[]::new);
        if (photoFileName.length == 0) {
            Config.sout("Нет скачанных файлов", getContext());
            return;
        }

        for (String s : photoFileName) {
            checkPhotoInDB(s);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.image_layout, null));
        builder.setCancelable(true);
        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        alertPhoto = builder.create();
        alertPhoto.getWindow().setLayout(600, 400);
        alertPhoto.show();

        TextView productID = alertPhoto.findViewById(R.id.nomenId);
        productID.setText(String.format("Код товара:   %s", kod5));

        ViewPager viewPager = alertPhoto.findViewById(R.id.photoViewPager);

        LinearLayout dots = alertPhoto.findViewById(R.id.layoutForPhotoDots);
        PhotoDotsAdapter photoDotsAdapter = new PhotoDotsAdapter(getContext(), photoFileName.length, 0);
        photoDotsAdapter.fillLayout(dots);

        ImageAdapter adapter = new ImageAdapter(getContext(), photoFileName, photoDir);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }

            @Override
            public void onPageSelected(int i) {
                photoDotsAdapter.changePosition(i);
            }
        });
    }

    public void checkPhotoInDB(String FileName) {
        Cursor cur;
        String isDownloaded = FilenameUtils.removeExtension(FileName);
        String sqlStr;

        if (isDownloaded.equals("_2")) {
            sqlStr = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded.replace(isDownloaded, "") + "'";
        } else {
            sqlStr = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded + "'";
        }
        cur = db.getWritableDatabase().rawQuery(sqlStr, null);

        if (cur.moveToFirst()) {
            if (cur.getInt(0) == 0) {
                if (isDownloaded.equals("_2")) {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded.replace(isDownloaded, "") + "'");
                } else {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded + "'");
                }
            }
        }
        cur.close();
        GridView gdNomen = getActivity().getWindow().getDecorView().findViewById(R.id.listContrs);
        myNom.requery();
        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();
        gdNomen.invalidateViews();
    }

    public AdapterView.OnLongClickListener PhotoLongClick = new AdapterView.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            try {
                TextView tvKod5 = ((RelativeLayout) view.getParent()).findViewById(R.id.ColNomCod);
                String kod5 = tvKod5.getText().toString();

                PopupMenu nomPopupMenu = new PopupMenu(getContext(), view);
                nomPopupMenu.getMenuInflater().inflate(R.menu.photo_context_menu, nomPopupMenu.getMenu());

                nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.forceDownloadPhoto) {
                        @SuppressLint({"NewApi", "LocalSuppress"}) String[] fileNames = db.getPhotoNames(kod5);
                        downloadAndShowPhotos(fileNames, kod5, true);
                    }
                    return true;
                });
                nomPopupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e, getContext());
            }
            return true;
        }
    };

    public AdapterView.OnItemClickListener GridNomenClick = (myAdapter, myView, position, mylng) -> {
        try {
            long viewId = myView.getId();

            if (viewId == R.id.ColNomPhoto) {
                showPhoto(myView, position, myAdapter);
            } else if (viewId == R.id.btPlus) {
                plusQTY(myView);
            } else if (viewId == R.id.btMinus) {
                minusQTY(myView);
            } else {
                TextView tvKOD5 = myView.findViewById(R.id.ColNomCod);

                String ID = tvKOD5.getText().toString();

                if (isMultiSelect) {
                    multiSelect(ID);
                } else {
                    fillNomenDataFromAlertDialog(ID);
                }
                if (PreviewZakazAdapter != null)
                    PreviewZakazAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Config.sout(e, getContext());
        }
    };

    protected void plusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        TextView tvPrice = parent.findViewById(R.id.ColNomPrice);

        String price = tvPrice.getText().toString();
        String kod5 = tvKOD5.getText().toString();
        getActivity().runOnUiThread(() -> {
            db.putPriceInNomen(kod5, price);
            db.PlusQty(kod5);
            if (myNom != null)
                myNom.requery();

            if (NomenAdapter != null)
                NomenAdapter.notifyDataSetChanged();

            if (PreviewZakazAdapter != null)
                PreviewZakazAdapter.notifyDataSetChanged();
        });
    }

    protected void minusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        String kod5 = tvKOD5.getText().toString();
        getActivity().runOnUiThread(() -> {
            db.MinusQty(kod5);
            if (myNom != null)
                myNom.requery();

            if (NomenAdapter != null)
                NomenAdapter.notifyDataSetChanged();

            if (PreviewZakazAdapter != null)
                PreviewZakazAdapter.notifyDataSetChanged();
        });
    }

    protected void showPhoto(View myView, int position, AdapterView<?> myAdapter) {
        getActivity().runOnUiThread(() -> {
            if (((TextView) myView).getText() == null || ((TextView) myView).getText().toString().equals(""))
                return;

            long ID = myAdapter.getItemIdAtPosition(position);

            @SuppressLint({"NewApi", "LocalSuppress"}) String[] fileNames = db.getPhotoNames(db.getProductKod5ByRowID(ID));
            downloadAndShowPhotos(fileNames, db.getProductKod5ByRowID(ID), false);
        });
    }

    protected void multiSelect(String ID) {
        getActivity().runOnUiThread(() -> {
            db.UpdateQty(ID, MultiQty);
            if (myNom != null)
                myNom.requery();
            if (NomenAdapter != null)
                NomenAdapter.notifyDataSetChanged();

            db.putPriceInNomen(ID, "" + DBHelper.pricesMap.get(ID));
            setContrAndSumValue(db, toolbar, isSales);
        });
    }

    protected void fillNomenDataFromAlertDialog(String ID) {
        getActivity().runOnUiThread(() -> {
            try {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View promptView = layoutInflater.inflate(R.layout.change_qty, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setView(promptView);

                final EditText input = promptView.findViewById(R.id.edPPQty);
                final TextView txtCod = promptView.findViewById(R.id.txtNomCode);
                final TextView txtDescr = promptView.findViewById(R.id.txtNomDescr);
                final TextView txtOst = promptView.findViewById(R.id.txtNomOst);
                final TextView txtGroup = promptView.findViewById(R.id.txtNomGroup);

                try {
                    input.setText(myNom.getString(myNom.getColumnIndex("ZAKAZ")));
                    txtCod.setText(myNom.getString(myNom.getColumnIndex("KOD5")));
                    txtDescr.setText(myNom.getString(myNom.getColumnIndex("DESCR")));
                    txtOst.setText(myNom.getString(myNom.getColumnIndex("OST")));
                } catch (Exception e1) {
                    Config.sout(e1, getContext());
                }

                try {
                    Cursor c = db.getReadableDatabase().rawQuery("SELECT DESCR FROM GRUPS WHERE CODE=?", new String[]{myNom.getString(myNom.getColumnIndex("GRUPPA"))});
                    c.moveToNext();
                    String groupDescription = c.getString(c.getColumnIndex("DESCR"));
                    txtGroup.setText(groupDescription);
                    c.close();
                } catch (Exception e2) {
                    Config.sout(e2, getContext());
                }

                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("OK", (dialog, id) -> {
                        })
                        .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

                final AlertDialog alertD = alertDialogBuilder.create();
                alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                WindowManager.LayoutParams wmlp = alertD.getWindow().getAttributes();
                wmlp.gravity = Gravity.TOP | Gravity.START;
                wmlp.x = 50;
                wmlp.y = 15;

                alertD.show();
                input.requestFocus();
                input.selectAll();
                input.performClick();
                input.setPressed(true);
                input.invalidate();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    TextView tvPrice = rootView.findViewById(R.id.ColNomPrice);
                    String price = tvPrice.getText().toString();
                    db.putPriceInNomen(ID, price);

                    db.UpdateQty(ID, Integer.parseInt(input.getText().toString()));
                    myNom.requery();

                    setContrAndSumValue(db, toolbar, isSales);

                    if (NomenAdapter != null)
                        NomenAdapter.notifyDataSetChanged();

                    if (PreviewZakazAdapter != null)
                        PreviewZakazAdapter.notifyDataSetChanged();

                    alertD.dismiss();
                    Config.hideKeyBoard(getActivity());
                });
                input.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    }
                    return true;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e, getContext());
            }
        });
    }

    public void resetCurData() {
        CurGroup = CurWCID = CurFocusID = CurSGI = "0";
        CurSearchName = "";

        if (NomenAdapter != null) {
            getActivity().runOnUiThread(() -> NomenAdapter.setDiscount(0f));
        }

        Menu menu = FormOrderFragment.mainMenu;
        if (menu != null && menu.size() > 1) {
            setIconColor(menu, R.id.NomenDiscount, false);
        }
    }

    public void setIconColor(Menu menu, int MenuItem, boolean vis) {
        getActivity().runOnUiThread(() -> {
            if (menu.findItem(MenuItem) == null)
                return;
            Drawable drawable = menu.findItem(MenuItem).getIcon();
            drawable.mutate();
            if (vis) {
                drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            } else {
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        });
    }
}
