package com.example.disconnect.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.disconnect.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SMSFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SMSFragment extends Fragment {
    private static final String TAG="SMSFragment";
    public static boolean isSmsActive=false;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public SMSFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SMSFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SMSFragment newInstance() {
        SMSFragment fragment = new SMSFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        SharedPreferences settings = view.getContext().getSharedPreferences("Disconnect", Context.MODE_PRIVATE);
        String messageDefaut=getResources().getString(R.string.reponseAutomatiqueDefault);

        TextView actualMessage=view.findViewById(R.id.actualMessageTextView);
        actualMessage.setMovementMethod(new ScrollingMovementMethod());
        actualMessage.setText(settings.getString("SMS_Text",messageDefaut));
        Switch smsActive=view.findViewById(R.id.smsSwitch);
        smsActive.setOnClickListener(v -> {
            isSmsActive=!isSmsActive;
            Log.i(TAG,"IsSMSActive"+isSmsActive);
        });
        EditText txt=view.findViewById(R.id.reponseEditText);
        Button sauver=view.findViewById(R.id.sauvegarderButton);
        sauver.setOnClickListener(v -> {
            Log.i(TAG,txt.getText().toString().length()+"");
            if(txt.getText().toString().length()>0) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("SMS_Text", txt.getText().toString());
                editor.apply();
                actualMessage.setText(settings.getString("SMS_Text", messageDefaut));
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms, container, false);
    }
}