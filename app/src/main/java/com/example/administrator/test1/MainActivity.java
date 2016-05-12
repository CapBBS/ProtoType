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
import java.net.ServerSocket;
import java.net.Socket;
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
                            sendIPaddress();
                            Toast.makeText(getApplicationContext(), Constants.CLIENT_ADRRESS, Toast.LENGTH_LONG).show();
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

    public void sendIPaddress(){
        if(wifiInfo.isGroupOwner){
            try {
                serverSocket = new ServerSocket(port);
                ssocket = serverSocket.accept();

                IPreceiver rc = new IPreceiver(ssocket);
                rc.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {

            try {
                // 서버 연결
                csocket = new Socket(Constants.HOST_ADRRESS, port);
                IPsender fs = new IPsender(csocket);
                fs.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
