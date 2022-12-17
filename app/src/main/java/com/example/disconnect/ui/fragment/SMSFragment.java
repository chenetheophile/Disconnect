package com.example.disconnect.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.disconnect.R;
import com.example.disconnect.ui.activity.VacanceActivity;

public class SMSFragment extends Fragment {
    private static final String TAG="SMSFragment";
    public static boolean isSmsActive=false;
    Switch smsActive;
    TextView actualMessage;
    String messageDefaut;
    public SMSFragment() {
        // Required empty public constructor
    }

    public static SMSFragment newInstance() {
        return new SMSFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        messageDefaut=getResources().getString(R.string.reponseAutomatiqueDefault);

        actualMessage=view.findViewById(R.id.actualMessageTextView);
        actualMessage.setMovementMethod(new ScrollingMovementMethod());

        smsActive=view.findViewById(R.id.smsSwitch);
        smsActive.setOnClickListener(v -> {
            isSmsActive=!isSmsActive;
            Log.i(TAG,"IsSMSActive"+isSmsActive);
        });
        EditText txt=view.findViewById(R.id.reponseEditText);
        Button sauver=view.findViewById(R.id.sauvegarderButton);
        sauver.setOnClickListener(v -> {
            Log.i(TAG,txt.getText().toString().length()+"");
            if(txt.getText().toString().length()<=0)
                return;
            SharedPreferences.Editor editor = VacanceActivity.preferences.edit();
            editor.putString("SMS_Text", txt.getText().toString());
            editor.apply();
        });
        updateUI();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sms, container, false);
    }
    public void updateUI(){
        actualMessage.setText(VacanceActivity.preferences.getString("SMS_Text",messageDefaut));
        smsActive.setChecked(isSmsActive);

    }
}