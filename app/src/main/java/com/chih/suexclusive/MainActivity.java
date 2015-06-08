package com.chih.suexclusive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;


public class MainActivity extends Activity {
    private class AppLister {
        //maintain list of all installed apps
        public Context context;

        public AppLister(Context context) {
            this.context = context;
        }

        public ArrayList<String> listApps() {
            ArrayList<String> appsList = new ArrayList<String>();

            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> appList = pm.getInstalledApplications(0);

            for(ApplicationInfo app : appList) {
                appsList.add(app.packageName);
            }

            return appsList;
        }
    }

    private class CpyCfgToSU {
        private Context ctx;
        public CpyCfgToSU(Context ctx) {
            this.ctx = ctx;
        }

        public void copySU() {
            boolean shAvailable = Shell.SU.available();
            if(shAvailable) {
                Log.d("SUEX", Shell.SU.version(true));
                Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");
                Shell.SU.run("chmod 777 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                Log.d("SUEX", "Copy SU Complete");
            }
        }
    }

    private class FileIO extends AsyncTask<ArrayList<String>, Void, Void> {
        private Context ctx;

        public FileIO setContext(Context context) {
            this.ctx = context;
            return this;
        }

        @Override
        protected Void doInBackground(ArrayList<String>... params) {
            String filename = params[0].get(0);
            try {
                File file = new File(ctx.getFilesDir(), filename);
                int mode = ctx.MODE_PRIVATE;
                if (file.exists()) {
                    mode = ctx.MODE_APPEND;
                }
                FileOutputStream outputStream;
                outputStream = openFileOutput(filename, mode);
                for (int i = 1; i < params[0].size(); i++) {
                    outputStream.write(params[0].get(i).getBytes());
                    outputStream.write("\n".getBytes());
                }
                outputStream.close();
                Log.d("SUEX", file.getAbsolutePath());
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class InitEX extends AsyncTask<Void, Void, Void> {
        private Context ctx;

        public InitEX setContext(Context context) {
            this.ctx = context;
            return this;
        }

        @Override
        protected Void doInBackground(Void... params) {
            File configFile = new File(ctx.getFilesDir(), "su.cfg");
            if (configFile.exists()) {
                //check if new app installed, append apps
                AppLister lister = new AppLister(ctx);
                ArrayList<String> instApps = lister.listApps();
                ArrayList<String> currApps = new ArrayList<String>();

                File appFile = new File(ctx.getFilesDir(),"appList");
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appFile)));
                    while (reader.ready()) {
                        currApps.add(reader.readLine());
                    }
                    reader.close();

                    if (!currApps.containsAll(instApps)) {
                        for (String pkg : instApps) {
                            if (!currApps.contains(pkg)) {
                                ArrayList<String> toWrite = new ArrayList<String>();
                                toWrite.add(pkg);
                                int uid = ctx.getPackageManager().getApplicationInfo(pkg, 0).uid % 10000;
                                toWrite.add(Integer.toString(uid));
                                toWrite.add("access=0\n");
                                (new FileIO()).setContext(ctx).execute(toWrite);
                            }
                            Log.d("SUEX", "Init: added app " + pkg);
                        }
                    }
                    else {
                        Log.d("SUEX", "Init: all apps included");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }


            }
            else {
                Log.d("SUEX", "First Time Initialization");

                ArrayList<String> createCfg = new ArrayList<String>();
                Default defaults = new Default();
                createCfg.add("su.cfg");
                createCfg.addAll(defaults.lines);

                AppLister lister = new AppLister(ctx);
                ArrayList<String> apps = lister.listApps();

                for (String app : apps) {
                    try {
                        createCfg.add("[" + app + "]");
                        int uid = ctx.getPackageManager().getApplicationInfo(app, 0).uid % 10000;
                        createCfg.add("uid=" + Integer.toString(uid));
                        createCfg.add("access=1\n");
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                }

                (new FileIO()).setContext(ctx).execute(createCfg);

                ArrayList<String> appListFileInput = new ArrayList<String>();
                appListFileInput.add("appList");
                appListFileInput.addAll(apps);
                (new FileIO()).setContext(ctx).execute(appListFileInput);
            }

            CpyCfgToSU copier = new CpyCfgToSU(ctx);
            copier.copySU();

            return null;
        }
    }

    public class ConfigReader {
        //check file and update ui
        public ArrayList<String[]> readConfig() {
            ArrayList<String[]> config = new ArrayList<String[]>();

            File appFile = new File(getFilesDir(),"su.cfg");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appFile)));
                ArrayList<String> pack = new ArrayList<String>();
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (!line.equals("")) {
                        pack.add(line);
                        Log.d("SUEX", "config read: " + pack.toString());
                    }
                    else {
                        String[] pkgEntry = new String[pack.size()];
                        pkgEntry = pack.toArray(pkgEntry);
                        config.add(pkgEntry);
                        pack = new ArrayList<String>();
                        Log.d("SUEX", "Create Packet Entry: " + pack.toString());
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return config;
        }
    }
    public static ArrayList<String[]> pkgList = new ArrayList<String[]>();

    private void updateConfig() {
        //check pkglist and update files
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppLister lister = new AppLister(this);
        ArrayList<String> appList;
        appList = lister.listApps();
        String[] listarray = new String[appList.size()];
        listarray = appList.toArray(listarray);
        ListAdapter adapter = new RowAdapter(this, listarray);
        ListView mainList = (ListView) findViewById(R.id.mainList);
        mainList.setAdapter(adapter);

        ConfigReader configReader = new ConfigReader();
        MainActivity.pkgList = configReader.readConfig();

        (new InitEX()).setContext(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
