package com.chih.suexclusive;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;


public class MainActivity extends Activity {
    public static PackagePermission interestedPK = new PackagePermission();
    public static boolean locked = false;
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
                Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
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

    private void FileEU () {
        try {
            File file = new File(this.getFilesDir(), "euFiles");
            if (!file.exists()) {
                int mode = this.MODE_PRIVATE;
                FileOutputStream outputStream;
                outputStream = openFileOutput("euFiles", mode);
                outputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void FileEU(PackagePermission pkg) {

        try {
            int mode = this.MODE_APPEND;
            FileOutputStream outputStream;
            outputStream = openFileOutput("euFiles", mode);
            outputStream.write(pkg.name.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void euRemove(String name) {
        try {
            File inputFile = new File(this.getFilesDir(),"euFiles");
            File tempFile = new File(this.getFilesDir(), "euTemp");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String currentLine;

            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if(trimmedLine.equals(name)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
            boolean successful = tempFile.renameTo(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] GetEU () {
        String[] list;
        ArrayList<String> lines = new ArrayList<String>();
        File appFile = new File(this.getFilesDir(), "euFiles");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appFile)));
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            reader.close();
            list = new String[lines.size()];
            list = lines.toArray(list);
            return list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateConfig(ArrayList<PackagePermission> list, boolean temp) {
        try {
            File config = new File(MainActivity.this.getFilesDir(), "su.cfg");
            if (temp) {
                config = new File(MainActivity.this.getFilesDir(), "sutemp.cfg");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(config));
            Default defaults = new Default();
            for (String s : defaults.lines) {
                writer.write(s);
                writer.newLine();
            }
            for (PackagePermission p : list) {
                if (!p.name.contains("default")) {
                    writer.write(p.name);
                    writer.newLine();
                    writer.write(p.uid);
                    writer.newLine();
                    writer.write(p.permission);
                    writer.newLine();
                    writer.newLine();
                }
            }

            writer.close();

            boolean shAvailable = Shell.SU.available();
            if (shAvailable) {
                Log.d("SUEX", Shell.SU.version(true));
                if (temp) {
                    Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/sutemp.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");
                }
                else {
                    Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");
                }
                Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                Log.d("SUEX", "Copy SU Complete");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConfig(ArrayList<PackagePermission> list, PackagePermission pkg, String permission) {
        //check pkglist and update files

        for (PackagePermission p : list) {
            if (p.name.equals(pkg.name)) {
                if (permission.equals("Deny")) {
                    p.permission = "access=0";
                    euRemove(pkg.name);
                }
                else if (permission.equals("Grant")) {
                    p.permission = "access=1";
                    euRemove(pkg.name);
                }
                else if (permission.equals("Exclusive")) {
                    p.permission = "access=1";
                    FileEU(p);
                }
            }
        }

        try {
            File config = new File(MainActivity.this.getFilesDir(), "su.cfg");
            BufferedWriter writer = new BufferedWriter(new FileWriter(config));
            Default defaults = new Default();
            for (String s : defaults.lines) {
                writer.write(s);
                writer.newLine();
            }
            for (PackagePermission p : list) {
                if (!p.name.contains("default")) {
                    writer.write(p.name);
                    writer.newLine();
                    writer.write(p.uid);
                    writer.newLine();
                    writer.write(p.permission);
                    writer.newLine();
                    writer.newLine();
                }
            }

            writer.close();

            boolean shAvailable = Shell.SU.available();
            if (shAvailable) {
                Log.d("SUEX", Shell.SU.version(true));
                Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");
                Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                Log.d("SUEX", "Copy SU Complete");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            FileEU();
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

                                try {
                                    File file = new File(ctx.getFilesDir(), "su.cfg");
                                    int mode = ctx.MODE_PRIVATE;
                                    if (file.exists()) {
                                        mode = ctx.MODE_APPEND;
                                    }
                                    BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
                                    writer.write(toWrite.get(0));
                                    writer.newLine();
                                    writer.write(toWrite.get(1));
                                    writer.newLine();
                                    writer.write(toWrite.get(2));
                                    writer.close();
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                                boolean shAvailable = Shell.SU.available();
                                if (shAvailable) {
                                    Log.d("SUEX", Shell.SU.version(true));
                                    Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");

                                    Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                                }
                                Log.d("SUEX", "Init: added app " + pkg);
                            }
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

                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName("eu.chainfire.supersu","eu.chainfire.supersu.MainActivity");
                p.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }

            CpyCfgToSU copier = new CpyCfgToSU(ctx);
            copier.copySU();

            return null;
        }
    }

    private class ConfigReader {
        public ArrayList<PackagePermission> pkgList;
        public ArrayList<PackagePermission> pkgListUnsort;

        private Context context;
        public ConfigReader(Context context) {
            this.context = context;
            pkgList = new ArrayList<PackagePermission>();
            pkgListUnsort = new ArrayList<PackagePermission>();
        }

        public void readConfig() {
            try {
                FileInputStream is = new FileInputStream(new File(context.getFilesDir(), "su.cfg"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                PackagePermission pkg = new PackagePermission();

                while(reader.ready()) {
                    String line = reader.readLine();
                    Log.d("SUEX", "InitUI: " + line);
                    if (line.startsWith("[")) {
                        pkg.name = line;
                    }
                    else if (line.startsWith("uid")) {
                        pkg.uid = line;
                    }
                    else if (line.startsWith("access")) {
                        pkg.permission = line;
                    }
                    else if (line.isEmpty()) {
                        pkgList.add(pkg);
                        pkgListUnsort.add(pkg);
                        pkg = new PackagePermission();
                    }
                }
                reader.close();
                Collections.sort(pkgList, new Comparator<PackagePermission>() {
                    @Override
                    public int compare(PackagePermission  pkg1, PackagePermission  pkg2)
                    {

                        return  pkg1.name.compareTo(pkg2.name);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        (new InitEX()).setContext(this).execute();

        final ConfigReader configReader = new ConfigReader(this);
        configReader.readConfig();
        ListView mainList = (ListView) findViewById(R.id.mainList);
        final CustomAdapter mainAdapter = new CustomAdapter(this, configReader.pkgList);
        mainList.setAdapter(mainAdapter);
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!MainActivity.locked) {
                    Log.d("SUEX", "List Item Clicked: " + configReader.pkgList.get(position).name);
                    MainActivity.interestedPK = configReader.pkgList.get(position);

                    final String[] perms = {"Deny", "Grant", "Exclusive"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(configReader.pkgList.get(position).name);
                    builder.setItems(perms, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            //UPDATE config
                            updateConfig(configReader.pkgListUnsort, MainActivity.interestedPK, perms[item]);
                            mainAdapter.notifyDataSetChanged();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        Thread checkEu = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        //TODO checks if new apps installed, add default config if so
                        Context ctx = MainActivity.this;
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

                                        try {
                                            File file = new File(ctx.getFilesDir(), "su.cfg");
                                            BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
                                            writer.write(toWrite.get(0));
                                            writer.newLine();
                                            writer.write(toWrite.get(1));
                                            writer.newLine();
                                            writer.write(toWrite.get(2));
                                            writer.close();
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        boolean shAvailable = Shell.SU.available();
                                        if (shAvailable) {
                                            Log.d("SUEX", Shell.SU.version(true));
                                            Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");

                                            Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                                        }
                                        Log.d("SUEX", "Init: added app " + pkg);
                                    }
                                }

                                for (String p : currApps) {
                                    if (!instApps.contains(p)) {
                                        try {
                                            File inputFile = new File(ctx.getFilesDir(),"appList");
                                            File tempFile = new File(ctx.getFilesDir(), "appListTemp");

                                            BufferedReader appreader = new BufferedReader(new FileReader(inputFile));
                                            BufferedWriter appwriter = new BufferedWriter(new FileWriter(tempFile));
                                            String currentLine;

                                            while((currentLine = appreader.readLine()) != null) {
                                                // trim newline when comparing with lineToRemove
                                                String trimmedLine = currentLine.trim();
                                                if(trimmedLine.equals(p)) continue;
                                                appwriter.write(currentLine + System.getProperty("line.separator"));
                                            }
                                            appwriter.close();
                                            appreader.close();
                                            boolean successful = tempFile.renameTo(inputFile);

                                            inputFile = new File(ctx.getFilesDir(), "su.cfg");
                                            tempFile = new File(ctx.getFilesDir(), "suTemp.cfg");
                                            appreader = new BufferedReader(new FileReader(inputFile));
                                            appwriter = new BufferedWriter(new FileWriter(tempFile));
                                            currentLine = "";
                                            int i = 0;
                                            boolean skip = false;
                                            while((currentLine = appreader.readLine()) != null) {
                                                // trim newline when comparing with lineToRemove
                                                String trimmedLine = currentLine.trim();
                                                if(trimmedLine.equals("[" + p + "]") || skip == true) {
                                                    if (i == 3) {
                                                        skip = false;
                                                    }
                                                    i++;
                                                    continue;
                                                }
                                                appwriter.write(currentLine + System.getProperty("line.separator"));
                                            }
                                            appwriter.close();
                                            appreader.close();
                                            boolean succ = tempFile.renameTo(inputFile);
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        boolean shAvailable = Shell.SU.available();
                                        if (shAvailable) {
                                            Log.d("SUEX", Shell.SU.version(true));
                                            Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");

                                            Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                                            Log.d("SUEX", "Copy SU Complete");
                                        }
                                    }
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

                        ActivityManager activityManager = (ActivityManager) MainActivity.this.getSystemService(ACTIVITY_SERVICE);
                        List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
                        String[] appsEU = GetEU();
                        MainActivity.locked = false;
                        for(int i = 0; i < procInfos.size(); i++)
                        {
                            if(Arrays.asList(appsEU).contains("[" + procInfos.get(i).processName + "]"))
                            {
                                Log.d("SUEX", "Exclusive App is running");
                                MainActivity.locked = true;
                                //create lock file if lock file not exist
                                try {
                                    File lockFile = new File(MainActivity.this.getFilesDir(),"lockFile");
                                    if (!lockFile.exists()) {
                                        BufferedWriter writer = new BufferedWriter(new FileWriter(lockFile));
                                        writer.write(procInfos.get(i).processName);
                                        writer.close();
                                    }
                                    ConfigReader cfg = new ConfigReader(MainActivity.this);
                                    cfg.readConfig();
                                    for (PackagePermission p : cfg.pkgListUnsort) {
                                        if (!p.name.contains(procInfos.get(i).processName) && !p.name.contains("suexclusive") && !p.name.contains("browser")) {
                                            p.permission = "access=0";
                                        }
                                    }
                                    updateConfig(cfg.pkgListUnsort, true);
                                    //TODO call update lock perm: copy current config, create config with everything deny except application
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (MainActivity.locked == false) {
                            //TODO call remove lock: rename original config to current config and remove lock file
                            File lockFile = new File(MainActivity.this.getFilesDir(),"lockFile");
                            if (lockFile.exists()) {
                                lockFile.delete();
                            }
                            File sutemp = new File(MainActivity.this.getFilesDir(), "sutemp.cfg");
                            if (sutemp.exists()) {
                                sutemp.delete();
                            }
                            boolean shAvailable = Shell.SU.available();
                            if (shAvailable) {
                                Log.d("SUEX", Shell.SU.version(true));
                                Shell.SU.run("yes | cp -f /data/data/com.chih.suexclusive/files/su.cfg /data/data/eu.chainfire.supersu/files/supersu.cfg");

                                Shell.SU.run("chmod 600 /data/data/eu.chainfire.supersu/files/supersu.cfg");
                                Log.d("SUEX", "Copy SU Complete");
                            }
                        }
                        sleep(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        checkEu.start();
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
