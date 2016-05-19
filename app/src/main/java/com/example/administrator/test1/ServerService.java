package com.example.administrator.test1;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-17.
 */
public class ServerService extends IntentService{
    private boolean serviceEnabled;

    private ResultReceiver serverResult;
    private File savefile;
    private int port;

    private String state;
    private int musicpos;

    public ServerService() {
        super("ServerService");
        Log.i("TAG","서버 서비스 생성");
        serviceEnabled = true;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");


        ServerSocket welcomeSocket = null;
        Socket socket = null;
        try {
            Log.i("TAG", "서버소켓생성");
            welcomeSocket = new ServerSocket(port);


            while(true && serviceEnabled)
            {
                Log.i("TAG","연결 대기");
                socket = welcomeSocket.accept();

                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                DataInputStream dis = new DataInputStream(is);

                state = dis.readUTF();
                musicpos = Integer.valueOf(dis.readUTF());
                Log.i("TAG", state + "상태수신완료");


                if(!state.equals("music state")) {
                File file = new File("/storage/emulated/0/Download/a.mp3");

                byte[] buffer = new byte[4096];
                int bytesRead;

                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);



                Log.i("TAG","파일 수신 시작");
                while(true)
                {
                    bytesRead = is.read(buffer, 0, buffer.length);
                    if(bytesRead == -1)
                    {
                        break;
                    }
                    bos.write(buffer, 0, bytesRead);
                    bos.flush();

                }
                Log.i("TAG","다 받음!");
                    serverResult.send(port, null);
                }else{
                    sendsignal(musicpos);
                }



                socket.close();
                //bos.close();
                dis.close();



            }
        } catch (IOException e) {
            Log.i("TAG",e.getMessage());
        }
        Log.i("TAG","이제꺼짐");


    }

    public void sendsignal(int pos){
        Bundle bundle = new Bundle();
        bundle.putInt("key",pos);
        serverResult.send(port,bundle);
    }


    public void onDestroy()
    {

        serviceEnabled = false;

        //Signal that the service was stopped
        //serverResult.send(port, new Bundle());

        stopSelf();
    }
}
