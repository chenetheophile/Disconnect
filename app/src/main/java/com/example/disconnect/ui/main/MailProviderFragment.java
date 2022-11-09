package com.example.disconnect.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.disconnect.R;
import com.example.disconnect.Gmail;
import com.example.disconnect.Outlook;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MailProviderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MailProviderFragment extends Fragment {

    public MailProviderFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MailProviderFragment newInstance() {
        MailProviderFragment fragment = new MailProviderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mail_provider, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        ImageButton outlookButton=view.findViewById(R.id.outlookButton);
        ImageButton gmailButton=view.findViewById(R.id.gmailButton);

        gmailButton.setOnClickListener(v -> {
            Intent gmail=new Intent(view.getContext(), Gmail.class);
            startActivity(gmail);
        });
        outlookButton.setOnClickListener(v -> {
            Intent outlook=new Intent(view.getContext(), Outlook.class);
            startActivity(outlook);
        });
    }
}