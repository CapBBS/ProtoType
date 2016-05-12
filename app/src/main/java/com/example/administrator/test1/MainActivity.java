package com.example.administrator.test1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Administrator on 2016-05-12.
 */
public class MainActivity extends Activity{

    public Button btnFindpeer, btnConnect;

    private boolean connectedAndReadyToSendFile;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter wifiP2pIntentFilter;
    private WifiBroadcastReceiver wifiBroadcastReceiver;

    private Intent clientServiceIntent;
    //private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFindpeer = (Button)findViewById(R.id.btnFindpeer);

        wifiP2pIntentFilter = new IntentFilter();
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        connectedAndReadyToSendFile = false;

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        wifiBroadcastReceiver = new WifiBroadcastReceiver(manager, channel, this);

        btnFindpeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel,null);
            }
        });



    }




    public void connectpeer(final WifiP2pDeviceList wifiP2pDeviceList) {
        for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.groupOwnerIntent = 0;
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            manager.connect(channel, config, null);
        }
    }

    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device)
    {
        wifiInfo = info;
        //targetDevice = device;
        connectedAndReadyToSendFile = status;
    }

    public void setTransferStatus(boolean status)
    {
        connectedAndReadyToSendFile = status;
    }
}
