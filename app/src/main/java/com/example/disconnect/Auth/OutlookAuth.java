// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.example.disconnect.Auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.disconnect.Mail.Outlook;
import com.example.disconnect.R;
import com.example.disconnect.ui.activity.MailPropertiesActivity;
import com.example.disconnect.ui.activity.VacanceActivity;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Implementation sample for 'Multiple account' mode.
 */
public class OutlookAuth {
    private static final String TAG = OutlookAuth.class.getSimpleName();

    /* UI & Debugging Variables */
    Context ctx;
    ExecutorService service;
    /* Azure AD Variables */
    private IMultipleAccountPublicClientApplication mMultipleAccountApp;
    public static List<IAccount> accountList;
    final String defaultGraphResourceUrl = "https://graph.microsoft.com/v1.0/me";
    boolean isSilent = false;
    boolean mustOpenProperties=false;
    public OutlookAuth(Context context,boolean shouldOpenProperties) {
        this.ctx = context;
        mustOpenProperties=shouldOpenProperties;
        PublicClientApplication.createMultipleAccountPublicClientApplication(ctx,
                R.raw.outlook_credentials,
                new IPublicClientApplication.IMultipleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(IMultipleAccountPublicClientApplication application) {
                        mMultipleAccountApp = application;
                        if (VacanceActivity.preferences.getBoolean("isOutlookConfigured", false)) {
                            Toast.makeText(ctx, ctx.getString(R.string.connexionSilentMessage), Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Connect Silently");
                            isSilent = true;
                            if(mustOpenProperties) {
                                Intent intent = new Intent(ctx, MailPropertiesActivity.class);
                                intent.putExtra("MailProvider", Outlook.class.getSimpleName());
                                ctx.startActivity(intent);
                            }
                            loadAccounts();
                        } else {
                            Toast.makeText(ctx, ctx.getString(R.string.connexionInteractiveMessage), Toast.LENGTH_LONG).show();
                            Log.i(TAG, "connect Interactively");
                            connectInteractively();
                        }
                    }

                    @Override
                    public void onError(MsalException exception) {
                        displayError(exception);
                    }
                });

    }

    public String[] getScopes() {
        return VacanceActivity.preferences.getString("OutlookScopes", "MailboxSettings.ReadWrite").toLowerCase().split(" ");
    }

    private void loadAccounts() {
        if (mMultipleAccountApp == null) {
            return;
        }

        mMultipleAccountApp.getAccounts(new IPublicClientApplication.LoadAccountsCallback() {
            @Override
            public void onTaskCompleted(final List<IAccount> result) {
                // You can use the account data to update your UI or your app database.
                accountList = result;
                if (isSilent)
                    connectSilently();
            }

            @Override
            public void onError(MsalException exception) {
                displayError(exception);
            }
        });
    }

    /**
     * Callback used in for silent acquireToken calls.
     */
    private SilentAuthenticationCallback getAuthSilentCallback() {
        return new SilentAuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.d(TAG, "Successfully authenticated");
                if(mustOpenProperties)
                    MailPropertiesActivity.sentSucessToast();
                else
                    Toast.makeText(ctx,ctx.getString(R.string.connexionSilentMessageSuccess),Toast.LENGTH_SHORT).show();
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                VacanceActivity.preferences.edit()
                        .putString("OutlookToken", authenticationResult.getAccessToken())
                        .putLong("last_update",System.currentTimeMillis())
                        .apply();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                displayError(exception);

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception instanceof MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }
        };
    }

    /**
     * Callback used for interactive request.
     * If succeeds we use the access token to call the Microsoft Graph.
     * Does not check cache.
     */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated");

                String scope = "";
                for (String mScope : authenticationResult.getScope()) {
                    scope += mScope + " ";
                }
                VacanceActivity.preferences.edit()
                        .putString("OutlookScopes", scope)
                        .putString("OutlookToken", authenticationResult.getAccessToken())
                        .putBoolean("isOutlookConfigured",true)
                        .putLong("last_update",System.currentTimeMillis())
                        .apply();
                if(mustOpenProperties) {
                    Intent intent = new Intent(ctx, MailPropertiesActivity.class);
                    intent.putExtra("MailProvider", Outlook.class.getSimpleName());
                    ctx.startActivity(intent);
                }
                loadAccounts();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());
                displayError(exception);

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }


    /**
     * Display the error message
     */
    private void displayError(@NonNull final Exception exception) {
        Log.i(TAG, exception.toString());
    }


    public void removeAccount() {
        if (mMultipleAccountApp == null) {
            return;
        }

        /**
         * Removes the selected account and cached tokens from this app (or device, if the device is in shared mode).
         */
        for (IAccount account : accountList)
            mMultipleAccountApp.removeAccount(accountList.get(accountList.indexOf(account)),
                    new IMultipleAccountPublicClientApplication.RemoveAccountCallback() {
                        @Override
                        public void onRemoved() {
                            Toast.makeText(ctx, "Account removed.", Toast.LENGTH_SHORT)
                                    .show();

                            /* Reload account asynchronously to get the up-to-date list. */
                            loadAccounts();
                        }

                        @Override
                        public void onError(@NonNull MsalException exception) {
                            displayError(exception);
                        }
                    });
    }

    public void connectInteractively() {
        if (mMultipleAccountApp == null) {
            return;
        }

        /**
         * Acquire token interactively. It will also create an account object for the silent call as a result (to be obtained by getAccount()).
         *
         * If acquireTokenSilent() returns an error that requires an interaction,
         * invoke acquireToken() to have the user resolve the interrupt interactively.
         *
         * Some example scenarios are
         *  - password change
         *  - the resource you're acquiring a token for has a stricter set of requirement than your SSO refresh token.
         *  - you're introducing a new scope which the user has never consented for.
         */
        mMultipleAccountApp.acquireToken(((Activity) ctx), getScopes(), getAuthInteractiveCallback());
    }

    public void connectSilently() {
        if (mMultipleAccountApp == null) {
            return;
        }
        final IAccount selectedAccount = accountList.get(0);

        /**
         * Performs acquireToken without interrupting the user.
         *
         * This requires an account object of the account you're obtaining a token for.
         * (can be obtained via getAccount()).
         */
        mMultipleAccountApp.acquireTokenSilentAsync(getScopes(),
                selectedAccount,
                selectedAccount.getAuthority(),
                getAuthSilentCallback());
    }
}
