package com.example.administrator.test1;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2016-05-17.
 */
public class ClientService extends IntentService {


    private int port;
    private File fileToSend;

    public ClientService() {
        super("ClientService");
        Log.i("TAG","클라이언트 서비스 시작");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        fileToSend = (File) intent.getExtras().get("sendtofile");

        try {
            InetAddress targetIP = InetAddress.getByName("192.168.49.1");

            Socket clientSocket = null;
            OutputStream os = null;

            try {


                clientSocket =new Socket(targetIP, port);
                os = clientSocket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);


                InputStream is = clientSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                Log.i("TAG","파일 전송 시작");

                byte[] buffer = new byte[4096];

                FileInputStream fis = new FileInputStream(fileToSend);
                BufferedInputStream bis = new BufferedInputStream(fis);
                // long BytesToSend = fileToSend.length();
                while(true)
                {
                    int bytesRead = bis.read(buffer, 0, buffer.length);

                    if(bytesRead == -1)
                    {
                        break;
                    }

                    //BytesToSend = BytesToSend - bytesRead;
                    os.write(buffer,0, bytesRead);
                    os.flush();
                }



                fis.close();
                bis.close();

                br.close();
                isr.close();
                is.close();

                pw.close();
                os.close();


                clientSocket.close();

                Log.i("TAG","File Transfer Complete, sent file: " + fileToSend.getName());


            } catch (IOException e) {
                Log.i("TAG",e.getMessage());
            }
            catch(Exception e)
            {
                Log.i("TAG",e.getMessage());

            }
        }catch (UnknownHostException e) {

        }


    }
}
