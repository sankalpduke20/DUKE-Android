package com.dukeai.android;

import android.os.Environment;
import android.os.Handler;

import com.dukeai.android.utils.UserConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class PODEmailComposition implements Runnable {

    int threadNo;
    Handler handler;
    Session session;

    Multipart _multipart = new MimeMultipart();
    ArrayList<byte[]> podDocumentsByteData = new ArrayList<>();
    ArrayList<byte[]> podDocumentsByteDataImg = new ArrayList<>();
    String podReport = "";

    public Message emailComposition(Session session, ArrayList<byte[]> podDocumentsByteData, ArrayList<byte[]> podDocumentsByteDataImg, String podReport) {
        MimeMessage message = new MimeMessage(session);
        UserConfig userConfig = UserConfig.getInstance();
        try {
            message.setFrom(new InternetAddress("dispatcher@duke.ai"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(userConfig.getUserDataModel().getUserEmail()));
            message.setSubject("Proof of delivery documents");
            message.setHeader("Content-Type", "multipart/mixed");

            int i = 1;
            for(byte[] docs: podDocumentsByteDataImg) {
                attachFiles(docs, i, "img", _multipart, new MimeBodyPart());
                i++;
            }
            for(byte[] docs: podDocumentsByteData) {
                attachFiles(docs, i, "pdf", _multipart, new MimeBodyPart());
                i++;
            }

            if(podReport.length()>0) {
                attachFiles(podReport, _multipart, new MimeBodyPart());
            }

            message.setContent(_multipart);
            return message;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return message;
    }

    public PODEmailComposition(Handler handler, Session session, ArrayList<byte[]> podDocumentsByteData, ArrayList<byte[]> podDocumentsByteDataImg, String podReport) {
        this.handler = handler;
        this.session = session;
        this.podDocumentsByteData = podDocumentsByteData;
        this.podDocumentsByteDataImg = podDocumentsByteDataImg;
        this.podReport = podReport;
    }

    @Override
    public void run() {
        threadNo = 1;
        try {
            Transport.send(emailComposition(session, podDocumentsByteData, podDocumentsByteDataImg, podReport));
            Duke.podDocsStoragePaths.clear();
            sendMessage(threadNo, "SUCCESS_EMAIL");
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(threadNo, "FAILED_EMAIL");
        }
    }

    public void sendMessage(int what, String msg) {
        android.os.Message message = handler.obtainMessage(what, msg);
        message.sendToTarget();
    }

    public void attachFiles(byte[] bytes, int documentCount, String attachmentType, Multipart multipart, BodyPart messageBodyPart) {
        try {
            if(attachmentType.equals("pdf")) {
                messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(bytes,"application/pdf")));
            } else {
                messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(bytes,"image/jpg")));
            }
            messageBodyPart.setFileName("Document - " + documentCount);
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void attachFiles(String podReport, Multipart multipart, MimeBodyPart messageBodyPart) {
        try {
            messageBodyPart.attachFile(new File(podReport), "application/pdf", null);
            messageBodyPart.setFileName("POD report");
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}
