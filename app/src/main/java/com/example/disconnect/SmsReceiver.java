package com.example.disconnect;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.disconnect.ui.main.SMSFragment;

import java.util.ArrayList;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG =
            SmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"IsSMSActive: "+SMSFragment.isSmsActive);
        if(SMSFragment.isSmsActive) {
            // Get the SMS message.
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String strMessage = "";
            String format = bundle.getString("format");
            // Retrieve the SMS message received.
            Object[] pdus = (Object[]) bundle.get(pdu_type);
            if (pdus != null) {
                // Check the Android version.
                boolean isVersionM =
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                // Fill the msgs array.
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    // Check Android version and use appropriate createFromPdu.
                    if (isVersionM) {
                        // If Android version M or newer:
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        // If Android version L or older:
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    // Build the message to show.
                    strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                    strMessage += " :" + msgs[i].getMessageBody() + "\n";
                    // Log and display the SMS message.
                    Log.d(TAG, "onReceive: " + strMessage);
                    smsSendMessage(msgs[i].getOriginatingAddress(),context);
                }
            }
        }
    }
    public void smsSendMessage(String destinationAddress,Context ctx) {
        // Get the text of the SMS message.
        try {
            SharedPreferences settings = ctx.getSharedPreferences("Disconnect", Context.MODE_PRIVATE);

            String smsMessage = settings.getString("SMS_Text", ctx.getResources().getString(R.string.reponseAutomatiqueDefault));
            Log.i(TAG, smsMessage);

            PendingIntent sentIntent = null, deliveryIntent = null;
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(smsMessage);

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentIntent);
            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(deliveryIntent);

            smsManager.sendMultipartTextMessage(destinationAddress, null, parts, sendList, deliverList);
        }catch (Exception exception){
            Log.e(TAG,exception.getMessage());
        }
    }
}