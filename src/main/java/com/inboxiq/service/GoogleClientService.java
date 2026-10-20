package com.inboxiq.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.gmail.Gmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class GoogleClientService {

    @Value("${google.credentials.path:credentials.json}")
    private String credentialsPath;

    private static final String APP_NAME = "InboxIQ";
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/drive.metadata.readonly");

    private Credential credential;
    private Gmail gmail;
    private Drive drive;

    private synchronized Credential credential() throws Exception {
        if (credential != null) return credential;
        File file = new File(credentialsPath);
        if (!file.exists()) {
            throw new IllegalStateException("credentials.json not found at " + file.getAbsolutePath());
        }
        GoogleClientSecrets secrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(), new InputStreamReader(new FileInputStream(file)));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), secrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return credential;
    }

    public synchronized Gmail gmail() throws Exception {
        if (gmail == null) {
            gmail = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), credential()).setApplicationName(APP_NAME).build();
        }
        return gmail;
    }

    public synchronized Drive drive() throws Exception {
        if (drive == null) {
            drive = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), credential()).setApplicationName(APP_NAME).build();
        }
        return drive;
    }
}