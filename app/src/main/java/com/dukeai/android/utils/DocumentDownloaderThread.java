package com.dukeai.android.utils;

import android.os.Handler;
import android.os.Message;

import java.io.OutputStream;

public class DocumentDownloaderThread implements Runnable {

    int threadNo;
    Handler handler;
    OutputStream os;
    int noOfBytes;

    public DocumentDownloaderThread(int threadNo, Handler handler, OutputStream os, int noOfBytes) {
        this.threadNo = threadNo;
        this.handler = handler;
        this.os = os;
        this.noOfBytes = noOfBytes;
    }

    @Override
    public void run() {

        try {
            os.write(noOfBytes);
            sendMessage(threadNo, "SUCCESS");
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(threadNo, "FAILED");
        }
    }

    public void sendMessage(int what, String msg) {
        Message message = handler.obtainMessage(what, msg);
        message.sendToTarget();
    }

}
