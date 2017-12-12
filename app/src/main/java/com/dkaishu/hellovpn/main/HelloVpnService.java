package com.dkaishu.hellovpn.main;

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


    //122.14.193.152
    private String test_IP = "122.14.193.152";

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
        establish();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 1）MTU（Maximun Transmission Unit），即表示虚拟网络端口的最大传输单元，如果发送的包长度超过这个数字，则会被分包；
     * 2）Address，即这个虚拟网络端口的IP地址；
     * 3）Route，只有匹配上的IP包，才会被路由到虚拟端口上去。如果是0.0.0.0/0的话，则会将所有的IP包都路由到虚拟端口上去；
     * 4）DNS Server，就是该端口的DNS服务器地址；
     * 5）Search Domain，就是添加DNS域名的自动补齐。DNS服务器必须通过全域名进行搜索，但每次查找都输入全域名太麻烦了，可以通过配置域名的自动补齐规则予以简化；
     * 6）Session，就是你要建立的VPN连接的名字，它将会在系统管理的与VPN连接相关的通知栏和对话框中显示出来；
     * 7）Configure Intent，这个intent指向一个配置页面，用来配置VPN链接。它不是必须的，如果没设置的话，则系统弹出的VPN相关对话框中不会出现配置按钮。
     * 最后调用Builder.establish函数，如果一切正常的话，tun0虚拟网络接口就建立完成了。并且，同时还会通过iptables命令，修改NAT表，将所有数据转发到tun0接口上。
     */
    private void establish() {
        Builder builder = new Builder();
        builder.setMtu(20000);
        builder.addAddress(test_IP,32);
        builder.addRoute("0.0.0.0",0);
//        builder.addDnsServer();
//        builder.addSearchDomain(...);
        builder.setSession("testVPN");
//        builder.setConfigureIntent();

//        ParcelFileDescriptor interface =builder.establish();
    }

/*    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(ProxyConfig.Instance.getMTU());
        if (ProxyConfig.IS_DEBUG)
            System.out.printf("setMtu: %d\n", ProxyConfig.Instance.getMTU());

        ProxyConfig.IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        if (ProxyConfig.IS_DEBUG)
            System.out.printf("addAddress: %s/%d\n", ipAddress.Address, ipAddress.PrefixLength);

        for (ProxyConfig.IPAddress dns : ProxyConfig.Instance.getDnsList()) {
            builder.addDnsServer(dns.Address);
            if (ProxyConfig.IS_DEBUG)
                System.out.printf("addDnsServer: %s\n", dns.Address);
        }

        if (ProxyConfig.Instance.getRouteList().size() > 0) {
            for (ProxyConfig.IPAddress routeAddress : ProxyConfig.Instance.getRouteList()) {
                builder.addRoute(routeAddress.Address, routeAddress.PrefixLength);
                if (ProxyConfig.IS_DEBUG)
                    System.out.printf("addRoute: %s/%d\n", routeAddress.Address, routeAddress.PrefixLength);
            }
            builder.addRoute(CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16);

            if (ProxyConfig.IS_DEBUG)
                System.out.printf("addRoute for FAKE_NETWORK: %s/%d\n", CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16);
        } else {
            builder.addRoute("0.0.0.0", 0);
            if (ProxyConfig.IS_DEBUG)
                System.out.printf("addDefaultRoute: 0.0.0.0/0\n");
        }


        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<String>();
        for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
            String value = (String) method.invoke(null, name);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
                if (value.replaceAll("\\d", "").length() == 3) {//防止IPv6地址导致问题
                    builder.addRoute(value, 32);
                } else {
                    builder.addRoute(value, 128);
                }
                if (ProxyConfig.IS_DEBUG)
                    System.out.printf("%s=%s\n", name, value);
            }
        }

        if (AppProxyManager.isLollipopOrAbove) {
            if (AppProxyManager.Instance.proxyAppInfo.size() == 0) {
                writeLog("Proxy All Apps");
            }
            for (AppInfo app : AppProxyManager.Instance.proxyAppInfo) {
                builder.addAllowedApplication("com.vm.shadowsocks");//需要把自己加入代理，不然会无法进行网络连接
                try {
                    builder.addAllowedApplication(app.getPkgName());
                    writeLog("Proxy App: " + app.getAppLabel());
                } catch (Exception e) {
                    e.printStackTrace();
                    writeLog("Proxy App Fail: " + app.getAppLabel());
                }
            }
        } else {
            writeLog("No Pre-App proxy, due to low Android version.");
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setConfigureIntent(pendingIntent);

        builder.setSession(ProxyConfig.Instance.getSessionName());
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        onStatusChanged(ProxyConfig.Instance.getSessionName() + getString(R.string.vpn_connected_status), true);
        return pfdDescriptor;
    }*/


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
        void onStatusChanged(String status, Boolean isRunning);

        void onLogReceived(String logString);
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
