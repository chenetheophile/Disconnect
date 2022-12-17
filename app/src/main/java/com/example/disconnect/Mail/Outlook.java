package com.example.disconnect.Mail;

import android.content.Context;
import android.util.Log;

import com.example.disconnect.Auth.HTTP;
import com.example.disconnect.ui.activity.VacanceActivity;
import com.example.disconnect.ui.other.TokenException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;

public class Outlook implements Mail {
    String baseTAG = "Outlook_";
    String accessToken;
    Context context;
    final String defaultGraphResourceUrl = "https://graph.microsoft.com/v1.0/me";
    static HashMap<String, String> result;
    public Outlook(Context ctx) {
        context = ctx;
        updateToken();
        Log.i(baseTAG, "Outlook: "+accessToken);
    }

    @Override
    public void setAutoReply(String answer, @Nullable String object,Long starting_date,Long ending_date, boolean active) throws InterruptedException,TokenException {
        String TAG=baseTAG+"setAutoReply";
        try {
            JSONObject automaticRepliesSettings = new JSONObject();
            JSONObject params = new JSONObject();
            if (active) {
                automaticRepliesSettings.put("externalReplyMessage", answer);
                automaticRepliesSettings.put("internalReplyMessage", answer);
                automaticRepliesSettings.put("status", "scheduled");
                if (starting_date == -1 && ending_date == -1)
                    automaticRepliesSettings.put("status", "alwaysEnabled");
                else
                    automaticRepliesSettings.put("scheduledStartDateTime", convertMiliToOutlookJsonDate(starting_date));
                if (ending_date != -1) {
                    automaticRepliesSettings.put("scheduledEndDateTime", convertMiliToOutlookJsonDate(ending_date));
                }
            } else {
                automaticRepliesSettings.put("status", "disabled");
            }
            params.put("automaticRepliesSetting", automaticRepliesSettings);
            HashMap<String,String>result = HTTP.sendPATCH(defaultGraphResourceUrl + "/mailboxSettings", params, accessToken);
                Log.i(TAG, "setAutoRaply: "+result.toString());
                if(result.get("Error")!=null){
                    Log.e(TAG, "enableAutoReply: TokenException: has to refresh token" );
                    throw new TokenException();
                };
        }catch (JSONException e){
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<String, String> getAutoReply() throws TokenException{
        String TAG = baseTAG + "getAutoReply";
        HashMap<String,String>vacationsettings=new HashMap<>();
        try {
            HashMap<String,String>result=HTTP.sendGET(defaultGraphResourceUrl + "/mailBoxSettings/automaticRepliesSetting",accessToken);
            Log.i(TAG, "getAutoReply: "+result.toString());
            if(result.get("Error")!=null){
                Log.e(TAG, "getAutoReply: TokenException: has to refresh token" );
                throw new TokenException();
            };
            vacationsettings.put("body", result.get("externalReplyMessage"));
            Log.i(TAG,result.get("scheduledStartDateTime"));
            vacationsettings.put("startTime", convertOutlookJsonDateToMili(result.get("scheduledStartDateTime")));
            vacationsettings.put("endTime", convertOutlookJsonDateToMili(result.get("scheduledEndDateTime")));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return vacationsettings;
    }
    @Override
    public boolean enableAutoReply(Long starting_date, Long ending_date, boolean active) throws TokenException {
        String TAG=baseTAG+"enableAutoReply";
        try {
            JSONObject automaticRepliesSettings = new JSONObject();
            JSONObject params = new JSONObject();
            if (active) {
                automaticRepliesSettings.put("status", "scheduled");
                if (starting_date == -1 && ending_date == -1)
                    automaticRepliesSettings.put("status", "alwaysEnabled");
                else
                    automaticRepliesSettings.put("scheduledStartDateTime", convertMiliToOutlookJsonDate(starting_date));
                if (ending_date != -1) {
                    automaticRepliesSettings.put("scheduledEndDateTime", convertMiliToOutlookJsonDate(ending_date));
                }
            } else {
                automaticRepliesSettings.put("status", "disabled");
            }
            params.put("automaticRepliesSetting", automaticRepliesSettings);
            HashMap<String,String> result=HTTP.sendPATCH(defaultGraphResourceUrl + "/mailboxSettings", params, accessToken);
            Log.i(TAG, "enableAutoReply: "+result.toString());
            if(result.get("Error")!=null){
                Log.e(TAG, "enableAutoReply: TokenException: has to refresh token" );
                throw new TokenException();
            };
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static String convertOutlookJsonDateToMili(String outlookDate) {
        HashMap<String,String> result=new Gson().fromJson(
                outlookDate, new TypeToken<HashMap<String, String>>() {}.getType());
        return String.valueOf(Instant.parse( result.get("dateTime")+ "Z").toEpochMilli());
    }
    public static JSONObject convertMiliToOutlookJsonDate(long mili)  {
        HashMap<String,String> result=new HashMap<>();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:00:00.0000000", Locale.FRENCH);
        String text = sdf.format(mili);
        result.put("dateTime",text);
        result.put("timeZone","UTC");
        return new JSONObject(result);
    }

    @Override
    public String toString() {
        return "Outlook";
    }
    public void updateToken(){
        Log.d(baseTAG+"updateToken", "updateToken: Token update");
        this.accessToken= VacanceActivity.preferences.getString("OutlookToken", "");

    }
}
