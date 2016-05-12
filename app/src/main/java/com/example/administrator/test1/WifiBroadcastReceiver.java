package com.example.administrator.test1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;


/**
 * Created by Administrator on 2016-05-03.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver{

        private WifiP2pManager wifiP2pManager;
        private Channel channel;
        private MainActivity Mactivity;

        public WifiBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
            super();
            this.channel = channel;
            this.wifiP2pManager = manager;
            this.Mactivity = Mactivity;
        }

        @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    Mactivity.btnConnect.setEnabled(true);
                }
            });
        }
    }
}

