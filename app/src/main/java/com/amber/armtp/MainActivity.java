package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity {
    //Defining Variables
    private static final long EveryMinutes = 60;
    private static final long DEBET_NOTIFY_INTERVAL = EveryMinutes * 60 * 1000; // интервал проверки обновления 1 час
    private static final long SMS_NOTIFY_INTERVAL = 30 * 60 * 1000; // интервал проверки обновления 5 минут
    private static final int LAYOUT = R.layout.activity_main;
    public TextView SmsMsg;
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
    Intent DebetIntent, SMSIntent;
    TextView tvAppVer, tvLastUpdate;
    Calendar cal;
    SQLiteDatabase UpdateSchemaDB;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private BroadcastReceiver uiUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMsg.setText(intent.getExtras().getString("SmsCount"));
        }
    };

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (globalVariable.viewFlipper != null) {
            int Lay = globalVariable.viewFlipper.getDisplayedChild();
            if (Lay == 1) {
                globalVariable.ordStatus = null;
                globalVariable.viewFlipper.setDisplayedChild(0);
                globalVariable.Orders.requery();
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

//        File file =  new File(DATABASE_FILE_PATH);
//        if(file == null || !file.exists()) {
//            System.out.println("ARMTP_DB not exists");
//        }

        globalVariable = (GlobalVars) getApplicationContext();

        globalVariable.setContext(getApplicationContext());

        globalVariable.glbContext = getApplicationContext();

//        System.out.println("Data dir: " + Environment.getDataDirectory().toString());
//        System.out.println("Root dir: " + Environment.getRootDirectory().toString());
//        System.out.println("External dir: " + Environment.getExternalStorageDirectory().toString());
//        System.out.println("ExternalFiles dir: " + getExternalFilesDir(null).toString());
//        System.out.println("Files dir: " + getFilesDir().toString());

//        File old_db = new File(globalVariable.GetSDCardpath()+"ARMTP_DB"+"/armtp.db");
//        if(old_db.exists()) {
////            System.out.println("armtp.db exists in path " + globalVariable.GetSDCardpath()+"ARMTP_DB"+"/armtp.db");
//            try {
//                FileUtils.copyFile(new File(old_db.toString()), new File(globalVariable.db.getWritableDatabase().getPath()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//            System.out.println("armtp.db not exists");
//        }

        settings = getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        String ftp_photo_srv = settings.getString("FtpPhotoSrv", "");
        if (ftp_photo_srv == "") {
            editor.putString("FtpPhotoSrv", getResources().getString(R.string.ftp_server));
            editor.commit();
        }

        String ftp_photo_user = settings.getString("FtpPhotoUser", "");
        if (ftp_photo_user == "") {
            editor.putString("FtpPhotoUser", getResources().getString(R.string.ftp_user));
            editor.commit();
        }

        String ftp_photo_pass = settings.getString("FtpPhotoPass", "");
        if (ftp_photo_pass == "") {
            editor.putString("FtpPhotoPass", getResources().getString(R.string.ftp_pass));
            editor.commit();
        }

        String ftp_up_server = settings.getString("AppUpdateSrv", "");
        if (ftp_up_server == "") {
            editor.putString("AppUpdateSrv", getResources().getString(R.string.ftp_update_server));
            editor.commit();
        }

        String ftp_up_user = settings.getString("AppUpdateUser", "");
        if (ftp_up_user == "") {
            editor.putString("AppUpdateUser", getResources().getString(R.string.ftp_update_user));
            editor.commit();
        }

        String ftp_up_pass = settings.getString("AppUpdatePass", "");
        if (ftp_up_pass == "") {
            editor.putString("AppUpdatePass", getResources().getString(R.string.ftp_update_pass));
            editor.commit();
        }

        String sql_server = settings.getString("UpdateSrv", "");
        if (sql_server == "") {
            editor.putString("UpdateSrv", getResources().getString(R.string.ftp_server));
            editor.commit();
        }

        String sql_port = settings.getString("sqlPort", "");
        if (sql_port == "") {
            editor.putString("sqlPort", getResources().getString(R.string.sql_port));
            editor.commit();
        }

        String sql_db = settings.getString("sqlDB", "");
        if (sql_db == "") {
            editor.putString("sqlDB", getResources().getString(R.string.sql_db));
            editor.commit();
        }

        String sql_loging = settings.getString("sqlLogin", "");
        if (sql_loging == "") {
            editor.putString("sqlLogin", getResources().getString(R.string.sql_user));
            editor.commit();
        }

        String sql_pass = settings.getString("sqlPass", "");
        if (sql_pass == "") {
            editor.putString("sqlPass", getResources().getString(R.string.sql_pass));
            editor.commit();
        }

        if (globalVariable.db == null) {
            globalVariable.db = new DBHepler(getApplicationContext());
        }

//        System.out.println("sdpath: " + globalVariable.db.getWritableDatabase().getPath());

//        System.out.println("SD Path: " + globalVariable.GetSDCardpath());
        File old_db = new File(globalVariable.GetSDCardpath() + "ARMTP_DB" + "/armtp.db");
        if (old_db.exists()) {
//            System.out.println("armtp.db exists in path " + globalVariable.GetSDCardpath()+"ARMTP_DB"+"/armtp.db");
            try {
                FileUtils.copyFile(new File(old_db.toString()), new File(globalVariable.db.getWritableDatabase().getPath()));
                File toName = new File(globalVariable.GetSDCardpath() + "ARMTP_DB/armtp.db_");
                old_db.renameTo(toName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        File new_db = new File(globalVariable.db.getWritableDatabase().getPath());
////        System.out.println("Database name" + new_db.toString());
////        System.out.println("Download path:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
//        if(new_db.exists()) {
////            System.out.println("armtp.db exists in path " + globalVariable.GetSDCardpath()+"ARMTP_DB"+"/armtp.db");
////            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
//            try {
//                FileUtils.copyFile(new File(new_db.toString()), new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/armtp_bak.db"));
//                FileUtils.copyFile(new File(new_db.toString()), new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/armtp_bak.db"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

//        else {
//            System.out.println("armtp.db not exists");
//        }

//        System.out.println(globalVariable.db.getReadableDatabase().getPath().toString());
        try {
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS sgi (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS GRUPS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, SGIID TEXT NOT NULL, DESCR TEXT NOT NULL)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS Nomen (ID TEXT UNIQUE, GRUPID TEXT, COD TEXT, DESCR NUMERIC, OST INTEGER, PRICE NUMERIC, ZAKAZ INT DEFAULT 0, lowDESCR TEXT, SGIID TEXT, CODE TEXT, PHOTO1 TEXT, PHOTO2 TEXT, VKOROB INTEGER DEFAULT 0, ISUPDATED INTEGER DEFAULT 0, ISNEW INTEGER DEFAULT 0, IS7DAY INTEGER DEFAULT 0, IS28DAY INTEGER DEFAULT 0, MP TEXT DEFAULT '', IS_PERM INTEGER DEFAULT 0, SALE_PRICE NUMERIC DEFAULT 0, P1D NUMERIC DEFAULT 0, P2D NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS CONTRS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT, DESCR TEXT, lowDESCR TEXT, CODE TEXT, INSTOP INTEGER, DOLG INTEGER, DYNAMO INTEGER, TP TEXT DEFAULT '', INFO TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ADDRS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,ID TEXT, PARENTEXT TEXT, DESCR TEXT,CODE TEXT, DOP_INFO TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS TORG_PRED (ID TEXT NOT NULL, DESCR TEXT NOT NULL, CODE TEXT, TP_PASS TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS TMP_DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
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
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS DEBET_FULL_IDX ON DEBET(ROWID, ROW, CONTR_ID, TP_ID, TP_IDS);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ORDERS_FULL_IDX ON ORDERS(TP_ID, CONTR_ID, ADDR_ID, DATA);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_FULL_IDX ON ZAKAZY(ROWID, DOCNO, TP_ID, CONTR_ID, ADDR_ID, DOC_DATE, STATUS);");
            globalVariable.db.getWritableDatabase().execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_DT_FULL_IDX ON ZAKAZY_DT(ROWID, ZAKAZ_ID, NOM_ID);");

        } catch (SQLiteException e) {
        }

//        System.out.println("Data dir: " + Environment.getDataDirectory().toString());
//        System.out.println("Root dir: " + Environment.getRootDirectory().toString());
//        System.out.println("External dir: " + Environment.getExternalStorageDirectory().toString());
//        System.out.println("ExternalFiles dir: " + getExternalFilesDir(null).toString());
//        System.out.println("Files dir: " + getFilesDir().toString());

        if (globalVariable.SmsDB == null) {
//            globalVariable.SmsDB = openOrCreateDatabase(globalVariable.GetStoragePath2019() + globalVariable.DBFolder + "/armtp_msg.db", MODE_MULTI_PROCESS, null);
//            globalVariable.SmsDB = openOrCreateDatabase(globalVariable.GetSDCardpath() + globalVariable.DBFolder + "/armtp_msg.db", MODE_MULTI_PROCESS, null);
            globalVariable.SmsDB = openOrCreateDatabase("armtp_msg.db", MODE_MULTI_PROCESS, null);
//            System.out.println(globalVariable.SmsDB.getPath().toString());
        }

        try {
            globalVariable.SmsDB.execSQL("CREATE TABLE IF NOT EXISTS MSGS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW_ID INTEGER, TP_ID TEXT, TP_IDS TEXT, MSG_HEAD TEXT, MESSAGE TEXT,MSG_DATE TEXT, MSG_TIME TEXT, IS_NEW INTEGER DEFAULT 1)");
//            globalVariable.SmsDB.execSQL("CREATE TABLE IF NOT EXISTS MSGS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW INTEGER, TP_ID TEXT, TP_IDS TEXT, MSG_HEAD TEXT, MESSAGE TEXT,MSG_DATE TEXT, MSG_TIME TEXT, IS_NEW INTEGER DEFAULT 1))");
        } catch (SQLiteException e) {
        }

//        if (globalVariable.geoDB==null){
////            globalVariable.geoDB = openOrCreateDatabase(globalVariable.GetSDCardpath() + globalVariable.DBFolder + "/armtp_geo.db", MODE_MULTI_PROCESS, null);
////            globalVariable.geoDB = openOrCreateDatabase(globalVariable.GetStoragePath2019() + globalVariable.DBFolder + "/armtp_geo.db", MODE_MULTI_PROCESS, null);
//            globalVariable.geoDB = openOrCreateDatabase("armtp_geo.db", MODE_MULTI_PROCESS, null);
//        }
//
//        try {
//            globalVariable.geoDB.execSQL("CREATE TABLE IF NOT EXISTS GEOPOS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, latitude TEXT, longitude TEXT, address TEXT, data TEXT DEFAULT '', time TEXT DEFAULT '')");
//        } catch (SQLiteException e) {
//        }

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
        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_update_data:
                        DisplayFragment(new update_data_Fragment(), "frag_update_data");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_jounal:
                        DisplayFragment(new journal_Fragment(), "frag_journal");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_form_order:
                        DisplayFragment(new form_order_Fragment(), "frag_form_order");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_view_order:
                        DisplayFragment(new view_order_Fragment(), "frag_view_order");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_order_header:
                        DisplayFragment(new order_head_Fragment(), "frag_order_header");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_send_orders:
                        DisplayFragment(new send_orders_Fragment(), "frag_send_orders");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_update_app:
                        DisplayFragment(new update_app_Fragment(), "frag_update_app");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_debet:
                        DisplayFragment(new debet_Fragment(), "frag_debet");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_downloadimages:
                        DisplayFragment(new images_Fragment(), "frag_images");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_messages:
                        DisplayFragment(new messages_Fragment(), "frag_messages");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_back_messages:
                        DisplayFragment(new backsms_Fragment(), "frag_back_messages");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_admin:
                        DisplayFragment(new admin_Fragment(), "frag_admin");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_start_page:
                        DisplayFragment(new defaul_Fragment(), "frag_default");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_help:
                        DisplayFragment(new help_Fragment(), "frag_help");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
//                    case R.id.nav_user_exit:
//                        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
//                        DisplayFragment(new login_Fragment(), "frag_login");
//                        setToolbarTitle("Авторизация");
//                        return true;
                    case R.id.nav_exit:
                        finish();
                        System.exit(0);
                        return true;
                    default:
                        DisplayFragment(new defaul_Fragment(), "frag_default");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                }

            }
        });
//        globalVariable.gps = new GPSTracker(MainActivity.this);

        SmsMsg = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_messages));
        initSMSCountDrawer();
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
                initSMSCountDrawer();
                initVersion();
                initLastUpdate();
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        fragment = new defaul_Fragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_default");
        fragmentTransaction.commit();
        setToolbarTitle("Общая информация");

//        if (!globalVariable.isServiceRunning(UpdateDebet.class)) {
//            DebetIntent = new Intent(globalVariable.glbContext, UpdateDebet.class);
//            PendingIntent pDebetintent = PendingIntent.getService(globalVariable.glbContext, 0, DebetIntent, 0);
//            AlarmManager alarm = (AlarmManager) getSystemService(globalVariable.glbContext.ALARM_SERVICE);
//            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), DEBET_NOTIFY_INTERVAL, pDebetintent);
//        }

        if (!globalVariable.isServiceRunning(CheckSMS.class)) {
            SMSIntent = new Intent(this, CheckSMS.class);
            PendingIntent pSMSintent = PendingIntent.getService(this, 0, SMSIntent, 0);

            AlarmManager alarm2 = (AlarmManager) getSystemService(getApplicationContext().ALARM_SERVICE);
            alarm2.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), SMS_NOTIFY_INTERVAL, pSMSintent);
        }

