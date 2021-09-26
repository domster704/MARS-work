package com.amber.armtp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FormOrderFragment extends Fragment {
    Menu mainMenu;
    SharedPreferences settings;
    SharedPreferences APKsettings;
    SharedPreferences.Editor editor;
    SearchView searchView;
    MenuItem searchItem;
    View thisView;
    TextView txtSgi, txtGroup, tvHeadCod, tvHeadDescr, tvHeadMP, tvHeadZakaz;
    TextView FilterSgi_ID, FilterGroup_ID, FilterTovcat_ID, FilterFunc_ID, FilterBrand_ID, FilterWC_ID, FilterProd_ID, FilterFocus_ID, FilterModel_ID, FilterColor_ID;
    TextView UnIFilterTypeID, UniFilterID;
    private android.support.v7.widget.Toolbar toolbar;
    public GlobalVars glbVars;

    public FormOrderFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.form_order_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        thisView = rootView;
        glbVars.view = rootView;
        return rootView;
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
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.form_order_menu, menu);
        mainMenu = menu;

        if (glbVars.db.CheckForSales() > 0) {
            mainMenu.getItem(2).setEnabled(false);
            glbVars.setSaleIcon(mainMenu, 1, true);
        } else {
            mainMenu.getItem(2).setEnabled(true);
            glbVars.setSaleIcon(mainMenu, 1, false);
        }

        glbVars.setDiscountIcon(mainMenu, 2, glbVars.isDiscount);

        searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Поиск номенклатуры");
        searchView.setOnQueryTextListener(searchTextListner);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.NomenMultiPos:
                LayoutInflater lInf = LayoutInflater.from(getActivity());
                View RangeDlgView;

                RangeDlgView = lInf.inflate(R.layout.change_range_qty, null);
                AlertDialog.Builder RangeDlg = new AlertDialog.Builder(getActivity());
                RangeDlg.setView(RangeDlgView);

                final EditText edBeginPP = RangeDlgView.findViewById(R.id.edBeginPP);
                final EditText edEndPP = RangeDlgView.findViewById(R.id.edEndPP);
                final EditText edPPQty = RangeDlgView.findViewById(R.id.edPPQty);
                edBeginPP.setText((glbVars.BeginPos != 0 ? String.valueOf(glbVars.BeginPos) : "0"));
                edEndPP.setText((glbVars.EndPos != 0 ? String.valueOf(glbVars.EndPos) : "0"));
                edPPQty.setText("0");

                RangeDlg
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDlg = RangeDlg.create();
                alertDlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                alertDlg.show();

                EditText edInput;

                if (glbVars.BeginPos != 0 && glbVars.EndPos != 0) {
                    edInput = edPPQty;
                } else if (glbVars.BeginPos != 0) {
                    if (glbVars.EndPos != 0) {
                        edInput = edPPQty;
                    } else {
                        edInput = edEndPP;
                    }
                } else {
                    edInput = edBeginPP;
                }

                edInput.requestFocus();
                edInput.selectAll();
                edInput.performClick();
                edInput.setPressed(true);
                edInput.invalidate();
                InputMethodManager immPP = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                immPP.showSoftInput(edInput, InputMethodManager.SHOW_IMPLICIT);

                alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edBeginPP.getText().toString() != "" && edEndPP.getText().toString() != "" && edPPQty.getText().toString() != "") {
                            glbVars.UpdateNomenRange(Integer.parseInt(edBeginPP.getText().toString()), Integer.parseInt(edEndPP.getText().toString()), Integer.parseInt(edPPQty.getText().toString()));
                            glbVars.BeginPos = 0;
                            glbVars.EndPos = 0;
                            alertDlg.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Необходимо указать начальную позицию, конечную позицию и нужное количество", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                edBeginPP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            if (!edBeginPP.getText().toString().equals("")) {
                                edEndPP.requestFocus();
                                edEndPP.selectAll();
                                edEndPP.performClick();
                                edEndPP.setPressed(true);
                                edEndPP.invalidate();
                            }
                        }
                        return true;
                    }
                });

                edEndPP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            if (!edEndPP.getText().toString().equals("")) {
                                edPPQty.requestFocus();
                                edPPQty.selectAll();
                                edPPQty.performClick();
                                edPPQty.setPressed(true);
                                edPPQty.invalidate();
                            }
                        }
                        return true;
                    }
                });

                return true;
            case R.id.NomenGotoBegin:
                glbVars.nomenList.setSelection(0);
                return true;
            case R.id.NomenGotoEnd:
                glbVars.nomenList.setSelection(glbVars.nomenList.getCount());
                return true;
            case R.id.NomenSales:

                if (glbVars.isSales) {
                    mainMenu.getItem(2).setEnabled(true);
                    glbVars.setSaleIcon(mainMenu, 1, false);
                } else {
                    glbVars.setDiscountIcon(mainMenu, 2, false);
                    glbVars.setSaleIcon(mainMenu, 1, true);
                }

                glbVars.db.calcSales(glbVars.db.GetContrID());

                if (glbVars.NomenAdapter != null) {
                    glbVars.myNom.requery();
                    glbVars.NomenAdapter.notifyDataSetChanged();
                }
                setContrAndSum();
                if (glbVars.isDiscount) {
                    glbVars.isDiscount = false;
                    glbVars.Discount = 0;
                    mainMenu.getItem(2).setEnabled(false);
                    glbVars.setDiscountIcon(mainMenu, 2, false);
                }
                return true;

            case R.id.NomenDiscount:
                glbVars.CalculatePercentSale(mainMenu, 0);
                return true;
            case R.id.NomenMultiSelect:
                if (!glbVars.isMultiSelect) {

                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                    View promptView;

                    promptView = layoutInflater.inflate(R.layout.multi_qty, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setView(promptView);

                    final EditText input = promptView.findViewById(R.id.txtPercent);
                    input.setText(String.valueOf(glbVars.MultiQty));

                    alertDialogBuilder
                            .setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    final AlertDialog alertD = alertDialogBuilder.create();
                    alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    alertD.show();
                    input.requestFocus();
                    input.selectAll();
                    input.performClick();
                    input.setPressed(true);
                    input.invalidate();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                    alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Boolean wantToCloseDialog = false;
                            glbVars.isMultiSelect = true;
                            glbVars.MultiQty = Integer.parseInt(input.getText().toString());
                            item.setIcon(getActivity().getResources().getDrawable(R.drawable.checkbox_marked));
                            alertD.dismiss();
                        }
                    });

                    input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                            }
                            return true;
                        }
                    });

                } else {
                    glbVars.isMultiSelect = false;
                    item.setIcon(getResources().getDrawable(R.drawable.checkbox_free));
                }

                return true;

            case R.id.NomenFilters:
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                final View promptView;
                promptView = layoutInflater.inflate(R.layout.nom_filters, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setView(promptView);
                glbVars.SetSelectedSgi("0", "0");
                glbVars.SetSelectedGrup("0");

                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNeutralButton("Сбросить фильтры", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                ;

                final AlertDialog alertD = alertDialogBuilder.create();
                alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                alertD.show();
                glbVars.LoadFiltersSgi(promptView);
                glbVars.LoadFiltersGroups(promptView);
                glbVars.LoadFiltersTovcat(promptView);
                glbVars.LoadFiltersFunc(promptView);
                glbVars.LoadFiltersBrand(promptView);
                glbVars.LoadFiltersWC(promptView);
                glbVars.LoadFiltersProd(promptView);
                glbVars.LoadFiltersFocus(promptView);
                glbVars.LoadFiltersModels(promptView);
                glbVars.LoadFiltersColors(promptView);

                String SgiFID = settings.getString("ColSgiFID", "0");
                String GrupFID = settings.getString("ColGrupFID", "0");
                String TovcatID = settings.getString("ColTovcatID", "0");
                String FuncID = settings.getString("ColFuncID", "0");
                String BrandID = settings.getString("ColBrandID", "0");
                String WCID = settings.getString("ColWCID", "0");
                String ProdID = settings.getString("ColProdID", "0");
                String FocusID = settings.getString("ColFocusID", "0");
                String ModelID = settings.getString("ColModelID", "0");
                String ColorID = settings.getString("ColColorID", "0");

                if (!SgiFID.equals("0")) {
                    glbVars.SetSelectedFilterSgi(SgiFID);
                }

                if (!GrupFID.equals("0")) {
                    glbVars.SetSelectedFilterGroup(GrupFID);
                }

                if (!TovcatID.equals("0")) {
                    glbVars.SetSelectedFilterTovcat(TovcatID);
                }

                if (!FuncID.equals("0")) {
                    glbVars.SetSelectedFilterFunc(FuncID);
                }

                if (!BrandID.equals("0")) {
                    glbVars.SetSelectedFilterBrand(BrandID);
                }

                if (!WCID.equals("0")) {
                    glbVars.SetSelectedFilterWC(WCID);
                }

                if (!ProdID.equals("0")) {
                    glbVars.SetSelectedFilterProd(ProdID);
                }

                if (!FocusID.equals("0")) {
                    glbVars.SetSelectedFilterFocus(FocusID);
                }

                if (!ModelID.equals("0")) {
                    glbVars.SetSelectedFilterModel(ModelID);
                }

                if (!ColorID.equals("0")) {
                    glbVars.SetSelectedFilterColor(ColorID);
                }
//                    input.requestFocus();
//                    input.selectAll();
//                    input.performClick();
//                    input.setPressed(true);
//                    input.invalidate();
//                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
//                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FilterSgi_ID = promptView.findViewById(R.id.ColSgiFID);
                        FilterGroup_ID = promptView.findViewById(R.id.ColGroupFID);
                        FilterTovcat_ID = promptView.findViewById(R.id.ColTovcatID);
                        FilterFunc_ID = promptView.findViewById(R.id.ColFuncID);
                        FilterBrand_ID = promptView.findViewById(R.id.ColBrandID);
                        FilterWC_ID = promptView.findViewById(R.id.ColWCID);
                        FilterProd_ID = promptView.findViewById(R.id.ColProdID);
                        FilterFocus_ID = promptView.findViewById(R.id.ColFocusID);
                        FilterModel_ID = promptView.findViewById(R.id.ColModelID);
                        FilterColor_ID = promptView.findViewById(R.id.ColColorID);

                        editor.putString("ColSgiFID", FilterSgi_ID.getText().toString());
                        editor.putString("ColGrupFID", FilterGroup_ID.getText().toString());
                        editor.putString("ColTovcatID", FilterTovcat_ID.getText().toString());
                        editor.putString("ColFuncID", FilterFunc_ID.getText().toString());
                        editor.putString("ColBrandID", FilterBrand_ID.getText().toString());
                        editor.putString("ColWCID", FilterWC_ID.getText().toString());
                        editor.putString("ColProdID", FilterProd_ID.getText().toString());
                        editor.putString("ColFocusID", FilterFocus_ID.getText().toString());
                        editor.putString("ColModelID", FilterModel_ID.getText().toString());
                        editor.putString("ColColorID", FilterColor_ID.getText().toString());

                        editor.commit();
//                            System.out.print("FilterProd_ID: " + FilterProd_ID.getText().toString());
                        glbVars.LoadNomByFilters(FilterSgi_ID.getText().toString(), FilterGroup_ID.getText().toString(), FilterTovcat_ID.getText().toString(), FilterFunc_ID.getText().toString(), FilterBrand_ID.getText().toString(), FilterWC_ID.getText().toString(), FilterProd_ID.getText().toString(), FilterFocus_ID.getText().toString(), FilterModel_ID.getText().toString(), FilterColor_ID.getText().toString());
                        alertD.dismiss();
                    }
                });

                alertD.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        glbVars.SetSelectedFilterSgi("0");
                        glbVars.SetSelectedFilterGroup("0");
                        glbVars.SetSelectedFilterTovcat("0");
                        glbVars.SetSelectedFilterFunc("0");
                        glbVars.SetSelectedFilterBrand("0");
                        glbVars.SetSelectedFilterWC("0");
                        glbVars.SetSelectedFilterProd("0");
                        glbVars.SetSelectedFilterFocus("0");
                        glbVars.SetSelectedFilterModel("0");
                        glbVars.SetSelectedFilterColor("0");
                    }
                });

