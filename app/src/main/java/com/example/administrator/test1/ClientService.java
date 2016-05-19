package com.example.administrator.test1;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
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

    private Boolean isfile;
    private String state;
    private int musicpos;

    public ClientService() {
        super("ClientService");
        Log.i("TAG","클라이언트 서비스 시작");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        fileToSend = (File) intent.getExtras().get("sendtofile");
        isfile = (Boolean) intent.getExtras().get("sendstate");
        state = (String) intent.getExtras().get("state");
        musicpos = ((Integer) intent.getExtras().get("musicpos")).intValue();

        try {
            InetAddress targetIP = InetAddress.getByName("192.168.49.1");

            Socket clientSocket = null;
            OutputStream os = null;

            try {

                if(isfile) {

                    clientSocket = new Socket(targetIP, port);
                    os = clientSocket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);

                    InputStream is = clientSocket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    DataOutputStream dos= new DataOutputStream(os);

                    dos.writeUTF("0");
                    dos.writeUTF("0");

                    Log.i("TAG", "파일 전송 시작");

                    byte[] buffer = new byte[4096];

                    FileInputStream fis = new FileInputStream(fileToSend);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    // long BytesToSend = fileToSend.length();
                    while (true) {
                        int bytesRead = bis.read(buffer, 0, buffer.length);

                        if (bytesRead == -1) {
                            break;
                        }

                        //BytesToSend = BytesToSend - bytesRead;
                        os.write(buffer, 0, bytesRead);
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

                    Log.i("TAG", "File Transfer Complete, sent file: " + fileToSend.getName());
                }
                else{
                    Log.i("TAG", "상태 전송 시작"+String.valueOf(musicpos));

                    clientSocket = new Socket(targetIP, port);
                    os = clientSocket.getOutputStream();
                    DataOutputStream dos= new DataOutputStream(os);

                    dos.writeUTF(state);
                    dos.writeUTF(String.valueOf(musicpos));


                    dos.close();
                    os.close();


                    clientSocket.close();

                    Log.i("TAG", "상태 전송 끝");

                }


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
