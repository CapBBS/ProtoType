package com.example.administrator.test1;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-12.
 */
public class IPsender extends Thread{
    Socket socket;
    DataOutputStream dos;
    String clientaddress;

    public IPsender(Socket socket) {
        this.socket = socket;
        clientaddress = socket.getLocalAddress().getHostAddress();
        try {
            // 데이터 전송용 스트림 생성
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            dos.writeUTF(clientaddress);

            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
