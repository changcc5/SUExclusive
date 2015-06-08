package com.chih.suexclusive;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

//need to handle intent to change configurations
//OnInitialize Intent: when phone starts, gather every packet and build config file and call update and create array list of config objects
//OnConfigChange intent: change local config file
//OnUpdate intent: copy local config to SU config

public class SUManager extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_UPDATE = "com.chih.suexclusive.action.update";
    public static final String ACTION_INIT = "com.chih.suexclusive.action.init";
    public static final String ACTION_CONFIG = "com.chih.suexclusive.action.config";

    public SUManager() {
        super("SUManager");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent != null && intent.getAction().equals(ACTION_UPDATE)) {

            } else if (intent.getAction().equals(ACTION_INIT)) {
                Log.d("SUEX", "Manager: Received Init");
                boolean shAvailable = Shell.SU.available();
                try {
                    FileOutputStream configFile = openFileOutput("su", MODE_WORLD_WRITEABLE);
                    BufferedWriter configWrite = new BufferedWriter(new FileWriter("su", true));
                    Default defaults = new Default();
                    for (String line : defaults.lines) {
                        Log.d("SUEX", "Written: " + line);
                        configFile.write(line.getBytes());
                    }
                    configFile.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
/*                File configFile = new File(this.getFilesDir(), "su.txt");
//                File configFile = new File("/data/data/com.chih.suexclusive/files/su.txt");

                //If this is the first time SUEx is run, create the configuration file
                try {
                    if (configFile.createNewFile()) {

List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
ArrayList<PackagePermission> pkgs = new ArrayList<PackagePermission>();

for (ApplicationInfo pkg : packages) {
try {
PackagePermission entry = new PackagePermission(pkg.processName, new Integer(getPackageManager().getApplicationInfo(pkg.packageName, 0).uid), PermissionLevel.DENY);
pkgs.add(entry);
} catch (PackageManager.NameNotFoundException e) {
e.printStackTrace();
}
}

                        BufferedWriter fwrite = new BufferedWriter(new FileWriter(configFile));
                        fwrite.write("Hello");
                        fwrite.write("There");
                        fwrite.flush();
                        fwrite.close();
                        Log.d("SUEX", "Config: " + configFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(shAvailable) {
                    Log.d("Shell", Shell.SU.version(true));
                    Shell.SU.run("mkdir -p /data/data/com.chih.suexclusive/files");
                    Shell.SU.run("yes | cp /data/data/eu.chainfire.supersu/files/supersu.cfg /data/data/com.chih.suexclusive/files/supersu.txt");
                }
        */
            } else if (intent.getAction().equals(ACTION_CONFIG)) {

            }
        }
    }

}
