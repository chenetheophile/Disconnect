package com.example.disconnect.Auth;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.disconnect.Mail.Gmail;
import com.example.disconnect.R;
import com.example.disconnect.ui.activity.MailPropertiesActivity;
import com.example.disconnect.ui.activity.VacanceActivity;
import com.google.api.services.gmail.GmailScopes;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Auth {
    static String baseTag = "Auth_";
    private String scope;
    final IMultipleAccountPublicClientApplication[] mMultipleAccountApp = {null};
    final IAccount[] mFirstAccount = {null};
    private final List<String> scopes = Collections.singletonList("http://outlook.office.com/Mail.Read");
    public Context ctx;
    public View view;
    public static UserCredentials credentials;
    String outlook_client_id;
    String gmail_client_id;
    String gmail_client_secret;
    String accessTokenString = "";
    String expiringTime = "-1";
    String token = "";
    boolean hasScopeChanged = false;
    public static GraphServiceClient<okhttp3.Request> graphClient;
    public ISingleAccountPublicClientApplication mSingleAccountApp;
    private IAccount mAccount;

    public Auth(View pView, Context pContext) {
        ctx = pContext;
        view = pView;
    }

    public void OAuthOutlook(boolean shouldOpenProperties) throws InterruptedException {
        String TAG = baseTag + "Outlook";
        outlook_client_id = ctx.getString(R.string.Client_ID_Outlook);
        new OutlookAuth(ctx,shouldOpenProperties);
    }

    public void OAuthGmail(boolean shouldOpenProperties) {
        gmail_client_id = ctx.getString(R.string.WebClient_ID);
        gmail_client_secret = ctx.getString(R.string.WebClient_secret);
        String TAG = baseTag + "Gmail";
        scope = GmailScopes.GMAIL_SETTINGS_BASIC;
        if (!scope.equalsIgnoreCase(VacanceActivity.preferences.getString("scope", null))) {
            Log.i(TAG, "Scope differs, asking for new authorization");
            hasScopeChanged = true;
            getGmailCode(scope, shouldOpenProperties);
        } else {
            Log.i(TAG, "Authorization already given");
            hasScopeChanged=false;
            generateGmailToken(shouldOpenProperties);

        }

    }

    private void getGmailCode(String authorization_code, boolean shouldOpenProperties) {
        String TAG = baseTag + "Gmail";
        WebView auth = ((VacanceActivity)view.getContext()).findViewById(R.id.Auth);
        auth.setVisibility(View.VISIBLE);
        String UserAgent = null;
        try {
            UserAgent = "Chrome/" + ctx.getPackageManager().getPackageInfo("com.android.chrome", 0) + " (Linux; Android " + Build.VERSION.SDK_INT + "; " + Build.MODEL + ")";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        auth.getSettings().setUserAgentString(UserAgent);
        auth.getSettings().setJavaScriptEnabled(true);
        auth.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("GmailCallback")) {
                    auth.setVisibility(View.GONE);
                    String token = Uri.parse(url).getQueryParameter("code");
                    SharedPreferences.Editor editor = VacanceActivity.preferences.edit();
                    editor.putString("GmailToken", token);
                    editor.putString("GmailScope", authorization_code);
                    editor.apply();
                    Log.i(TAG, "GmailToken: " + token);
                    generateGmailToken(shouldOpenProperties);
                }
            }
        });
        HashMap<String, String> params = new HashMap<>();
        params.put("access_type", "offline");
        params.put("client_id", gmail_client_id);
        params.put("response_type", "code");
        params.put("scope", authorization_code);
        params.put("redirect_uri", "https://localhost:8888/GmailCallback");
        String url = "https://accounts.google.com/o/oauth2/auth?";

        url+=HTTP.buildstrUrl(params);

        Log.i(TAG, "This is the url " + url);
        auth.loadUrl(url);
    }

    private void generateGmailToken(boolean shouldOpenProperties) {
        String TAG = baseTag + "Gmail";
        HashMap<String, String> params = new HashMap<>();
        params.put("client_id", gmail_client_id);
        params.put("client_secret", gmail_client_secret);
        Log.i(TAG,VacanceActivity.preferences.getString("GmailToken", null));
        Log.i(TAG, String.valueOf(hasScopeChanged));
        String refresh_token = VacanceActivity.preferences.getString("refresh_token", null);
        if (refresh_token == null || hasScopeChanged) {
            params.put("grant_type", "authorization_code");
            params.put("code", VacanceActivity.preferences.getString("GmailToken", null));
            params.put("redirect_uri", "https://localhost:8888/GmailCallback");
        } else {
            params.put("grant_type", "refresh_token");
            token = refresh_token;
            params.put("refresh_token", refresh_token);
        }
        Map<String, String> result = null;
        try {
            result = HTTP.sendPOST("https://oauth2.googleapis.com/token", params);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        assert result != null;
        Log.i(TAG, "Result of httpPost" + result);

        saveCredential(result);

        assert expiringTime != null;
        Log.i(TAG, "refresh_token: " + token);
        AccessToken accessToken = new AccessToken(accessTokenString, new Date(Integer.parseInt(expiringTime)));
        credentials = UserCredentials.newBuilder()
                .setAccessToken(accessToken)
                .setClientId(gmail_client_id)
                .setClientSecret(gmail_client_secret)
                .setRefreshToken(token)
                .build();
        credentials.createScoped(scope);
        if (shouldOpenProperties) {
            Intent intent = new Intent(ctx, MailPropertiesActivity.class);
            intent.putExtra("Credentials", credentials);
            intent.putExtra("MailProvider", Gmail.class.getSimpleName());
            ctx.startActivity(intent);
        }
    }

    public UserCredentials getGmailToken() {
        OAuthGmail(false);
        return credentials;
    }
    private void saveCredential(Map<String,String> result){
        SharedPreferences.Editor editor = ctx.getSharedPreferences("Disconnect", MODE_PRIVATE).edit();

        for (String key : result.keySet()) {
            switch (key) {
                case "access_token":
                    accessTokenString = result.get(key);
                    break;
                case "expires_in":
                    expiringTime = result.get(key);
                    break;
                case "refresh_token":
                    token = result.get(key);
                    break;
                default:
                    break;

            }
            editor.putString(key, result.get(key));
        }
        editor.apply();
    }
}
