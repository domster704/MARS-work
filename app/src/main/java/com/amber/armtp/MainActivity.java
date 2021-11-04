package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity {
    //Defining Variables
    private static final long SMS_NOTIFY_INTERVAL = 30 * 60 * 1000; // интервал проверки обновления 5 минут
    private static final int LAYOUT = R.layout.activity_main;
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    public SharedPreferences sPref;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public GlobalVars globalVariable;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Fragment fragment = null;
    FragmentTransaction fragmentTransaction;
    TextView tvAppVer, tvLastUpdate;
    Calendar cal;
    Intent SMSIntent;
    SQLiteDatabase UpdateSchemaDB;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (globalVariable.viewFlipper != null) {
            int Lay = globalVariable.viewFlipper.getDisplayedChild();
            if (Lay == 1) {
                globalVariable.ordStatus = null;
                globalVariable.viewFlipper.setDisplayedChild(0);
                globalVariable.OrdersAdapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        globalVariable = (GlobalVars) getApplicationContext();

        globalVariable.setContext(getApplicationContext());

        globalVariable.glbContext = getApplicationContext();

        settings = getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        String[][] ftpPhoto = new String[][]{
                new String[]{"FtpPhotoSrv", getResources().getString(R.string.ftp_server)},
                new String[]{"FtpPhotoUser", getResources().getString(R.string.ftp_user)},
                new String[]{"FtpPhotoPass", getResources().getString(R.string.ftp_pass)},
                new String[]{"AppUpdateSrv", getResources().getString(R.string.ftp_update_server)},
                new String[]{"AppUpdateUser", getResources().getString(R.string.ftp_update_user)},
                new String[]{"AppUpdatePass", getResources().getString(R.string.ftp_update_pass)},
                new String[]{"UpdateSrv", getResources().getString(R.string.ftp_server)},
                new String[]{"sqlPort", getResources().getString(R.string.sql_port)},
                new String[]{"sqlDB", getResources().getString(R.string.sql_db)},
                new String[]{"sqlLogin", getResources().getString(R.string.sql_user)},
                new String[]{"sqlPass", getResources().getString(R.string.sql_pass)},
        };

        for (String[] i : ftpPhoto) {
            String ftpPhotoVar = settings.getString(i[0], "");
            if (Objects.equals(ftpPhotoVar, "")) {
                editor.putString(i[0], i[1]);
                editor.commit();
            }
        }


        if (globalVariable.db == null) {
            globalVariable.db = new DBHepler(getApplicationContext());
        }

        File old_db = new File(globalVariable.GetSDCardpath() + "ARMTP_DB" + "/armtp.db");
        if (old_db.exists()) {
            try {
                FileUtils.copyFile(new File(old_db.toString()), new File(globalVariable.db.getWritableDatabase().getPath()));
                File toName = new File(globalVariable.GetSDCardpath() + "ARMTP_DB/armtp.db_");
                old_db.renameTo(toName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS sgi (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS GRUPS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, SGIID TEXT NOT NULL, DESCR TEXT NOT NULL)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS Nomen (ID TEXT UNIQUE, GRUPID TEXT, COD TEXT, DESCR NUMERIC, OST INTEGER, PRICE NUMERIC, ZAKAZ INT DEFAULT 0, lowDESCR TEXT, SGIID TEXT, CODE TEXT, PHOTO1 TEXT, PHOTO2 TEXT, VKOROB INTEGER DEFAULT 0, ISUPDATED INTEGER DEFAULT 0, ISNEW INTEGER DEFAULT 0, IS7DAY INTEGER DEFAULT 0, IS28DAY INTEGER DEFAULT 0, MP TEXT DEFAULT '', IS_PERM INTEGER DEFAULT 0, SALE_PRICE NUMERIC DEFAULT 0, P1D NUMERIC DEFAULT 0, P2D NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS CONTRS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT, DESCR TEXT, lowDESCR TEXT, CODE TEXT, INSTOP INTEGER, DOLG INTEGER, DYNAMO INTEGER, TP TEXT DEFAULT '', INFO TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ADDRS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,ID TEXT, PARENTEXT TEXT, DESCR TEXT,CODE TEXT, DOP_INFO TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS TORG_PRED (ID TEXT NOT NULL, DESCR TEXT NOT NULL, CODE TEXT, TP_PASS TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [ROW] INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS TMP_DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [ROW] INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ZAKAZY (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DOCNO TEXT, TP_ID TEXT, CONTR_ID BLOB, ADDR_ID TEXT, DOC_DATE REAL, DELIVERY_DATE TEXT, COMMENT TEXT, STATUS INTEGER DEFAULT 0, DELIV_TIME TEXT DEFAULT '', GETMONEY NUMERIC DEFAULT 0, GETBACKWARD NUMERIC DEFAULT 0, BACKTYPE NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ZAKAZY_DT (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ZAKAZ_ID TEXT, NOM_ID TEXT, CODE TEXT, COD5 TEXT, DESCR TEXT, QTY INTEGER, PRICE NUMERIC, IS_OUTED INTEGER DEFAULT 0, OUT_QTY INTEGER DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ORDERS (TP_ID TEXT, CONTR_ID TEXT, ADDR_ID TEXT, DATA TEXT, COMMENT TEXT, DELIV_TIME TEXT DEFAULT '', GETMONEY NUMERIC DEFAULT 0, GETBACKWARD NUMERIC DEFAULT 0, BACKTYPE NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS SALES (CONTR_ID TEXT, GRUP_ID TEXT, SALE NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS CEN_TYPES (ROW_ID INTEGER, CEN_ID TEXT, DESCR TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS TP_GRUP_ACCESS (TP_ID TEXT, SGI_ID TEXT, GRUP_ID TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS sgi_FULL_IDX ON sgi(rowid, ID);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS GRUPS_FULL_IDX ON GRUPS(rowid, ID, SGIID);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS Nomen_FULL_IDX ON Nomen(ID, GRUPID, COD, DESCR, lowDESCR, ost, ZAKAZ, SGIID, ISUPDATED);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS CONTR_FULL_IDX ON CONTRS(ROWID, ID,  DESCR, lowDESCR);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS CONTRS_IDX ON CONTRS(ID);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS TP_IDX ON TORG_PRED(ID);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ADDRS_FULL_IDX ON ADDRS(rowid, ID, PARENTEXT);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS DEBET_FULL_IDX ON DEBET(ROWID, [ROW], CONTR_ID, TP_ID, TP_IDS);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ORDERS_FULL_IDX ON ORDERS(TP_ID, CONTR_ID, ADDR_ID, DATA);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_FULL_IDX ON ZAKAZY(ROWID, DOCNO, TP_ID, CONTR_ID, ADDR_ID, DOC_DATE, STATUS);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_DT_FULL_IDX ON ZAKAZY_DT(ROWID, ZAKAZ_ID, NOM_ID);");

        } catch (SQLiteException e) {
            e.printStackTrace();
        }


        if (globalVariable.SmsDB == null) {
            globalVariable.SmsDB = openOrCreateDatabase("armtp_msg.db", MODE_MULTI_PROCESS, null);
        }

        try {
            globalVariable.SmsDB.execSQL("CREATE TABLE IF NOT EXISTS MSGS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW_ID INTEGER, TP_ID TEXT, TP_IDS TEXT, MSG_HEAD TEXT, MESSAGE TEXT,MSG_DATE TEXT, MSG_TIME TEXT, IS_NEW INTEGER DEFAULT 1)");
        } catch (SQLiteException ignored) {
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(LAYOUT);

        if (!globalVariable.appDBFolder.exists()) {
            globalVariable.appDBFolder.mkdir();
        }

        if (!globalVariable.appPhotoFolder.exists()) {
            globalVariable.appPhotoFolder.mkdir();
        }

        if (!globalVariable.appUpdatesFolder.exists()) {
            globalVariable.appUpdatesFolder.mkdir();
        }

        cal = Calendar.getInstance();
        UpdateDbSchema();
        // Initializing Toolbar and setting it as the actionbar
        initToolbar();
        initNavigationView();
        // Initializing NavigationView
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // Checking if the item is in checked state or not, if not make it in checked state
                menuItem.setChecked(!menuItem.isChecked());

                // Closing drawer on item click
                drawerLayout.closeDrawers();

                // Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    // Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_update_data:
                        DisplayFragment(new UpdateDataFragment(), "frag_update_data");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_journal:
                        DisplayFragment(new JournalFragment(), "frag_journal");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
//                    case R.id.nav_form_order:
//                        DisplayFragment(new FormOrderFragment(), "frag_form_order");
//                        setToolbarTitle(menuItem.getTitle());
//                        return true;
                    case R.id.nav_order_header:
                        DisplayFragment(new OrderHeadFragment(), "frag_order_header");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_send_orders:
                        DisplayFragment(new SendOrdersFragment(), "frag_send_orders");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_update_app:
                        DisplayFragment(new UpdateAppFragment(), "frag_update_app");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_debet:
                        DisplayFragment(new DebetFragment(), "frag_debet");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
//                    case R.id.nav_back_messages:
//                        DisplayFragment(new BackSMSFragment(), "frag_back_messages");
//                        setToolbarTitle(menuItem.getTitle());
//                        return true;
                    case R.id.nav_admin:
                        DisplayFragment(new SettingFragment(), "frag_admin");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_exit:
                        finish();
                        System.exit(0);
                        return true;
                    default:
                        DisplayFragment(new DefaultFragment(), "frag_default");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
                initVersion();
                initLastUpdate();
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        fragment = new DefaultFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_default");
        fragmentTransaction.commit();
        setToolbarTitle("Общая информация");


        if (!globalVariable.isServiceRunning(CheckSMS.class)) {
            SMSIntent = new Intent(this, CheckSMS.class);
            PendingIntent pSMSintent = PendingIntent.getService(this, 0, SMSIntent, 0);

            AlarmManager alarm2 = (AlarmManager) getSystemService(getApplicationContext().ALARM_SERVICE);
            alarm2.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), SMS_NOTIFY_INTERVAL, pSMSintent);
        }

        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void initNavigationView() {
        drawerLayout = findViewById(R.id.drawer);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ShortcutBadger.applyCount(this, 10);
    }

    @Override
    public void onPause() {
        super.onPause();
        Integer count = globalVariable.getSMSCount();
        ShortcutBadger.applyCount(getApplicationContext(), count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (globalVariable.isServiceRunning(CheckSMS.class)) {
            stopService(SMSIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void DisplayFragment(Fragment Frag, String Tag) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (Frag != null && !Tag.equals("")) {
            fragment = Frag;
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment, Tag);
            fragmentTransaction.commit();
        } else {
            Toast.makeText(getApplicationContext(), "Заданы неккоректные данные для загрузки", Toast.LENGTH_LONG).show();
        }
    }

    public void setToolbarTitle(CharSequence Title) {
        toolbar.setTitle(Title);
    }

    private void initVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String version = String.valueOf(pInfo.versionCode);
        String versionName = String.valueOf(pInfo.versionName);
        tvAppVer = findViewById(R.id.tvAppVersion);
        if (tvAppVer != null) {
            tvAppVer.setText(version + " ( сборка " + versionName + ")");
        }
    }

    private void initLastUpdate() {
        tvLastUpdate = findViewById(R.id.tvLastUpdateText);
        if (tvLastUpdate != null) {
            tvLastUpdate.setText(globalVariable.ReadLastUpdate());
        }
    }

    private void UpdateDbSchema() {
        if (globalVariable.db == null) {
            globalVariable.db = new DBHepler(getApplicationContext());
            UpdateSchemaDB = globalVariable.db.getWritableDatabase();
        } else {

            UpdateSchemaDB = globalVariable.db.getWritableDatabase();

            //Изменения от 28-06-2019
            try {
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS sgi (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS GRUPS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, SGIID TEXT NOT NULL, DESCR TEXT NOT NULL)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS Nomen (ID TEXT UNIQUE, GRUPID TEXT, COD TEXT, DESCR NUMERIC, OST INTEGER, PRICE NUMERIC, ZAKAZ INT DEFAULT 0, lowDESCR TEXT, SGIID TEXT, CODE TEXT, PHOTO1 TEXT, PHOTO2 TEXT, VKOROB INTEGER DEFAULT 0, ISUPDATED INTEGER DEFAULT 0, ISNEW INTEGER DEFAULT 0, IS7DAY INTEGER DEFAULT 0, IS28DAY INTEGER DEFAULT 0, MP TEXT DEFAULT '', IS_PERM INTEGER DEFAULT 0, SALE_PRICE NUMERIC DEFAULT 0, P1D NUMERIC DEFAULT 0, P2D NUMERIC DEFAULT 0)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS CONTRS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT, DESCR TEXT, lowDESCR TEXT, CODE TEXT, INSTOP INTEGER, DOLG INTEGER, DYNAMO INTEGER, TP TEXT DEFAULT '', INFO TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS ADDRS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,ID TEXT, PARENTEXT TEXT, DESCR TEXT,CODE TEXT, DOP_INFO TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TORG_PRED (ID TEXT NOT NULL, DESCR TEXT NOT NULL, CODE TEXT, TP_PASS TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [ROW] INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TMP_DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [ROW] INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS ZAKAZY (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DOCNO TEXT, TP_ID TEXT, CONTR_ID BLOB, ADDR_ID TEXT, DOC_DATE REAL, DELIVERY_DATE TEXT, COMMENT TEXT, STATUS INTEGER DEFAULT 0, DELIV_TIME TEXT DEFAULT '', GETMONEY NUMERIC DEFAULT 0, GETBACKWARD NUMERIC DEFAULT 0, BACKTYPE NUMERIC DEFAULT 0)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS ZAKAZY_DT (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ZAKAZ_ID TEXT, NOM_ID TEXT, CODE TEXT, COD5 TEXT, DESCR TEXT, QTY INTEGER, PRICE NUMERIC, IS_OUTED INTEGER DEFAULT 0, OUT_QTY INTEGER DEFAULT 0)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS ORDERS (TP_ID TEXT, CONTR_ID TEXT, ADDR_ID TEXT, DATA TEXT, COMMENT TEXT, DELIV_TIME TEXT DEFAULT '', GETMONEY NUMERIC DEFAULT 0, GETBACKWARD NUMERIC DEFAULT 0, BACKTYPE NUMERIC DEFAULT 0)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS SALES (CONTR_ID TEXT, GRUP_ID TEXT, SALE NUMERIC DEFAULT 0)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS CEN_TYPES (ROW_ID INTEGER, CEN_ID TEXT, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TP_GRUP_ACCESS (TP_ID TEXT, SGI_ID TEXT, GRUP_ID TEXT)");
                //Обновление от 17-04-2020. Создание таблиц матрицы. Товарная категория. Функциональная группа. Бренд. Демпризнак. Производитель/Импортер
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS FUNC (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TOVCAT (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS BRAND (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS WC (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS PROD (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS FOCUS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT, LONG_DESCR TEXT, BDATE TEXT, EDATE TEXT)");

                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS UNI_MATRIX (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, TYPE_ID TEXT NOT NULL DEFAULT '', TYPE_DESCR TEXT NOT NULL DEFAULT '', ID TEXT NOT NULL, DESCR TEXT, LOWDESCR TEXT)");


                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS sgi_FULL_IDX ON sgi(rowid, ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS GRUPS_FULL_IDX ON GRUPS(rowid, ID, SGIID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_FULL_IDX ON Nomen(ID, GRUPID, COD, DESCR, lowDESCR, ost, ZAKAZ, SGIID, ISUPDATED);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS CONTR_FULL_IDX ON CONTRS(ROWID, ID,  DESCR, lowDESCR);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS CONTRS_IDX ON CONTRS(ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS TP_IDX ON TORG_PRED(ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ADDRS_FULL_IDX ON ADDRS(rowid, ID, PARENTEXT);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS DEBET_FULL_IDX ON DEBET(ROWID, [ROW], CONTR_ID, TP_ID, TP_IDS);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ORDERS_FULL_IDX ON ORDERS(TP_ID, CONTR_ID, ADDR_ID, DATA);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_FULL_IDX ON ZAKAZY(ROWID, DOCNO, TP_ID, CONTR_ID, ADDR_ID, DOC_DATE, STATUS);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_DT_FULL_IDX ON ZAKAZY_DT(ROWID, ZAKAZ_ID, NOM_ID);");

                //Обновление от 17-04-2020. Создание индексов таблиц матрицы. Товарная категория. Функциональная группа. Бренд. Демпризнак. Производитель/Импортер
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS func_FULL_IDX ON FUNC(rowid, ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS tovcat_FULL_IDX ON TOVCAT(rowid, ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS brand_FULL_IDX ON BRAND(rowid, ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS wc_FULL_IDX ON WC(rowid, ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS prod_FULL_IDX ON PROD(rowid, ID);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS focus_FULL_IDX ON FOCUS(rowid, ID, BDATE, EDATE);");
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS uni_matrix_FULL_IDX ON UNI_MATRIX(rowid, TYPE_ID, ID, DESCR, LOWDESCR);");

            } catch (SQLiteException ignored) {
            }

            class SQLData {
                public final String tableName;
                public final String column;
                public final String defaultValue;

                public SQLData(String tableName, String column, String defaultValue) {
                    this.tableName = tableName;
                    this.column = column;
                    this.defaultValue = defaultValue;
                }
            }

            SQLData[] sqlData = new SQLData[]{
                    new SQLData("CONTRS", "INFO", "''"),
                    new SQLData("DEBET", "FIRMA", "''"),
                    new SQLData("TMP_DEBET", "FIRMA", "''"),
                    new SQLData("TMP_DEBET", "P1D", "0"),
                    new SQLData("TMP_DEBET", "P2D", "0"),
                    new SQLData("CONTRS", "CRT_DATE", "''"),
                    new SQLData("Nomen", "TOVCATID", "''"),
                    new SQLData("Nomen", "FUNCID", "''"),
                    new SQLData("Nomen", "BRANDID", "''"),
                    new SQLData("Nomen", "WCID", "''"),
                    new SQLData("Nomen", "PRODID", "''"),
                    new SQLData("Nomen", "FOCUSID", "''"),
                    new SQLData("BRAND", "LOWDESCR", "''"),
                    new SQLData("FOCUS", "LOWDESCR", "''"),
                    new SQLData("FOCUS", "LOWLONG_DESCR", "''"),
                    new SQLData("FUNC", "LOWDESCR", "''"),
                    new SQLData("GRUPS", "LOWDESCR", "''"),
                    new SQLData("PROD", "LOWDESCR", "''"),
                    new SQLData("TOVCAT", "LOWDESCR", "''"),
                    new SQLData("WC", "LOWDESCR", "''"),
                    new SQLData("sgi", "LOWDESCR", "''"),
                    new SQLData("Nomen", "FOCUSID", "''"),
                    new SQLData("Nomen", "MODELID", "''"),
                    new SQLData("Nomen", "SIZEID", "''"),
                    new SQLData("Nomen", "COLORID", "''"),
            };

            for (SQLData i : sqlData) {
                try {
                    UpdateSchemaDB.execSQL("ALTER TABLE " + i.tableName + " ADD COLUMN " + i.column + " TEXT DEFAULT " + i.defaultValue);
                } catch (SQLiteException ignored) {
                }
            }

            try {
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_MX_FULL_IDX ON Nomen(ID, GRUPID, SGIID, TOVCATID, FUNCID, BRANDID, WCID, PRODID);");
            } catch (SQLiteException ignored) {
            }

            try {
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_MX_FOCUS_FULL_IDX ON Nomen(ID, GRUPID, SGIID, TOVCATID, FUNCID, BRANDID, WCID, PRODID, FOCUSID);");
            } catch (SQLiteException ignored) {
            }

            try {
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_MX_MDL_SZ_CLR_FULL_IDX ON Nomen(ID, GRUPID, SGIID, TOVCATID, FUNCID, BRANDID, WCID, PRODID, FOCUSID, MODELID, SIZEID, COLORID);");
            } catch (SQLiteException ignored) {
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
