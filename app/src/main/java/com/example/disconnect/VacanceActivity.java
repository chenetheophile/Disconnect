package com.example.disconnect;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.example.disconnect.ui.main.VacancePagerAdapter;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;


import com.example.disconnect.databinding.ActivityVacanceBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VacanceActivity extends AppCompatActivity {
    private ActivityVacanceBinding binding;
    private ArrayList<Integer> tabName;
    private static final String TAG="ResourceRetrieving";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVacanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tabName= retrieveTabName(this);

        VacancePagerAdapter vacancePagerAdapter = new VacancePagerAdapter(this);
        ViewPager2 viewPager2=binding.vacancePager;

        viewPager2.setAdapter(vacancePagerAdapter);
        TabLayout tabs = binding.tabs;
        new TabLayoutMediator(tabs,viewPager2,
                (tab, position) -> tab.setText(getResources().getString(tabName.get(position)))
        ).attach();
    }
    public ArrayList<Integer> retrieveTabName(Context context){
        ArrayList<Integer> tab= new ArrayList<>();
        for (int i = 1; i < context.getResources().getInteger(R.integer.numTab)+1; i++ ){
            String fname = "tab_" + i;
            tab.add(context.getResources().getIdentifier(fname, "string", context.getPackageName()));
        }
        return tab;
    }
}