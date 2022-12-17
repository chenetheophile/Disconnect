package com.example.disconnect.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.disconnect.Auth.Auth;
import com.example.disconnect.Auth.OutlookAuth;
import com.example.disconnect.Mail.Gmail;
import com.example.disconnect.Mail.Outlook;
import com.example.disconnect.R;
import com.example.disconnect.ui.activity.VacanceActivity;
import com.example.disconnect.ui.other.TokenException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MailProviderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MailProviderFragment extends Fragment {
    private static String TAG = "MailProviderFragment";
    public static Context ctx;
    private View MailView;
    private static int nbRetryGmail = 0;
    private static int nbRetryOutlook = 0;
    public static boolean isGmailActive = false;
    public static boolean isMailActive = false;
    private Auth auth;
    private Gmail gmail;
    private Outlook outlook;
    private Switch switchGmail;
    private Switch switchOutlook;
    private Switch switchMail;
    public static boolean isOutlookActive = false;

    public MailProviderFragment() {
    }

    public static MailProviderFragment newInstance() {
        return new MailProviderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this.getContext();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mail_provider, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Resumed");
        updateUI();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        auth = new Auth(view, ctx);
        this.MailView = view;
        ImageButton outlookButton = view.findViewById(R.id.outlookButton);
        ImageButton gmailButton = view.findViewById(R.id.gmailButton);
        switchGmail = view.findViewById(R.id.switchGmail);
        switchOutlook = view.findViewById(R.id.switchOutlook);
        switchMail = view.findViewById(R.id.mailSwitch);

        gmailButton.setOnClickListener(v -> {
            auth.OAuthGmail(true);
        });
        outlookButton.setOnClickListener(v -> {
            try {
                auth.OAuthOutlook(true);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        });


        switchGmail.setOnClickListener(v -> {
            if (switchGmail.isChecked()) {
                checkDate(Gmail.class.getSimpleName());

            } else {
                deactivateMail(Gmail.class.getSimpleName());

            }
            updateUI();
        });

        switchOutlook.setOnClickListener(v -> {
            if (switchOutlook.isChecked()) {
                checkDate(Outlook.class.getSimpleName());
            } else {
                deactivateMail(Outlook.class.getSimpleName());

            }
            updateUI();
        });
        switchMail.setOnClickListener(v -> {
            if (!switchMail.isChecked()) {
                Log.i(TAG, "deactivating mail");
                if (switchGmail.isChecked()) {
                    deactivateMail(Gmail.class.getSimpleName());

                }
                if (switchOutlook.isChecked()) {
                    deactivateMail(Outlook.class.getSimpleName());
                }
            } else {
                Log.i(TAG, "Activating mail");
                if (!switchOutlook.isChecked())
                    if (VacanceActivity.preferences.getBoolean("isOutlookPropertiesConfigured", false)) {
                        Log.i(TAG, "activating outlook as it is configured");
                        checkDate(Outlook.class.getSimpleName());

                    }
                if (!switchGmail.isChecked())
                    if (VacanceActivity.preferences.getBoolean("isGmailPropertiesConfigured", false)) {
                        Log.i(TAG, "activating gmail as it is configured");
                        checkDate(Gmail.class.getSimpleName());
                    }
            }
            updateUI();
        });
    }

    public void mailEnablingError(Switch mswitch, String serviceName, boolean active) {
        if (serviceName.equals(Outlook.class.getSimpleName())) {
            isOutlookActive = !active;
        } else {
            isGmailActive = !active;
        }
        mswitch.setChecked(!active);
        Toast.makeText(ctx, getString(R.string.erreurActivationMail), Toast.LENGTH_LONG).show();

    }

    public void deactivateMail(String service) {
        boolean active = false;
        try {
            if (service.equals(Outlook.class.getSimpleName())) {
                nbRetryOutlook++;
                if (!outlook.enableAutoReply((long) -1, (long) -1, active)) {
                    mailEnablingError(switchOutlook, service, active);
                    return;
                }
                isOutlookActive = active;
                nbRetryOutlook = 0;
            } else {
                nbRetryGmail++;
                if (!gmail.enableAutoReply((long) -1, (long) -1, active)) {
                    mailEnablingError(switchGmail, service, active);
                    return;
                }
                isGmailActive = active;
                nbRetryGmail = 0;
            }
        } catch (TokenException e) {
            handleTokenException(service, -1, -1, active);
        }
    }

    public void activateMail(String service) {
        boolean active = true;
        try {
            if (service.equals(Outlook.class.getSimpleName())) {
                nbRetryOutlook++;
                if (!outlook.enableAutoReply((long) -1, (long) -1, active)) {
                    mailEnablingError(switchOutlook, service, active);
                    return;
                }
                isOutlookActive = active;
                nbRetryOutlook = 0;
            } else {
                nbRetryGmail++;
                if (!gmail.enableAutoReply((long) -1, (long) -1, active)) {
                    mailEnablingError(switchGmail, service, active);
                    return;
                }
                isGmailActive = active;
                nbRetryGmail = 0;
            }
        } catch (TokenException e) {
            Log.e(TAG, "activateMail: Token Exception");
            handleTokenException(service, -1, -1, active);
        }
    }

    public void activateMail(String service, long starting, long ending) {
        boolean active = true;
        try {
            if (service.equals(Outlook.class.getSimpleName())) {
                nbRetryOutlook++;
                if (!outlook.enableAutoReply(starting, ending, active)) {
                    mailEnablingError(switchOutlook, service, active);
                    return;
                }
                isOutlookActive = active;
                nbRetryOutlook = 0;
            } else {
                nbRetryGmail++;
                if (!gmail.enableAutoReply(starting, ending, active)) {
                    mailEnablingError(switchGmail, service, active);
                    return;
                }
                isGmailActive = active;
                nbRetryGmail = 0;
            }
        } catch (TokenException e) {
            Log.e(TAG, "activateMail: Token Exception");
            handleTokenException(service, starting, ending, active);
        }
    }

    public void updateUI() {
        ((VacanceActivity) getActivity()).updatePreference();
        TextView gmailConfigure = MailView.findViewById(R.id.isGmailConfigured);
        TextView outlookConfigure = MailView.findViewById(R.id.isOutlookConfigured);
        switchGmail.setChecked(isGmailActive);
        switchOutlook.setChecked(isOutlookActive);
        if (switchGmail.isChecked() || switchOutlook.isChecked())
            switchMail.setChecked(true);
        if (!switchOutlook.isChecked() && !switchGmail.isChecked())
            switchMail.setChecked(false);
        if (VacanceActivity.preferences.getBoolean("isGmailPropertiesConfigured", false)) {
            gmailConfigure.setText(getString(R.string.configurer));
            MailView.findViewById(R.id.layoutSwitchGmail).setVisibility(View.VISIBLE);
            if (gmail == null) gmail = new Gmail(auth.getGmailToken());
        }
        if (VacanceActivity.preferences.getBoolean("isOutlookPropertiesConfigured", false)) {
            outlookConfigure.setText(getString(R.string.configurer));
            MailView.findViewById(R.id.layoutSwitchOutlook).setVisibility(View.VISIBLE);
            Log.i(TAG, "updateUI: " + ((System.currentTimeMillis() - VacanceActivity.preferences.getLong("last_update", System.currentTimeMillis() + 3600000)) / 60000 >= 60));
            if (outlook == null || (System.currentTimeMillis() - VacanceActivity.preferences.getLong("last_update", System.currentTimeMillis() + 3600000)) / 60000 >= 60)
                outlook = new Outlook(ctx);
            outlook.updateToken();
        }

    }

    public void checkDate(String service) {
        SimpleDateFormat df2 = new SimpleDateFormat(" dd/MM/yyyy", Locale.FRENCH);
        if (VacanceActivity.preferences.getBoolean("isDateConfigured", false)) {
            long debut = VacanceActivity.preferences.getLong(service + "_starting_date", -1);
            long fin = VacanceActivity.preferences.getLong(service + "_ending_date", -1);
            Date begin = new Date(debut);
            Date end = new Date(fin);
            AlertDialog.Builder alert = new AlertDialog.Builder(ctx)
                    .setTitle(R.string.DateAlertTitle);
            String message = getString(R.string.DateQuestion);

            if (end.getTime() >= begin.getTime()) {
                message += getString(R.string.du) + " " + df2.format(begin) + "\n" + getString(R.string.au) + " " + df2.format(end);
            } else {
                message += "A partir " + getString(R.string.du).toLowerCase() + df2.format(begin);
            }
            if (service.equals(Outlook.class.getSimpleName()))
                alert.setIcon(R.mipmap.outlook_foreground);
            else
                alert.setIcon(R.mipmap.gmail_foreground);
            alert.setMessage(message)
                    .setPositiveButton("Oui", (dialog, which) -> {
                        Log.i(TAG, "Alert dialog has been accepted");
                        activateMail(service, debut, fin);
                    })
                    .setNegativeButton("Non", (dialog, which) -> {
                        Log.i(TAG, "Alert dialog has been declined");
                        activateMail(service);
                    }).show();
        }
        activateMail(service);
    }

    private void handleTokenException(String service, long starting, long ending, boolean active) {
        Log.e(TAG, "handleTokenException: refreshing Token");
        ExecutorService executorService= Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            new OutlookAuth(ctx,false);
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        outlook=new Outlook(ctx);
        if (nbRetryGmail > 3 || nbRetryOutlook > 3) {
            if (service.equals(Gmail.class.getSimpleName())) {
                mailEnablingError(switchGmail, service, active);
            } else if (service.equals(Outlook.class.getSimpleName())) {
                mailEnablingError(switchOutlook, service, active);
            }
            return;
        }
        if (active) {
            activateMail(service, starting, ending);
        } else {
            deactivateMail(service);
        }
    }
}