//                    input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                        @Override
//                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                                alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
//                            }
//                            return true;
//                        }
//                    });


                return true;

            case R.id.NomenUniFilters:
                LayoutInflater layoutInflater1 = LayoutInflater.from(getActivity());
                final View promptView1;

                promptView1 = layoutInflater1.inflate(R.layout.nomen_unifilter_layout, null);
                AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(getActivity());
                alertDialogBuilder1.setView(promptView1);
                glbVars.SetSelectedSgi("0", "0");
                glbVars.SetSelectedGrup("0");

                alertDialogBuilder1
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
//                        .setNeutralButton("Сбросить фильтры", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                            }
//                        })
                ;

                final AlertDialog alertD1 = alertDialogBuilder1.create();
                alertD1.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                alertD1.show();
                glbVars.LoadUniFilters(promptView1, "");
                glbVars.txtUniFilter = promptView1.findViewById(R.id.txtUniFilter);

                glbVars.txtUniFilter.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String Filter = glbVars.txtUniFilter.getText().toString();
                        if (Filter.length() != 0) {
                            glbVars.LoadUniFilters(promptView1, Filter);
                        } else {
                            glbVars.LoadUniFilters(promptView1, "");
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });
//                glbVars.LoadFiltersGroups(promptView);
//                glbVars.LoadFiltersTovcat(promptView);
//                glbVars.LoadFiltersFunc(promptView);
//                glbVars.LoadFiltersBrand(promptView);
//                glbVars.LoadFiltersWC(promptView);
//                glbVars.LoadFiltersProd(promptView);
//                glbVars.LoadFiltersFocus(promptView);
//
//                String SgiFID = settings.getString("ColSgiFID", "0");
//                String GrupFID = settings.getString("ColGrupFID", "0");
//                String TovcatID = settings.getString("ColTovcatID", "0");
//                String FuncID = settings.getString("ColFuncID", "0");
//                String BrandID = settings.getString("ColBrandID", "0");
//                String WCID = settings.getString("ColWCID", "0");
//                String ProdID = settings.getString("ColProdID", "0");
//                String FocusID = settings.getString("ColFocusID", "0");

                alertD1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UnIFilterTypeID = promptView1.findViewById(R.id.tvUniTypeID);
                        UniFilterID = promptView1.findViewById(R.id.tvUniID);