//        getSupportActionBar().setHomeButtonEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setDisplayShowHomeEnabled(false);
//        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);
//        actionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initSMSCountDrawer() {
        Integer count = globalVariable.getSMSCount();
        ShortcutBadger.applyCount(getApplicationContext(), count);
        SmsMsg.setGravity(Gravity.CENTER_VERTICAL);
        SmsMsg.setTypeface(null, Typeface.BOLD);
        SmsMsg.setTextColor(getResources().getColor(R.color.colorAccent));
        SmsMsg.setText(count.toString());
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
        registerReceiver(uiUpdated, new IntentFilter("SMS_COUNT"));
        ShortcutBadger.applyCount(this, 10);

//        GPSTracker gps = new GPSTracker(this);
//        if (!globalVariable.gps.isProviderEnabled()) {
//            globalVariable.gps.showSettingsAlert(globalVariable.CurAc);
//        } else {
//            if (globalVariable.gps.canGetLocation()) {
//                double latitude = globalVariable.gps.getLatitude();
//                double longitude = globalVariable.gps.getLongitude();
//                RecStartPosition(latitude, longitude);
//            }
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(uiUpdated);
//        globalVariable.gps.stopUsingGPS();
        Integer count = globalVariable.getSMSCount();
        ShortcutBadger.applyCount(getApplicationContext(), count);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (globalVariable.isServiceRunning(UpdateDebet.class)) {
//            stopService(DebetIntent);
//        }

        if (globalVariable.isServiceRunning(CheckSMS.class)) {
            stopService(SMSIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        String manufacturer = Build.MANUFACTURER;
//        String model = Build.MODEL;
//        String brand = Build.BRAND;
//        System.out.println(model + " " + manufacturer + " " + brand);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Notification.Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.amber.armtp/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

//    private void RecStartPosition(double lat, double longt) {
//        String geoAddress = "";
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//        String currentDate = sdf.format(new Date());
//        sdf = new SimpleDateFormat("HH:mm:ss");
//        String currentTime = sdf.format(new Date());
//
//        if (Geocoder.isPresent()) {
//            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
//            StringBuilder builder = new StringBuilder();
//            try {
//                List<Address> address = geoCoder.getFromLocation(lat, longt, 1);
//                if (address != null && address.size() > 0) {
//                    int maxLines = address.get(0).getMaxAddressLineIndex();
//                    for (int i = 0; i < maxLines; i++) {
//                        String addressStr = address.get(0).getAddressLine(i);
//                        builder.append(addressStr);
//                        builder.append(" ");
//                    }
//                    geoAddress = builder.toString();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            String sql = "INSERT INTO GEOPOS(latitude, longitude, address, data, time)  VALUES (?,?,?,?,?);";
//            SQLiteStatement statement = globalVariable.geoDB.compileStatement(sql);
//            globalVariable.geoDB.beginTransactionNonExclusive();
//            statement.clearBindings();
//            statement.bindString(1, String.valueOf(lat));
//            statement.bindString(2, String.valueOf(longt));
//            statement.bindString(3, geoAddress);
//            statement.bindString(4, currentDate);
//            statement.bindString(5, currentTime);
//            statement.executeInsert();
//            statement.clearBindings();
//            globalVariable.geoDB.setTransactionSuccessful();
//            globalVariable.geoDB.endTransaction();
//        } catch (SQLiteException ex) {
//
//        } finally {
//
//        }
//    }

    private void initVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = String.valueOf(pInfo.versionCode);
        String versionName = String.valueOf(pInfo.versionName);
        tvAppVer = (TextView) findViewById(R.id.tvAppVersion);
        if (tvAppVer != null) {
            tvAppVer.setText(version + " ( сборка " + versionName + ")");
        }
    }

    private void initLastUpdate() {
        tvLastUpdate = (TextView) findViewById(R.id.tvLastUpdateText);
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

//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN VKOROB INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN ISUPDATED INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN ISNEW INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN IS7DAY INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN IS28DAY INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN MP TEXT DEFAULT ''");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ADDRS ADD COLUMN DOP_INFO TEXT DEFAULT ''");
//            } catch (SQLiteException e) {
//            }

//            try {
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS sgi_FULL_IDX ON sgi(rowid, ID);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS GRUPS_FULL_IDX ON GRUPS(rowid, ID, SGIID);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_FULL_IDX ON Nomen(ID, GRUPID, COD, DESCR, lowDESCR, ost, ZAKAZ, SGIID, ISUPDATED);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS CONTR_FULL_IDX ON CONTRS(ROWID, ID,  DESCR, lowDESCR);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS CONTRS_IDX ON CONTRS(ID);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS TP_IDX ON TORG_PRED(ID);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ADDRS_FULL_IDX ON ADDRS(rowid, ID, PARENTEXT);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS DEBET_FULL_IDX ON DEBET(ROWID, ROW, CONTR_ID, TP_ID, TP_IDS);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ORDERS_FULL_IDX ON ORDERS(TP_ID, CONTR_ID, ADDR_ID, DATA);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_FULL_IDX ON ZAKAZY(ROWID, DOCNO, TP_ID, CONTR_ID, ADDR_ID, DOC_DATE, STATUS);");
//                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS ZAKAZY_DT_FULL_IDX ON ZAKAZY_DT(ROWID, ZAKAZ_ID, NOM_ID);");
//            } catch (SQLiteException e) {
//            }

//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ORDERS ADD COLUMN DELIV_TIME TEXT DEFAULT ''");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ORDERS ADD COLUMN GETMONEY NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ORDERS ADD COLUMN GETBACKWARD NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ORDERS ADD COLUMN BACKTYPE NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ZAKAZY ADD COLUMN DELIV_TIME TEXT DEFAULT ''");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ZAKAZY ADD COLUMN GETMONEY NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ZAKAZY ADD COLUMN GETBACKWARD NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ZAKAZY ADD COLUMN BACKTYPE NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

//            Изменения от 16-06-2016
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE CONTRS ADD COLUMN TP TEXT DEFAULT ''");
//            } catch (SQLiteException e) {
//            }

//            Изменения от 17-06-2016
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TORG_PRED ADD COLUMN TP_PASS TEXT DEFAULT ''");
//            } catch (SQLiteException e) {
//            }

            //Изменения от 28-07-2016
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE ZAKAZY_DT ADD COLUMN IS_OUTED INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

//            try {
////                UpdateSchemaDB.execSQL("ALTER TABLE ZAKAZY_DT ADD COLUMN OUT_QTY INTEGER DEFAULT 0");
////            } catch (SQLiteException e) {
////            }

//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN IS_PERM INTEGER DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

//            Обновление структуры таблицы дебиторки. Добавляем поля A35, A42, A49, A56, A63, A64
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN A35 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN A42 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN A49 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN A56 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN A63 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN A64 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN OTG30 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN OPL30 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }


            //            Обновление структуры временной таблицы дебиторки. Добавляем поля A35, A42, A49, A56, A63, A64

//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN A35 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN A42 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN A49 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN A56 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN A63 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN A64 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN OTG30 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN OPL30 NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

//            Обновление таблицы номенклатуры. Добавляем поле цены со скидкой SALE_PRICE
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN SALE_PRICE NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

            //            Обновление таблицы номенклатуры. Добавляем поле цены со скидкой SALE_PRICE
//            try {
//                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS SALES (CONTR_ID TEXT, GRUP_ID TEXT, SALE NUMERIC DEFAULT 0);");
//            } catch (SQLiteException e) {
//            }

            //            Обновление таблицы номенклатуры. Добавляем поля P1d, P2d - признак того что фото1 и фото2 загружены на планшет
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN P1D NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }
//
//            try {
//                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN P2D NUMERIC DEFAULT 0");
//            } catch (SQLiteException e) {
//            }

            //Создаем таблицу с привязкой номенклатуры к торговому представителю
//            try {
//                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TP_GRUP_ACCESS (TP_ID TEXT, SGI_ID TEXT, GRUP_ID TEXT);");
//            } catch (SQLiteException e) {
//            }

            //Изменения от 28-06-2019

            try {
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS sgi (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, DESCR TEXT)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS GRUPS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, SGIID TEXT NOT NULL, DESCR TEXT NOT NULL)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS Nomen (ID TEXT UNIQUE, GRUPID TEXT, COD TEXT, DESCR NUMERIC, OST INTEGER, PRICE NUMERIC, ZAKAZ INT DEFAULT 0, lowDESCR TEXT, SGIID TEXT, CODE TEXT, PHOTO1 TEXT, PHOTO2 TEXT, VKOROB INTEGER DEFAULT 0, ISUPDATED INTEGER DEFAULT 0, ISNEW INTEGER DEFAULT 0, IS7DAY INTEGER DEFAULT 0, IS28DAY INTEGER DEFAULT 0, MP TEXT DEFAULT '', IS_PERM INTEGER DEFAULT 0, SALE_PRICE NUMERIC DEFAULT 0, P1D NUMERIC DEFAULT 0, P2D NUMERIC DEFAULT 0)");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS CONTRS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ID TEXT, DESCR TEXT, lowDESCR TEXT, CODE TEXT, INSTOP INTEGER, DOLG INTEGER, DYNAMO INTEGER, TP TEXT DEFAULT '', INFO TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS ADDRS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,ID TEXT, PARENTEXT TEXT, DESCR TEXT,CODE TEXT, DOP_INFO TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TORG_PRED (ID TEXT NOT NULL, DESCR TEXT NOT NULL, CODE TEXT, TP_PASS TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
                UpdateSchemaDB.execSQL("CREATE TABLE IF NOT EXISTS TMP_DEBET (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW INTEGER, CONTR_ID TEXT, KREDIT INTEGER, LIM INTEGER, NEKONTR INTEGER, SALDO NUMERIC, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, TP_ID TEXT DEFAULT 0, TP_IDS TEXT DEFAULT 0, A35 NUMERIC DEFAULT 0, A42 NUMERIC DEFAULT 0, A49 NUMERIC DEFAULT 0, A56 NUMERIC DEFAULT 0, A63 NUMERIC DEFAULT 0, A64 NUMERIC DEFAULT 0, OTG30 NUMERIC DEFAULT 0, OPL30 NUMERIC DEFAULT 0, FIRMA TEXT DEFAULT '')");
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
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS DEBET_FULL_IDX ON DEBET(ROWID, ROW, CONTR_ID, TP_ID, TP_IDS);");
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

            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE CONTRS ADD COLUMN INFO TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            //Создаем таблицу с привязкой номенклатуры к торговому представителю

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE DEBET ADD COLUMN FIRMA TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN FIRMA TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN P1D NUMERIC DEFAULT 0");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE TMP_DEBET ADD COLUMN P2D NUMERIC DEFAULT 0");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE CONTRS ADD COLUMN CRT_DATE TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN TOVCATID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN FUNCID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN BRANDID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN WCID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN PRODID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN FOCUSID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }
//Обновление от 21-04-2020
            try {
                UpdateSchemaDB.execSQL("ALTER TABLE BRAND ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE FOCUS ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE FOCUS ADD COLUMN LOWLONG_DESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE FUNC ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE GRUPS ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE PROD ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE TOVCAT ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE WC ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE sgi ADD COLUMN LOWDESCR TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

//Конец Обновление от 21-04-2020
            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN FOCUSID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN MODELID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN SIZEID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("ALTER TABLE Nomen ADD COLUMN COLORID TEXT DEFAULT ''");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_MX_FULL_IDX ON Nomen(ID, GRUPID, SGIID, TOVCATID, FUNCID, BRANDID, WCID, PRODID);");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_MX_FOCUS_FULL_IDX ON Nomen(ID, GRUPID, SGIID, TOVCATID, FUNCID, BRANDID, WCID, PRODID, FOCUSID);");
            } catch (SQLiteException e) {
            }

            try {
                UpdateSchemaDB.execSQL("CREATE INDEX IF NOT EXISTS Nomen_MX_MDL_SZ_CLR_FULL_IDX ON Nomen(ID, GRUPID, SGIID, TOVCATID, FUNCID, BRANDID, WCID, PRODID, FOCUSID, MODELID, SIZEID, COLORID);");
            } catch (SQLiteException e) {
            }


//            P1D NUMERIC DEFAULT 0, P2D NUMERIC DEFAULT 0
            //
        }


    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.amber.armtp/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
    }
}

