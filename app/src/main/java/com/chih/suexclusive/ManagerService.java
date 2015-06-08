package com.chih.suexclusive;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ManagerService extends Service {
    public ManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
