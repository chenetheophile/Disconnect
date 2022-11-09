package com.example.disconnect;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/* class to demonstrate use of Gmail list labels API */
public class GmailAuth {
    private static final String TAG="GmailAuth";
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Disconnect";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static String TOKENS_DIRECTORY_PATH="/tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);
    private static InputStream CREDENTIALS_FILE_PATH;

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        File tokenFolder = new File(TOKENS_DIRECTORY_PATH);
        if (CREDENTIALS_FILE_PATH == null) {

            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH.toString());
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(CREDENTIALS_FILE_PATH));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokenFolder))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }
    public GmailAuth(Context ctx) throws IOException, GeneralSecurityException{
        // Build a new authorized API client service.
        Log.i(TAG,"GmailAuth Starting");
        CREDENTIALS_FILE_PATH=ctx.getResources().openRawResource(R.raw.credentials);
        TOKENS_DIRECTORY_PATH=ctx.getExternalFilesDir("").getAbsolutePath()+"/tokens";
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Log.i(TAG,"Gmail Service is retrieved");
        // Print the labels in the user's account.
        String user = "ClientDisconnect";
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.isEmpty()) {
            Log.i(TAG,"No labels found.");
        } else {
            Log.i(TAG,"Labels:");
            for (Label label : labels) {
                Log.i(TAG,"- "+ label.getName());
            }
        }
    }

}
