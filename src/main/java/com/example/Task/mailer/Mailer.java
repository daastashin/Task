package com.example.Task.mailer;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.Set;

@Component
public class Mailer {

    private Gmail service;

    public Mailer() {
        NetHttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException | GeneralSecurityException e) {
            System.out.println(e.getMessage());
        }
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        try {
            service = new Gmail.Builder(httpTransport, jsonFactory, getCredentials(httpTransport,jsonFactory))
                    .setApplicationName("APPLICATION_NAME")
                    .build();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static Credential getCredentials(final NetHttpTransport httpTransport, GsonFactory jsonFactory)
            throws IOException {
        File file = new File("E:\\Task\\src\\main\\resources\\test.json");
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file));

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(jsonFactory, isr);

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Set.of(GmailScopes.GMAIL_SEND))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) {
        new Mailer().sendEmail("subject", "message");
    }

    public void sendEmail(String contactEmail, String code) {

        /* Encode as MIME message */
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        try {
            email.setFrom(new InternetAddress(contactEmail));
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(contactEmail));
            email.setSubject("Auth code");
            email.setText(code);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            email.writeTo(buffer);
        } catch (IOException | MessagingException e) {
            System.out.println("Exception");;
        }
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message msg = new Message();
        msg.setRaw(encodedEmail);

        try {
            msg = service.users().messages().send("me", msg).execute();
            System.out.println("Message id: " + msg.getId());
            System.out.println(msg.toPrettyString());
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to send message: " + e.getDetails());
            } else {
                System.out.println("other exception");;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