//                        editor.putString("ColSgiFID", FilterSgi_ID.getText().toString());
//                        editor.putString("ColGrupFID", FilterGroup_ID.getText().toString());
//                        editor.putString("ColTovcatID", FilterTovcat_ID.getText().toString());
//                        editor.putString("ColFuncID", FilterFunc_ID.getText().toString());
//                        editor.putString("ColBrandID", FilterBrand_ID.getText().toString());
//                        editor.putString("ColWCID", FilterWC_ID.getText().toString());
//                        editor.putString("ColProdID", FilterProd_ID.getText().toString());
//                        editor.putString("ColFocusID", FilterFocus_ID.getText().toString());
//
//                        editor.commit();

                        glbVars.LoadNomByUniFilters(UnIFilterTypeID.getText().toString(), UniFilterID.getText().toString());
                        alertD1.dismiss();
                    }
                });

//                alertD.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        glbVars.SetSelectedFilterSgi("0");
//                        glbVars.SetSelectedFilterGroup("0");
//                        glbVars.SetSelectedFilterTovcat("0");
//                        glbVars.SetSelectedFilterFunc("0");
//                        glbVars.SetSelectedFilterBrand("0");
//                        glbVars.SetSelectedFilterWC("0");
//                        glbVars.SetSelectedFilterProd("0");
//                        glbVars.SetSelectedFilterFocus("0");
//                    }
//                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final SearchView.OnQueryTextListener searchTextListner =
            new SearchView.OnQueryTextListener() {
                boolean isSearchClicked = false;

                @Override
                public boolean onQueryTextChange(String newText) {
                    String ItemID = "";
                    if (glbVars.myGrups != null) {
                        ItemID = glbVars.myGrups.getString(glbVars.myGrups.getColumnIndex("ID"));
                    }

                    if (newText.equals("")) {
                        if (!isSearchClicked) {
                            glbVars.LoadNom(ItemID);
                            searchView.clearFocus();
                            searchView.setIconified(true);
                        }
                        return true;
                    } else {
                        if (newText.length() >= 1) {
                            if (!ItemID.equals("0")) {
                                glbVars.SearchNomInGroup(newText, ItemID);
                                return true;
                            } else {
                                glbVars.LoadNom(ItemID);
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.equals("")) {
                        glbVars.SearchNom(query);
                        glbVars.spSgi.setSelection(0);
                        glbVars.spGrup.setAdapter(null);
                        isSearchClicked = true;
                        searchView.clearFocus();
                        searchView.setIconified(true);
                        return true;
                    } else {
                        return false;
                    }
                }
            };

    @Override
    public void onPause() {

        super.onPause();
        txtGroup = getActivity().findViewById(R.id.ColGrupID);
        txtSgi = getActivity().findViewById(R.id.ColSgiID);

        glbVars.isMultiSelect = false;
        glbVars.MultiQty = 0;
        if (txtSgi != null && txtGroup != null) {
            editor.putString("ColSgiID", txtSgi.getText().toString());
            editor.putString("ColGrupID", txtGroup.getText().toString());
            editor.putInt("ColPosition", glbVars.nomenList.getFirstVisiblePosition());
            editor.commit();
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        String SgiID = settings.getString("ColSgiID", "0");
        String GrupID = settings.getString("ColGrupID", "0");
        int VisiblePos = settings.getInt("ColPosition", 0);
        glbVars.nomenList.setSelection(VisiblePos);

        if (!SgiID.equals("0")) {
            glbVars.LoadGroups(SgiID);
            glbVars.SetSelectedSgi(SgiID, GrupID);
            glbVars.SetSelectedGrup(GrupID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);

        glbVars.nomenList = getActivity().findViewById(R.id.listContrs);
        glbVars.spSgi = getActivity().findViewById(R.id.SpinSgi);
        glbVars.spGrup = getActivity().findViewById(R.id.SpinGrups);

        tvHeadCod = getActivity().findViewById(R.id.tvHeadCod);
        tvHeadDescr = getActivity().findViewById(R.id.tvHeadDescr);
        tvHeadMP = getActivity().findViewById(R.id.tvHeadMP);
        tvHeadZakaz = getActivity().findViewById(R.id.tvHeadZakaz);

        settings = getActivity().getSharedPreferences("form_order", 0);
        editor = settings.edit();

        glbVars.LoadSgi();
        if (glbVars.frSgi != "" && glbVars.frSgi != null) {
            glbVars.SetSelectedSgi(glbVars.frSgi, glbVars.frGroup);
            glbVars.LoadGroups(glbVars.frSgi);
            glbVars.SetSelectedGrup(glbVars.frGroup);
        }

        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum);
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    private void setContrAndSum() {
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum);
    }
}
