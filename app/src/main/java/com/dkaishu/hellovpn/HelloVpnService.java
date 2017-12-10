package com.dkaishu.hellovpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/12/7.
 */

public class HelloVpnService extends VpnService implements Runnable {

    public static HelloVpnService Instance;
    public static String ProxyUrl;
    public static boolean IsRunning = false;

    private static int ID;
    private static int LOCAL_IP;
    private static ConcurrentHashMap<onStatusChangedListener, Object> mOnStatusChangedListeners = new ConcurrentHashMap<onStatusChangedListener, Object>();
    private Handler mHandler;


    public HelloVpnService() {
        ID++;
        mHandler = new Handler();
        Instance = this;

        System.out.printf("New VPNService(%d)\n", ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IsRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {

    }
    public void writeLog(final String format, Object... args) {
        final String logString = String.format(format, args);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : mOnStatusChangedListeners.entrySet()) {
                    entry.getKey().onLogReceived(logString);
                }
            }
        });
    }

    public interface onStatusChangedListener {
        public void onStatusChanged(String status, Boolean isRunning);

        public void onLogReceived(String logString);
    }

    public static void addOnStatusChangedListener(onStatusChangedListener listener) {
        if (!mOnStatusChangedListeners.containsKey(listener)) {
            mOnStatusChangedListeners.put(listener, 1);
        }
    }

    public static void removeOnStatusChangedListener(onStatusChangedListener listener) {
        if (mOnStatusChangedListeners.containsKey(listener)) {
            mOnStatusChangedListeners.remove(listener);
        }
    }

    public static void removeAllOnStatusChangedListener() {
        mOnStatusChangedListeners.clear();
    }

    private void onStatusChanged(final String status, final boolean isRunning) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : mOnStatusChangedListeners.entrySet()) {
                    entry.getKey().onStatusChanged(status, isRunning);
                }
            }
        });
    }
}
