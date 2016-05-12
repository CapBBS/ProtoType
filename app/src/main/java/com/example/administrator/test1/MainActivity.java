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
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-05-12.
 */
public class MainActivity extends Activity{

    public Button btnFindpeer;

    Button[] buttons;

    private boolean connectedAndReadyToSendFile;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter wifiP2pIntentFilter;
    private WifiBroadcastReceiver wifiBroadcastReceiver;
    protected WifiP2pDeviceList wifiP2pDeviceList;

    private Intent clientServiceIntent;
    //private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;

    private int port = 1111;
    private ServerSocket serverSocket;
    private Socket ssocket,csocket;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFindpeer = (Button)findViewById(R.id.btnFindpeer);

        buttons = new Button[3];
        buttons[0] = (Button)findViewById(R.id.btnDevice1);
        buttons[1] = (Button)findViewById(R.id.btnDevice2);
        buttons[2] = (Button)findViewById(R.id.btnDevice3);

        for(Button button : buttons) {
            button.setVisibility(Button.INVISIBLE);
            button.setEnabled(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                        if(device.deviceName.equals(((Button)v).getText())) {
                            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                            wifiP2pConfig.deviceAddress = device.deviceAddress;
                            wifiP2pConfig.groupOwnerIntent = 0;
                            manager.connect(channel, wifiP2pConfig, null);
                            //Toast.makeText(getApplicationContext(), Constants.CLIENT_ADRRESS, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }


        wifiP2pIntentFilter = new IntentFilter();
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiP2pIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        connectedAndReadyToSendFile = false;

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        wifiBroadcastReceiver = new WifiBroadcastReceiver(manager, channel, this);

        registerReceiver(wifiBroadcastReceiver, wifiP2pIntentFilter);


        btnFindpeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel,null);
            }
        });



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


    String ip;

    public void sendIPaddress(WifiP2pInfo info){
        if(info.isGroupOwner){
            try {
                serverSocket = new ServerSocket(port);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ssocket=serverSocket.accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                }.start();
                IPreceiver rc = new IPreceiver(ssocket);
                rc.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {

                // 서버 연결
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            csocket = new Socket(InetAddress.getByName(Constants.HOST_ADRRESS), port);
                            ip = csocket.getLocalAddress().getHostAddress();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }.start();

                IPsender fs = new IPsender(ip);
                fs.start();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(wifiBroadcastReceiver);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiBroadcastReceiver);
    }

    protected void displayPeerButtons(WifiP2pDeviceList peers) {
        ArrayList<String> deviceNameList = new ArrayList<String>();
        for(WifiP2pDevice device : peers.getDeviceList()) {
            deviceNameList.add(device.deviceName);
        }
        int iteration = deviceNameList.size();
        if(deviceNameList.size() <= 3) {

            for(int i = 0; i < 3; i++) {
                if(i < deviceNameList.size()) {
                    buttons[i].setEnabled(true);
                    buttons[i].setVisibility(Button.VISIBLE);
                    buttons[i].setText(deviceNameList.get(i));
                } else {
                    buttons[iteration].setEnabled(false);
                    buttons[iteration].setVisibility(Button.INVISIBLE);
                }
            }
        } else {
        }

    }

}
