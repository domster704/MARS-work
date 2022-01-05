package com.amber.armtp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.zip.ZipDownload;
import com.amber.armtp.zip.ZipUnpacking;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Updated by domster704 on 27.09.2021
 */
public class MainActivity extends AppCompatActivity {
    public static String filesPath;

    //Defining Variables
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
    TextView tvLastUpdate;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getFilesDir().getPath();
        filesPath = path.substring(0, path.lastIndexOf("/")) + "/databases/";
        Log.d("ftp", filesPath);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // For ftp-server
        String host = getResources().getString(R.string.host);
        String dir = getResources().getString(R.string.fileDirectory);
        String fileName = getResources().getString(R.string.fileName);
        int port = getResources().getInteger(R.integer.port);

        String user = getResources().getString(R.string.user);
        String password = getResources().getString(R.string.password);

        // It's singleton instance for future using
        ServerDetails.getInstance(host, dir, port, filesPath + fileName, user, password);

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

        if (globalVariable.dbOrders == null) {
            globalVariable.dbOrders = new DBOrdersHelper(getApplicationContext());
        }

        if (globalVariable.dbApp == null) {
            globalVariable.dbApp = new DBAppHelper(getApplicationContext());
        }

        File old_db = new File(globalVariable.GetSDCardpath() + "ARMTP_DB" + "/armtp3.db");
        if (old_db.exists()) {
            try {
                FileUtils.copyFile(new File(old_db.toString()), new File(globalVariable.db.getWritableDatabase().getPath()));
                File toName = new File(globalVariable.GetSDCardpath() + "ARMTP_DB/armtp3.db_");
                old_db.renameTo(toName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // armtp3.db
        try {
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS SGI (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, CODE TEXT NOT NULL, DESCR TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS GRUPS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, CODE TEXT NOT NULL, SGIID TEXT NOT NULL, DESCR TEXT NOT NULL)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS NOMEN (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, SGI TEXT, GRUPPA TEXT, KOD5 TEXT, DESCR TEXT, DEMP TEXT, FOCUSID TEXT, GOFRA INTEGER, FOTO TEXT, POSTDATA DATE, OST INTEGER, PD NUMERIC DEFAULT 0, ZAKAZ NUMERIC DEFAULT 0, PRICE NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS PRICES (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, NOMENID TEXT, TIPCE TEXT, PRICE NUMERIC)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS CONTRS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, CODE TEXT NOT NULL, DESCR TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ADDRS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, KONTRCODE TEXT, CODE TEXT, DESCR TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS TORG_PRED (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, CODE TEXT, DESCR TEXT NOT NULL)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS DEBET (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, KONTR TEXT, CONTR_ID TEXT, SCHET TEXT, STATUS TEXT, KREDIT TEXT, DOGOVOR DATE, A7 NUMERIC, A14 NUMERIC, A21 NUMERIC, A28 NUMERIC, A35 NUMERIC, A42 NUMERIC, A49 NUMERIC, A56 NUMERIC, A63 NUMERIC, A64 NUMERIC, DOLG NUMERIC, OTGR30 NUMERIC, OPL30 NUMERIC, K_OBOR NUMERIC, FIRMA TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS STATUS (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DOCID TEXT, STATUS TEXT)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS VYCHERK (rowid INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DOCID TEXT, NOM TEXT, COL NUMERIC)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS SKIDKI (CONTRID TEXT, SGI TEXT, GRUPID TEXT, TIPCE TEXT, SALE NUMERIC DEFAULT 0)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS FOKUS (CODE TEXT, DESCR TEXT, DATAN DATE, DATAK DATE)");
            globalVariable.db.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ORDERS (TP TEXT, CONTR TEXT, ADDR TEXT, DATA TEXT, COMMENT TEXT)");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        // order.db
        try {
            globalVariable.dbOrders.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ZAKAZY (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DOCID TEXT, TP TEXT, CONTR BLOB, ADDR TEXT, DOC_DATE REAL, DELIVERY_DATE TEXT, COMMENT TEXT, STATUS INTEGER DEFAULT 0, CONTR_DES TEXT, ADDR_DES TEXT)");
            globalVariable.dbOrders.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS ZAKAZY_DT (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ZAKAZ_ID TEXT, NOMEN TEXT, DESCR TEXT, QTY INTEGER, PRICE NUMERIC, IS_OUTED INTEGER DEFAULT 0, OUT_QTY INTEGER DEFAULT 0)");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        // appData.db
        try {
            globalVariable.dbApp.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS DEMP (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, DEMP TEXT)");
        } catch (SQLiteException e) {
            e.printStackTrace();
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

        // Initializing Toolbar and setting it as the actionbar
        initToolbar();
        initNavigationView();

        // Initializing NavigationView
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @RequiresApi(api = Build.VERSION_CODES.O)
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
//                        try {
//                            DownloadDB();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        return true;
                    case R.id.nav_journal:
                        DisplayFragment(new JournalFragment(), "frag_journal");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_order_header:
                        DisplayFragment(new OrderHeadFragment(), "frag_order_header");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
                    case R.id.nav_debet:
                        DisplayFragment(new DebetFragment(), "frag_debet");
                        setToolbarTitle(menuItem.getTitle());
                        return true;
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
                initLastUpdate();
            }
        };


        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        fragment = new JournalFragment();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
        fragmentTransaction.commit();
        setToolbarTitle("Журнал");

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

    private void initLastUpdate() {
        tvLastUpdate = findViewById(R.id.tvLastUpdateText);
        if (tvLastUpdate != null) {
            tvLastUpdate.setText(globalVariable.ReadLastUpdate());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private int countOfReturning = 0;

//    private void DownloadDB() throws IOException {
//        ZipDownload zipDownload;
//        try {
//            zipDownload = new ZipDownload(ServerDetails.getInstance());
//            zipDownload.downloadZip();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        ZipUnpacking zipUnpacking;
//        try {
//            zipUnpacking = new ZipUnpacking(ServerDetails.getInstance());
//            zipUnpacking.doUnpacking();
//            Toast.makeText(getApplication(), "Загрузка завершена", Toast.LENGTH_SHORT).show();
//
//            if (getFileSizeKiloBytes(new File(filesPath + "armtp3.db")) < 100 && countOfReturning < 3) {
//                countOfReturning++;
//                Toast.makeText(getApplication(), "Попытка: " + countOfReturning, Toast.LENGTH_SHORT).show();
//                DownloadDB();
//            } else if (countOfReturning >= 3) {
//                Toast.makeText(getApplication(), "Что-то пошло не так. Проверьте подключение к интернету и попробуйте снова", Toast.LENGTH_SHORT).show();
//            } else {
//                File f = new File(filesPath + "orders.db");
//                if (f.exists()) {
//                    f.delete();
//                }
//
//                globalVariable.dbApp.putDemp(globalVariable.db.getReadableDatabase());
//            }
//
//            restart();
//        } catch (Exception e) {
//            Toast.makeText(getApplication(), "Что-то пошло не так. Проверьте подключение к интернету и попробуйте снова", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//    }
//
//    public void restart() {
//        Intent intent = new Intent(this, MainActivity.class);
//        this.startActivity(intent);
//        System.exit(0);
//    }
//
//    private static double getFileSizeKiloBytes(File file) {
//        return (double) file.length() / 1024;
//    }
}
