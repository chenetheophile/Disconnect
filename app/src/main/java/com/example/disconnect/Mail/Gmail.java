package com.example.disconnect.Mail;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.model.VacationSettings;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* Class to demonstrate the use of Gmail Enable Auto Reply API*/
public class Gmail implements Mail{
    String baseTAG="Gmail_";
    UserCredentials credentials;
    com.google.api.services.gmail.Gmail service;
    public Gmail(UserCredentials pCredentials){
        this.credentials=pCredentials;
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        // Create the gmail API client
        service = new com.google.api.services.gmail.Gmail.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Disconnect")
                .build();
    }
    @Override
    public void setAutoReply(String answer, @Nullable String object, @NonNull Long starting_date,@NonNull Long ending_date, boolean active) throws IOException, InterruptedException {
        String TAG=baseTAG+"setAutoReply";
        ExecutorService executorService=Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            try {
                // Enable auto reply by restricting domain with start time and end time
                VacationSettings vacationSettings = new VacationSettings()
                        .setEnableAutoReply(active)
                        .setResponseBodyHtml(
                                answer)
                        .setRestrictToDomain(true);
                if(starting_date!=-1) {
                    vacationSettings.setStartTime(starting_date);
                }
                if (ending_date!=-1){
                    vacationSettings.setEndTime(ending_date);
                }
                if(object!=null){
                    vacationSettings.setResponseSubject(object);
                }
                VacationSettings response = service.users().settings()
                        .updateVacation("me", vacationSettings).execute();
                // Prints the auto-reply response body
                if(active)
                    Log.i(TAG,"Enabled auto reply with message : " + response.getResponseBodyHtml());
                else
                    Log.i(TAG,"Disabled auto reply with message: "+response.getResponseBodyHtml());
                executorService.shutdown();
            } catch (GoogleJsonResponseException e) {
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 403) {
                    Log.e(TAG, "Unable to enable auto reply: " + e.getDetails());
                }
            }catch (IOException e){
                Log.e(TAG, Arrays.toString(e.getStackTrace()));
            }
        });
        if(!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
            throw new InterruptedException("service has been interrupted");

    }
    @Override
    public HashMap<String,String> getAutoReply() {
        String TAG=baseTAG+"getAutoReply";
        HashMap<String,String >result=new HashMap<>();
        ExecutorService executorService= Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            try {
                VacationSettings vacation = service.users().settings().getVacation("me").execute();
                Log.i(TAG,"Vacation: "+vacation.getResponseBodyHtml());
                Log.i(TAG,"Object: "+vacation.getResponseSubject());
                Log.i(TAG,"Time: "+vacation.getStartTime()+" "+vacation.getEndTime());
                if(vacation.getResponseBodyHtml()!=null)
                    result.put("body", Jsoup.parse(vacation.getResponseBodyHtml()).text());
                result.put("object",vacation.getResponseSubject());
                if(vacation.getStartTime()!=null)
                    result.put("startTime",vacation.getStartTime().toString());
                if(vacation.getEndTime()!=null)
                    result.put("endTime",vacation.getEndTime().toString());
                executorService.shutdown();
            }catch (GoogleJsonResponseException e){
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 403) {
                    Log.e(TAG,"Unable to enable auto reply: " + e.getDetails());
                }
                else Log.e(TAG,e.getMessage());
            }catch (IOException e){
                Log.e(TAG,e.getMessage());
            }
        });
        try {
            if(!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
                throw new InterruptedException("service has been interrupted");

        }catch (InterruptedException e){
            Log.e(TAG,e.getMessage());
        }
        return result;
    }
    @Override
    public boolean enableAutoReply(@NonNull Long starting_date, @NonNull Long ending_date,boolean active) {
        String TAG=baseTAG+"enableAutoReply";
        try {
            Log.i(TAG, "enabling auto reply");
            HashMap<String, String> result = getAutoReply();
            Log.i(TAG, result.toString());
            setAutoReply(result.get("body"), result.get("object"), starting_date, ending_date, active);
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Gmail";
    }
}