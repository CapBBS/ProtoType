package com.example.administrator.test1;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Administrator on 2016-05-12.
 */
public class IPreceiver extends Thread{
    Socket socket;
    DataInputStream dis;

    public IPreceiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());

            Constants.CLIENT_ADRRESS = dis.readUTF();

            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
