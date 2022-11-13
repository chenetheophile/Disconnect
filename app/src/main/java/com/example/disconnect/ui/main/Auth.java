package com.example.disconnect.ui.main;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.disconnect.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

public class Auth extends AppCompatActivity {
    String TAG="Auth";
    SignInClient oneTapClient;
    BeginSignInRequest signInRequest;
    ActivityResultLauncher<IntentSenderRequest> mSigninResult2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //OneTapSignIn(this);
    }
    public void OneTapSignIn(Context ctx){

        mSigninResult2=registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result->{
            if (result.getResultCode() == RESULT_OK) Log.d(TAG, "RESULT_OK.");
            if (result.getResultCode() == RESULT_CANCELED) Log.d(TAG, "RESULT_CANCELED.");
            if (result.getResultCode() == RESULT_FIRST_USER) Log.d(TAG, "RESULT_FIRST_USER.");
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());

                String idToken=credential.getGoogleIdToken();
                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
                    Log.d(TAG, "Got ID token.");
                }
            } catch (ApiException e) {
                // ...
            }
        });
        oneTapClient = Identity.getSignInClient(ctx);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId("1083110443747-k7pid5v3sn96mfovv4am9kqvdgqsf8qc.apps.googleusercontent.com")
                        // Only show accounts previously used to sign in.
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener((Activity) ctx, result -> mSigninResult2.launch(new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build()));
    }
}
