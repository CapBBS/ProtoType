package com.example.administrator.test1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016-05-03.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver{

        private WifiP2pManager wifiP2pManager;
        private Channel channel;
        private MainActivity activity;

        public WifiBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
            super();
            this.channel = channel;
            this.wifiP2pManager = manager;
            this.activity = activity;
        }

        @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    activity.wifiP2pDeviceList = peers;
                    ArrayList<String> deviceNameList = new ArrayList<String>();
                    for(WifiP2pDevice device : peers.getDeviceList()) {
                        deviceNameList.add(device.deviceName);
                    }
                    int iteration = 0;
                    if(deviceNameList.size() <= 3) {
                        iteration = deviceNameList.size();
                    } else {
                        iteration = 3;
                    }
                    for(int i = 0; i < iteration; i++) {
                        activity.buttons[i].setEnabled(true);
                        activity.buttons[i].setVisibility(Button.VISIBLE);
                        activity.buttons[i].setText(deviceNameList.get(i));
                    }
                }
            });
        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if(networkState.isConnected())
            {
                //set client state so that all needed fields to make a transfer are ready

                //activity.setTransferStatus(true);
                activity.setNetworkToReadyState(true, wifiInfo, device);
            }
            else
            {
                //set variables to disable file transfer and reset client back to original state

                activity.setTransferStatus(false);
                wifiP2pManager.cancelConnect(channel, null);

            }
            //activity.setClientStatus(networkState.isConnected());

            // Respond to new connection or disconnections
        }
    }
}

