package com.amber.armtp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;

/**
 * Панель админа
 */
public class AdminFragment extends Fragment {
    private final int LOCATION_SDPATH = 1;
    private final int LOCATION_PHOTO_PATH = 2;
    public GlobalVars glbVars;
    SharedPreferences settings, PriceSettings, settingPaths;
    SharedPreferences.Editor editor, PriceEditor, settingPathEditor;
    Button btClearSgi,
            btClearGroups,
            btClearNomen,
            btClearContrs,
            btClearAddrs,
            btClearTP,
            btClearDebet,
            btClearAll,
            btCompactDb,
            btCheckDb,
            btDeleteDBF,
            btClearZakazy,
            btClearZakazyDt,
            btDeleteCanceledZakaz,
            btUnlockTP,
            btLockTP,
            btSaveUpdateSrv;
    Button btSetPhotoPath, btSetSDPath;
    Button btnLogin, btnCancel;
    TextView tvPhotoPath, tvSDPath, tbPublicPhoto;
    Switch swTestLoadPhoto;
    EditText etPass, etUpdateSrv, etSqlPort, etSqlLogin, etSqlPass, etSqlDB;
    EditText etFtpPhoto, etFtpPhotoUser, etFtpPhotoPass;
    EditText etFtpUpdate, etFtpUpdateUser, etFtpUpdatePass;
    Dialog login;
    Boolean isSelectPath = false;
    String CenTypeID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.admin_fragment, container, false);
        glbVars.view = v;
        return v;
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

    /**
     * Получение элементов фрагмента и изменение их параметров
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        TabHost tabs = getActivity().findViewById(R.id.tabHost);

        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("tag1");

        spec.setContent(R.id.TabDB);
        spec.setIndicator("База данных");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag2");
        spec.setContent(R.id.TabDBF);
        spec.setIndicator("DBF");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag3");
        spec.setContent(R.id.TabPaths);
        spec.setIndicator("Пути");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag4");
        spec.setContent(R.id.TabPrices);
        spec.setIndicator("Цены");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag5");
        spec.setContent(R.id.TabUpdateSrv);
        spec.setIndicator("Сервера обновлений");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        glbVars.spinCenTypes = getActivity().findViewById(R.id.spnCentype);
        glbVars.LoadCenTypes();

        tvPhotoPath = getActivity().findViewById(R.id.tvPhotoPath);
        tvSDPath = getActivity().findViewById(R.id.tvSDPath1);
        tbPublicPhoto = getActivity().findViewById(R.id.tvPublicPhoto);
        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        PriceSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        PriceEditor = PriceSettings.edit();
        settingPaths = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tvPhotoPath.setText(settingPaths.getString("PhotoPath", ""));
        tvSDPath.setText(settingPaths.getString("SDPath", ""));
        tbPublicPhoto.setText(glbVars.getPhotoDir());
        settingPathEditor = settingPaths.edit();

        btClearSgi = getActivity().findViewById(R.id.btClearSgi);
        btClearGroups = getActivity().findViewById(R.id.btClearGroups);
        btClearNomen = getActivity().findViewById(R.id.btClearNomen);
        btClearContrs = getActivity().findViewById(R.id.btClearContrs);
        btClearAddrs = getActivity().findViewById(R.id.btClearAddrs);
        btClearTP = getActivity().findViewById(R.id.btClearTP);
        btClearDebet = getActivity().findViewById(R.id.btClearDebet);
        btCompactDb = getActivity().findViewById(R.id.btCompactDB);
        btClearZakazy = getActivity().findViewById(R.id.btClearZakazy);
        btClearZakazyDt = getActivity().findViewById(R.id.btClearZakazyDt);
        btClearAll = getActivity().findViewById(R.id.btClearAll);
        btCheckDb = getActivity().findViewById(R.id.btCheckDB);
        btDeleteDBF = getActivity().findViewById(R.id.btDeleteDBF);
        btUnlockTP = getActivity().findViewById(R.id.btUnlockTP);
        btLockTP = getActivity().findViewById(R.id.btLockTp);
        btSaveUpdateSrv = getActivity().findViewById(R.id.btSaveSrv);

        btDeleteCanceledZakaz = getActivity().findViewById(R.id.btDeleteCanceledZakaz);

        btSetPhotoPath = getActivity().findViewById(R.id.btSetImagesPath);
        btSetSDPath = getActivity().findViewById(R.id.btSetSDPath);

        swTestLoadPhoto = getActivity().findViewById(R.id.swTestLoadPhoto);

        etUpdateSrv = getActivity().findViewById(R.id.edUpdateServer);
        etSqlPort = getActivity().findViewById(R.id.edSqlPort);
        etSqlLogin = getActivity().findViewById(R.id.edSqlLogin);
        etSqlPass = getActivity().findViewById(R.id.edSqlPass);
        etSqlDB = getActivity().findViewById(R.id.edSqlDB);

        etUpdateSrv.setText(settings.getString("UpdateSrv", getResources().getString(R.string.sql_server)));
        etSqlPort.setText(settings.getString("sqlPort", getResources().getString(R.string.sql_port)));
        etSqlLogin.setText(settings.getString("sqlLogin", getResources().getString(R.string.sql_user)));
        etSqlPass.setText(settings.getString("sqlPass", getResources().getString(R.string.sql_pass)));
        etSqlDB.setText(settings.getString("sqlDB", getResources().getString(R.string.sql_db)));

        etFtpUpdate = getActivity().findViewById(R.id.edFtpUpdate);
        etFtpUpdatePass = getActivity().findViewById(R.id.edFtpUpdatePass);
        etFtpUpdateUser = getActivity().findViewById(R.id.edFtpUpdateUser);

        etFtpUpdate.setText(settings.getString("AppUpdateSrv", getResources().getString(R.string.ftp_update_server)));
        etFtpUpdatePass.setText(settings.getString("AppUpdatePass", getResources().getString(R.string.ftp_update_pass)));
        etFtpUpdateUser.setText(settings.getString("AppUpdateUser", getResources().getString(R.string.ftp_update_user)));

        etFtpPhoto = getActivity().findViewById(R.id.edFtpPhoto);
        etFtpPhotoPass = getActivity().findViewById(R.id.edFtpPhotoPass);
        etFtpPhotoUser = getActivity().findViewById(R.id.edFtpPhotoUser);

        etFtpPhoto.setText(settings.getString("FtpPhotoSrv", getResources().getString(R.string.ftp_server)));
        etFtpPhotoPass.setText(settings.getString("FtpPhotoPass", getResources().getString(R.string.ftp_pass)));
        etFtpPhotoUser.setText(settings.getString("FtpPhotoUser", getResources().getString(R.string.ftp_user)));

        CenTypeID = settings.getString("usr_centype", "");

        boolean TestLoad = settingPaths.getBoolean("TestingLoad", false);

        swTestLoadPhoto.setChecked(TestLoad);
    }

    @Override
    public void onPause() {
        super.onPause();
        initButtons(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        initButtons(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isSelectPath) {
            login = new Dialog(getActivity());
            login.setContentView(R.layout.login_dialog);
            login.setTitle("Авторизация");
            login.setCancelable(false);
            login.setCanceledOnTouchOutside(false);
            btnLogin = login.findViewById(R.id.btnLogin);
            btnCancel = login.findViewById(R.id.btnCancel);
            etPass = login.findViewById(R.id.etPassword);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etPass.getText().toString().equals("01072019")) {
                        login.dismiss();
                        initButtons(true);
                    }
                }
            });

            etPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        btnLogin.performClick();
                    }
                    return true;
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login.dismiss();
                }
            });
            login.show();
        }

        int CenTypeRowid = glbVars.db.GetCenTypeRowID(CenTypeID);
        SetSelectedCenType(CenTypeRowid);

        btClearSgi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы СГИ");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM sgi; DELETE FROM sqlite_sequence WHERE name = 'sgi';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы товарных групп");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM GRUPS; DELETE FROM sqlite_sequence WHERE name = 'GRUPS';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearNomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы номенклатуры");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM Nomen; DELETE FROM sqlite_sequence WHERE name = 'Nomen';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearContrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы контрагентов");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM CONTRS; DELETE FROM sqlite_sequence WHERE name = 'CONTRS';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearAddrs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы адресов");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ADDRS; DELETE FROM sqlite_sequence WHERE name = 'ADDRS';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы торговых представителей");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM TORG_PRED; DELETE FROM sqlite_sequence WHERE name = 'TORG_PRED';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();

            }
        });

        btClearDebet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы дебиторки");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM DEBET; DELETE FROM sqlite_sequence WHERE name = 'DEBET';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблиц");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM sgi; DELETE FROM sqlite_sequence WHERE name = 'sgi';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM GRUPS; DELETE FROM sqlite_sequence WHERE name = 'GRUPS';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM Nomen; DELETE FROM sqlite_sequence WHERE name = 'Nomen';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM CONTRS; DELETE FROM sqlite_sequence WHERE name = 'CONTRS';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM ADDRS; DELETE FROM sqlite_sequence WHERE name = 'ADDRS';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM TORG_PRED; DELETE FROM sqlite_sequence WHERE name = 'TORG_PRED';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM DEBET; DELETE FROM sqlite_sequence WHERE name = 'DEBET';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY; DELETE FROM sqlite_sequence WHERE name = 'ZAKAZY';");
                                glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT; DELETE FROM sqlite_sequence WHERE name = 'ZAKAZY_DT';");
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btCompactDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Сжатие базы данных");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("VACUUM");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btCheckDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Проверка целостности базы данных");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Boolean result;
                        result = glbVars.db.getWritableDatabase().isDatabaseIntegrityOk();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (result) {
                                    Toast.makeText(getActivity(), "База данных в полном порядке", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "База данных не совсем в порядке", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        btDeleteDBF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Удаление DBF файлов");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File dir = FileUtils.getFile(glbVars.GetSDCardpath() + glbVars.DBFolder + "/");
                        File delFile;
                        for (String file : dir.list(new SuffixFileFilter(".dbf"))) {
//                            delFile = new File(glbVars.GetSDCardpath()+glbVars.DBFolder+"/"+file);
                            delFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + file);
                            //String FileName =  getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/orders_"+curdate.toString()+".dbf";
                            FileUtils.deleteQuietly(delFile);
                            final File finalDelFile = delFile;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setTitle(finalDelFile.toString());
                                }
                            });
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearZakazy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы заказов");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY; DELETE FROM sqlite_sequence WHERE name = 'ZAKAZY';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btClearZakazyDt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Очистка таблицы заказов (табличная часть)");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT; DELETE FROM sqlite_sequence WHERE name = 'ZAKAZY_DT';");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btDeleteCanceledZakaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(false);
                progressDialog.setTitle("Удаление отмененных/удаленных заказов");
                progressDialog.setMessage("Пожалуйста подождите...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZY_DT.ZAKAZ_ID IN (SELECT DOCNO FROM ZAKAZY WHERE ZAKAZY.STATUS=5 OR ZAKAZY.STATUS=99);");
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY WHERE STATUS=5 OR STATUS=99;");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }
        });

        btSetPhotoPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, LOCATION_PHOTO_PATH);
            }
        });

        btSetSDPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, LOCATION_SDPATH);
            }
        });

        swTestLoadPhoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settingPathEditor.putBoolean("TestingLoad", true);
                    settingPathEditor.commit();
                } else {
                    settingPathEditor.putBoolean("TestingLoad", false);
                    settingPathEditor.commit();
                }

            }
        });

        btUnlockTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPathEditor.putBoolean("TP_LOCK", false);
                settingPathEditor.commit();
            }
        });

        btLockTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingPathEditor.putBoolean("TP_LOCK", true);
                settingPathEditor.commit();
            }
        });

        btSaveUpdateSrv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("UpdateSrv", etUpdateSrv.getText().toString());
                editor.putString("sqlPort", etSqlPort.getText().toString());
                editor.putString("sqlLogin", etSqlLogin.getText().toString());
                editor.putString("sqlPass", etSqlPass.getText().toString());
                editor.putString("sqlDB", etSqlDB.getText().toString());

                editor.putString("FtpPhotoSrv", etFtpPhoto.getText().toString());
                editor.putString("FtpPhotoPass", etFtpPhotoPass.getText().toString());
                editor.putString("FtpPhotoUser", etFtpPhotoUser.getText().toString());

                editor.putString("AppUpdateSrv", etFtpUpdate.getText().toString());
                editor.putString("AppUpdatePass", etFtpUpdatePass.getText().toString());
                editor.putString("AppUpdateUser", etFtpUpdateUser.getText().toString());

                editor.commit();
            }
        });
    }


    private void initButtons(Boolean EnableButtons) {
        btClearSgi.setEnabled(EnableButtons);
        btClearGroups.setEnabled(EnableButtons);
        btClearNomen.setEnabled(EnableButtons);
        btClearContrs.setEnabled(EnableButtons);
        btClearAddrs.setEnabled(EnableButtons);
        btClearTP.setEnabled(EnableButtons);
        btClearDebet.setEnabled(EnableButtons);
        btCompactDb.setEnabled(EnableButtons);
        btClearZakazy.setEnabled(EnableButtons);
        btClearZakazyDt.setEnabled(EnableButtons);
        btClearAll.setEnabled(EnableButtons);
        btCheckDb.setEnabled(EnableButtons);
        btDeleteDBF.setEnabled(EnableButtons);
        btDeleteCanceledZakaz.setEnabled(EnableButtons);
        btSetPhotoPath.setEnabled(EnableButtons);
        btSetSDPath.setEnabled(EnableButtons);
        btUnlockTP.setEnabled(EnableButtons);
        btLockTP.setEnabled(EnableButtons);
        btSaveUpdateSrv.setEnabled(EnableButtons);
        etSqlDB.setEnabled(EnableButtons);
        etSqlPass.setEnabled(EnableButtons);
        etSqlLogin.setEnabled(EnableButtons);
        etSqlPort.setEnabled(EnableButtons);
        etUpdateSrv.setEnabled(EnableButtons);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            Uri uri = resultData.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), resultData.getData());
            if (requestCode == LOCATION_PHOTO_PATH) {
                settingPathEditor.putString("PhotoPath", resultData.getDataString());
                settingPathEditor.putString("PhotoPathName", pickedDir.getName());
                settingPathEditor.commit();
                tvPhotoPath.setText(resultData.getDataString());
            } else if (requestCode == LOCATION_SDPATH) {
                settingPathEditor.putString("SDPath", resultData.getDataString());
                settingPathEditor.putString("SDPathName", pickedDir.getName());
                settingPathEditor.commit();
                tvSDPath.setText(resultData.getDataString());
            }
            initButtons(true);
            isSelectPath = true;
        }
    }

    public void SetSelectedCenType(int ROWID) {
        for (int i = 0; i < glbVars.spinCenTypes.getCount(); i++) {
            Cursor value = (Cursor) glbVars.spinCenTypes.getItemAtPosition(i);
            int id = value.getInt(value.getColumnIndexOrThrow("_id"));
            if (ROWID == id) {
                glbVars.spinCenTypes.setSelection(i);
                break;
            }
        }
    }
}