package com.example.disconnect.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.example.disconnect.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;

public class MailPropertiesActivity extends AppCompatActivity {
    private static final String TAG="MailPropertiesActivity";
    private static GsonFactory JSON_FACTORY= GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);
    private static String IDToken;
    private static String UserID;
    private Date expiration;

    private static Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_properties);
        ctx=getApplicationContext();
        EditText objetMail =findViewById(R.id.editTextTextObjetMail);
        EditText reponseMail=findViewById(R.id.reponseMailEditText);
        Switch inclureSignature=findViewById(R.id.signatureSwitch);
        Button sauvegarderMail=findViewById(R.id.sauvegarderButton);
        TextView objetMailActuel=findViewById(R.id.objetMailActuel);
        TextView actualMessage=findViewById(R.id.actualMessageTextView);
        ExecutorService executorService=Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
               getCredentials();
               create();
            } catch (IOException e) {
                Log.i(TAG,e.getMessage());
            } catch (GeneralSecurityException e) {
                Log.i(TAG,e.getMessage());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    private void getCredentials() throws GeneralSecurityException, IOException {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("Disconnect",MODE_PRIVATE);
        IDToken=sharedPreferences.getString("IDtoken","-1");
        Log.i(TAG,IDToken);
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JSON_FACTORY)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(getResources().getString(R.string.Client_ID)))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

// (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(IDToken);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            UserID = payload.getSubject();
            Log.i(TAG,"User ID: " + UserID);
            // Get profile information from payload
            String email = payload.getEmail();
            expiration=new Date(payload.getExpirationTimeSeconds());
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            // Use or store profile information
            // ...

        } else {
            Log.i(TAG,"Invalid ID token.");
        }
    }

    public static Draft create() throws MessagingException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(ctx.getResources().openRawResource(R.raw.credentials));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        Gmail gmailApi=new Gmail(
                new NetHttpTransport(),
                JSON_FACTORY,
                requestInitializer
        );
        String messageSubject = "Test message";
        String bodyText = "lorem ipsum.";

        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("chene.theophile@gmail.com"));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress("chene.theophile@gmail.com"));
        email.setSubject(messageSubject);
        email.setText(bodyText);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        try {
            // Create the draft message
            Draft draft = new Draft();
            draft.setMessage(message);
            draft = gmailApi.users().drafts().create(UserID, draft).execute();
            Log.i(TAG,"Draft id: " + draft.getId());
            Log.i(TAG,draft.toPrettyString());
            return draft;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                Log.e(TAG,"Unable to create draft: " + e.getMessage());
            } else {
                throw e;
            }
        }
        return null;
    }
}