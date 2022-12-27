package com.dukeai.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.dukeai.android.Duke;
import com.dukeai.android.apiUtils.ApiConstants;
import com.dukeai.android.models.UserDataModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloaderThread implements Runnable {

    String parentFolderName = "DUKE.AI";

    int threadNo;
    Handler handler;
    URL imageUrl;
    String imageName;
    String folderName;
    String sub_folder;
    File file;

    public ImageDownloaderThread() {
    }

    public ImageDownloaderThread(int threadNo, Handler handler, URL imageUrl, String imageName, String folderName) {
        this.threadNo = threadNo;
        this.handler = handler;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.folderName = folderName;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        UserDataModel userDataModel = UserConfig.getInstance().getUserDataModel();
        // So download the image from this url
        try {

            // Initialize a new http url connection
            connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestProperty("Authorization", userDataModel.getStoredJWTToken());
            connection.setRequestProperty("Content-Type", ApiConstants.DownloadFile.CONTENT_TYPE);
            connection.setRequestProperty("Accept", ApiConstants.DownloadFile.ACCEPT);
            // Connect the http url connection
            connection.connect();

            // Convert BufferedInputStream to Bitmap object
            Bitmap bmp = BitmapFactory.decodeStream(new BufferedInputStream(connection.getInputStream()));

            /**Save Image to External Storage**/
            saveImageToInternalStorage(bmp);

            sendMessage(threadNo, "SUCCESS");
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(threadNo, "FAILED");
        } finally {
            // Disconnect the http url connection
            connection.disconnect();
        }
    }

    public void saveImageToInternalStorage(Bitmap bitmap) {

        sub_folder = "POD Documents";

        File myDir0 = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), parentFolderName);
        if (!myDir0.exists()) {
            myDir0.mkdirs();
        }

        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + parentFolderName, sub_folder);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File myDir2;

        if (Duke.PODDocumentsPath != "") {
            myDir2 = new File(Environment.getExternalStorageDirectory() + "/" + parentFolderName + "/" + sub_folder, Duke.PODDocumentsPath);

            if (!myDir2.exists()) {
                myDir2.mkdirs();
            }
        } else {
            myDir2 = new File(Environment.getExternalStorageDirectory() + "/" + parentFolderName + "/" + sub_folder, folderName);

            if (!myDir2.exists()) {
                myDir2.mkdirs();
            }
        }

        file = new File(myDir2, imageName);

        try {
            // Initialize a new OutputStream
            OutputStream stream = null;


            // If the output file exists, it can be replaced or appended to it
            stream = new FileOutputStream(file);

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            // Flushes the stream
            stream.flush();

            // Closes the stream
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(int what, String msg) {
        Message message = handler.obtainMessage(what, msg);
        message.sendToTarget();
    }
}
