package com.example.disconnect.Auth;

import android.util.Log;

import com.example.disconnect.ui.other.TokenException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HTTP {
    private static final String TAG = "HTTP";
    static String USER_AGENT = "Mozilla/5.0";
    static HashMap<String, String> resultPOST = new HashMap<>();
    static HashMap<String, String> resultGET = new HashMap<>();
    static HashMap<String, String> resultPATCH = new HashMap<>();
    static int nb_retry = 0;

    public static Map<String, String> sendPOST(String Url, HashMap<String, String> params) throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                URL obj = new URL(Url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", USER_AGENT);

                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();

                StringBuilder param = buildstrUrl(params);

//                Log.i(TAG, param.toString());
                os.write(param.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = con.getResponseCode();
//                Log.i(TAG, "POST Response Code :: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    handleHTTPError(con);
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
//                Log.i(TAG, response.toString());
                resultPOST = convertResult(response.toString());
                executorService.shutdown();
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            } catch (TokenException e) {
                e.printStackTrace();
            }
        });
        if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
            throw new InterruptedException("executorService has been interrupted in POST request");
        return resultPOST;
    }

    public static HashMap<String, String> sendGET(String Url, String Access_token) throws InterruptedException {
        Log.i(TAG, "sendGET: " + Access_token);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                URL obj = new URL(Url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", Access_token);
                con.setRequestProperty("User-Agent", USER_AGENT);
                int responseCode = con.getResponseCode();
//                Log.i(TAG, "GET Response Code :: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    handleHTTPError(con);
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
//                Log.i(TAG, response.toString());
                resultGET = convertResult(response.toString());
                executorService.shutdown();
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            } catch (TokenException e) {
                e.printStackTrace();
            }

        });
        if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
            throw new InterruptedException("executorService has been interrupted in GET request");
        return resultGET;
    }

    public static HashMap<String, String> sendPATCH(String Url, JSONObject params, String accessToken) throws InterruptedException {
        Log.i(TAG, "sendPATCH: " + accessToken);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                URL obj = new URL(Url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("PATCH");
                con.setRequestProperty("Content-Type", "application/json;");
                con.setRequestProperty("Authorization", accessToken);
                con.setRequestProperty("User-Agent", USER_AGENT);
//                Log.i(TAG, params.toString());

                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(params.toString().getBytes());
                os.flush();
                os.close();
                con.connect();
                int responseCode = con.getResponseCode();
                Log.i(TAG, "PATCH Response Code :: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    handleHTTPError(con);
                }
//                else{
//                    handleHTTPError(con);
//                }
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                Log.i(TAG, response.toString());
                resultPATCH = convertResult(response.toString());
                executorService.shutdown();
            } catch (IOException  | JSONException  e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            } catch (TokenException e) {
                e.printStackTrace();
                Log.e(TAG, "sendPATCH: Token Error" );
                resultPATCH.put("Error","401");
                executorService.shutdown();
            }
        });
        if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS))
            throw new InterruptedException("executorService has been interrupted in PATCH request");
        Log.i(TAG, "sendPATCH: "+resultPATCH.toString());
        return resultPATCH;
    }

    private static HashMap<String, String> convertResult(String response) throws JSONException {
        HashMap<String, String> result = new HashMap<>();
        JSONObject jsonObject = new JSONObject(response);
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            result.put(key, jsonObject.getString(key));
        }
        return result;
    }

    public static StringBuilder buildstrUrl(HashMap<String, String> params) {
        int numKey = params.size();
        StringBuilder url = new StringBuilder();
        for (String key : params.keySet()) {
            url.append(key).append("=").append(params.get(key));
            numKey--;
            if (numKey != 0) {
                url.append("&");

            }

        }
        return url;
    }

    private static void handleHTTPError(HttpURLConnection con) throws TokenException, IOException {
        if (con.getResponseCode() == 401 && con.getResponseMessage().contains("Unauthorized")) {
            Log.e(TAG, "has to refreshing Token");
            throw new TokenException();
        }
        Log.e(TAG, con.getRequestMethod() + " request did not work.");
        Log.e(TAG, con.getResponseMessage());
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        Log.e(TAG, response.toString());
    }
}
