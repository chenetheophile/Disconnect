package com.example.disconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int SEND_SMS_PERMISSION=100;
    private static final int RECEIVE_SMS_PERMISSION=101;
    private static final int READ_EXTERNAL_STORAGE=102;
    private static final int WRITE_EXTERNAL_STORAGE=103;
    private static final int INTERNET=104;
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        String message="";
        switch (requestCode){
            case SEND_SMS_PERMISSION:
                message="Send sms";
                break;
            case RECEIVE_SMS_PERMISSION:
                message="Receive sms";
                break;
            case WRITE_EXTERNAL_STORAGE:
                message="Write external storage";
                break;
            case READ_EXTERNAL_STORAGE:
                message="Read external storage";
                break;
            case INTERNET:
                message="Internet";
                break;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, message+"Permission Granted", Toast.LENGTH_SHORT) .show();
        }
        else {
            Toast.makeText(MainActivity.this, message +"Send sms Permission Denied", Toast.LENGTH_SHORT) .show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        ImageButton modeVacance=findViewById(R.id.modeVacance);
        modeVacance.setOnClickListener(v -> {
            Intent intent=new Intent(this,VacanceActivity.class);
            startActivity(intent);
        });
        RelativeLayout background=findViewById(R.id.backgroundLayout);
        checkPermission(Manifest.permission.SEND_SMS,SEND_SMS_PERMISSION);
        checkPermission(Manifest.permission.RECEIVE_SMS,RECEIVE_SMS_PERMISSION);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE);
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE);
        checkPermission(Manifest.permission.INTERNET,INTERNET);
    }
}