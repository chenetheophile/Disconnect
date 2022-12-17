package com.example.disconnect.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.disconnect.Mail.Gmail;
import com.example.disconnect.Mail.Outlook;
import com.example.disconnect.R;
import com.example.disconnect.databinding.ActivityVacanceBinding;
import com.example.disconnect.ui.fragment.MailProviderFragment;
import com.example.disconnect.ui.fragment.SMSFragment;
import com.example.disconnect.ui.other.VacancePagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Date;

public class VacanceActivity extends AppCompatActivity {
    private ActivityVacanceBinding binding;
    private ArrayList<Integer> tabName;
    private ArrayList<Switch> listSwitch=new ArrayList<>();
    public static VacanceActivity instance;
    private static final String TAG = "VacanceActivity";
    public static SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        binding = ActivityVacanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tabName = retrieveTabName(this);
        ImageButton desactiveModeVacance = findViewById(R.id.desactiveModeVacance);
        VacancePagerAdapter vacancePagerAdapter = new VacancePagerAdapter(this);
        ViewPager2 viewPager2 = binding.vacancePager;
        viewPager2.setAdapter(vacancePagerAdapter);

        TabLayout tabs = binding.tabs;
        new TabLayoutMediator(tabs, viewPager2,
                (tab, position) -> {
                    if (position == 0)
                        listSwitch.add(tab.view.findViewById(R.id.mailSwitch));
                    if (position == 1)
                        listSwitch.add(tab.view.findViewById(R.id.smsSwitch));
                    tab.setText(getResources().getString(tabName.get(position)));
                }
        ).attach();
        viewPager2.setOffscreenPageLimit(3);
        desactiveModeVacance.setOnClickListener(v -> {
            ArrayList<Fragment> list=vacancePagerAdapter.getFragments();
            MailProviderFragment mailProviderFragment= (MailProviderFragment) list.get(0);
            SMSFragment smsFragment=(SMSFragment) list.get(1);
            Switch emailSwitch = mailProviderFragment.getView().findViewById(R.id.mailSwitch);
            Switch smsSwitch = smsFragment.getView().findViewById(R.id.smsSwitch);
            Log.i(TAG, String.valueOf(emailSwitch.isChecked()));
            Log.i(TAG, String.valueOf(smsSwitch.isChecked()));
            if (emailSwitch.isChecked()) {
                Switch switchGmail=mailProviderFragment.getView().findViewById(R.id.switchGmail);
                Switch switchOutlook=mailProviderFragment.getView().findViewById(R.id.switchOutlook);
                Log.i(TAG, "deactivating mail");
                if (switchGmail.isChecked()) {
                    mailProviderFragment.deactivateMail(Gmail.class.getSimpleName());
                }
                if (switchOutlook.isChecked()) {
                    mailProviderFragment.deactivateMail(Outlook.class.getSimpleName());
                }
            }
            if (smsSwitch.isChecked())
                smsSwitch.callOnClick();
            mailProviderFragment.updateUI();
            smsFragment.updateUI();
            finish();
        });
    }

    public ArrayList<Integer> retrieveTabName(Context context) {
        ArrayList<Integer> tab = new ArrayList<>();
        for (int i = 1; i < context.getResources().getInteger(R.integer.numTab) + 1; i++) {
            String fname = "tab_" + i;
            tab.add(context.getResources().getIdentifier(fname, "string", context.getPackageName()));
        }
        return tab;
    }

    public void updatePreference(){
        Log.d(TAG, "updatePreference: Preference updated");
        preferences=getApplicationContext().getSharedPreferences("Disconnect",MODE_PRIVATE);
        Log.d(TAG, String.valueOf(new Date(System.currentTimeMillis())));
        Log.d(TAG,String.valueOf(new Date(preferences.getLong("last_update",0))));
    }
}