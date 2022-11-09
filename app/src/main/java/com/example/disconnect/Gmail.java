package com.example.disconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.disconnect.R;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Gmail extends AppCompatActivity {
    private static final String TAG="Gmail";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail);
        Log.i(TAG,"GmailActivity is started");
        try {
            GmailAuth gmailAuth=new GmailAuth(getApplicationContext());
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        } catch (GeneralSecurityException e) {
            Log.e(TAG,e.getMessage());
        }
    }
}