package com.chih.suexclusive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class AppReceiver extends BroadcastReceiver {

    public AppReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent beginIntent = new Intent(context, SUManager.class);
            beginIntent.setAction(SUManager.ACTION_UPDATE);
            context.startService(beginIntent);
            Log.i("SUEX", "Manager Init Request");
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
