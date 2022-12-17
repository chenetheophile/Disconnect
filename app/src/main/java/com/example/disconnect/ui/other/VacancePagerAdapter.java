package com.example.disconnect.ui.other;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.disconnect.R;
import com.example.disconnect.ui.fragment.MailProviderFragment;
import com.example.disconnect.ui.fragment.PlaceholderFragment;
import com.example.disconnect.ui.fragment.SMSFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class VacancePagerAdapter extends FragmentStateAdapter {
    private FragmentActivity fragment;
    private Fragment mailProvider;
    private Fragment smsFragment;
    public VacancePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragment=fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                mailProvider=MailProviderFragment.newInstance();
                return mailProvider;
            case 1:
                smsFragment=SMSFragment.newInstance();
                return smsFragment;
            default:
                return new PlaceholderFragment();
        }
    }

    @Override
    public int getItemCount() {
        return fragment.getResources().getInteger(R.integer.numTab);
    }
    public ArrayList<Fragment> getFragments(){
        return new ArrayList<>(Arrays.asList(mailProvider,smsFragment));
    }
}
