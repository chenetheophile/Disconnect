package com.example.disconnect.Mail;

import androidx.annotation.NonNull;

import com.example.disconnect.ui.other.TokenException;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

public interface Mail {
    void setAutoReply(String answer, @Nullable String object, @NonNull Long starting_date, @NonNull Long ending_date, boolean active) throws IOException, InterruptedException, TokenException;

    HashMap<String,String> getAutoReply() throws TokenException;

    boolean enableAutoReply(@NonNull Long starting_date, @NonNull Long ending_date,boolean active) throws InterruptedException, IOException,TokenException;

    String toString();
}
