package com.dkaishu.hellovpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 *
 * Created by Administrator on 2017/12/7.
 */

public class HelloVpnService extends VpnService {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
