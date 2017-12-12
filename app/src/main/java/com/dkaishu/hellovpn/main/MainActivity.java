package com.dkaishu.hellovpn.main;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;

import com.dkaishu.hellovpn.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private void startVPN() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            Intent intent = new Intent(this, HelloVpnService.class);
            startService(intent);
        }
    }
}
