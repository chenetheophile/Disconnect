package com.example.disconnect.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.disconnect.MainActivity;
import com.example.disconnect.R;

public class VacancePagerAdapter extends FragmentStateAdapter {
    private FragmentActivity fragment;

    public VacancePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragment=fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return MailProviderFragment.newInstance();
            case 1:
                return SMSFragment.newInstance();
            default:
                return new PlaceholderFragment();
        }
    }

    @Override
    public int getItemCount() {
        return fragment.getResources().getInteger(R.integer.numTab);
    }
}
