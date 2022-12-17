package com.example.disconnect.ui.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.disconnect.Mail.Gmail;
import com.example.disconnect.Mail.Mail;
import com.example.disconnect.Mail.Outlook;
import com.example.disconnect.R;
import com.example.disconnect.ui.fragment.MailProviderFragment;
import com.example.disconnect.ui.other.TokenException;
import com.google.auth.oauth2.UserCredentials;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MailPropertiesActivity extends AppCompatActivity {
    private static final String TAG = "MailPropertiesActivity";
    private static Context ctx;
    final Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    int beginDay, beginMonth, beginYear;
    long debutAutoReponse = -1;
    long finAutoReponse = -1;
    TextView objetMailActuel;
    TextView actualMessage;
    TextView startTime;
    TextView endTime;
    Mail mailService;
    private TextView objetMail;
    private TextView reponseMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_properties);
        getSupportActionBar().hide();
        ctx = getApplicationContext();

        objetMail = findViewById(R.id.editTextTextObjetMail);
        reponseMail = findViewById(R.id.reponseMailEditText);
        Button sauvegarderMail = findViewById(R.id.sauvegarderButton);
        objetMailActuel = findViewById(R.id.objetMailActuel);
        actualMessage = findViewById(R.id.actualMessageTextView);
        startTime = findViewById(R.id.startTimeTextView);
        endTime = findViewById(R.id.endTimeTextView);
        EditText dateDebut = findViewById(R.id.DateDebut);
        EditText dateFin = findViewById(R.id.DateFin);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        if (getIntent().getExtras().get("MailProvider").equals(Gmail.class.getSimpleName())) {
            UserCredentials credentials = (UserCredentials) getIntent().getExtras().get("Credentials");
            mailService = new Gmail(credentials);
        } else {
            mailService = new Outlook(ctx);
            objetMailActuel.setVisibility(View.GONE);
            objetMail.setVisibility(View.GONE);
            findViewById(R.id.objetTextView1).setVisibility(View.GONE);
            findViewById(R.id.objetTextView2).setVisibility(View.GONE);
        }

        dateDebut.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MailPropertiesActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        beginYear = year1;
                        beginMonth = monthOfYear;
                        beginDay = dayOfMonth;
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        dateDebut.setText(date);
                        c.set(year1, monthOfYear, dayOfMonth);
                        debutAutoReponse = c.getTimeInMillis();
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        dateFin.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    // on below line we are passing context.
                    MailPropertiesActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        if (year1 <= beginYear && monthOfYear <= beginMonth && dayOfMonth <= beginDay) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.erreurCalendrier), Toast.LENGTH_LONG).show();
                            dateFin.callOnClick();
                        }
                        Calendar end = Calendar.getInstance();
                        end.set(year1, monthOfYear, dayOfMonth);
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        dateFin.setText(date);
                        c.set(year1, monthOfYear, dayOfMonth);
                        finAutoReponse = c.getTimeInMillis();
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        sauvegarderMail.setOnClickListener(v -> sauvegarderMailParameters());
        updateUI();
    }

    public void updateUI() {

        try {
            Date date;
            SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
            HashMap<String, String> vacation_settings = mailService.getAutoReply();
            Log.i(TAG, vacation_settings.toString());
            actualMessage.setText(vacation_settings.get("body"));
            objetMailActuel.setText(vacation_settings.get("object"));
            if (vacation_settings.get("startTime") != null) {
                date = new Date(Long.parseLong(vacation_settings.get("startTime")));
                startTime.setText(df2.format(date));
            } else {
                startTime.setText(df2.format(System.currentTimeMillis()));
            }
            if (vacation_settings.get("endTime") != null) {
                date = new Date(Long.parseLong(vacation_settings.get("endTime")));
                endTime.setText(df2.format(date));
            } else {
                endTime.setText("");
            }

        } catch (NullPointerException | TokenException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void sauvegarderMailParameters() {
        SharedPreferences.Editor editor = VacanceActivity.preferences.edit();
        try {
            String body = "";
            String object = "";
            if (reponseMail.getText().toString().equals(""))
                body = actualMessage.getText().toString();
            else
                body = reponseMail.getText().toString();
            if (objetMail.getText().toString().equals(""))
                object = objetMailActuel.getText().toString();
            else
                object = objetMail.getText().toString();
            if (body.equals("") && object.equals("")) {
                Toast.makeText(ctx, getString(R.string.erreurSauvegarde), Toast.LENGTH_LONG).show();
                throw new Exception("Can't create or update vacation settings with o body nor object");
            }
            Log.i(TAG, String.valueOf(debutAutoReponse));
            mailService.setAutoReply(body, object, debutAutoReponse, finAutoReponse, true);
            if (debutAutoReponse != -1)
                editor.putBoolean("isDateConfigured", true)
                        .putLong(mailService.toString()+"_starting_date", debutAutoReponse)
                        .putLong(mailService.toString()+"_ending_date",finAutoReponse);
            editor.putBoolean("is" + mailService.toString() + "PropertiesConfigured", true);
            editor.apply();
            Toast.makeText(ctx, getString(R.string.mailSauvegarder), Toast.LENGTH_LONG).show();
            if(mailService.toString().equals(Outlook.class.getSimpleName())){
                MailProviderFragment.isOutlookActive=true;
            }else{
                MailProviderFragment.isGmailActive=true;
            }
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sentSucessToast() {
        Toast.makeText(ctx, ctx.getString(R.string.connexionSilentMessageSuccess), Toast.LENGTH_SHORT).show();
    }
}