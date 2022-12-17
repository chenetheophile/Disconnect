package com.example.disconnect.ui.other;

import androidx.annotation.Nullable;

public class TokenException extends Exception{
    private int code;
    private String message;
    public TokenException(){
        this.code=401;
        this.message="Token Expired";
    }

    public String getCode() {
        return String.valueOf(code);
    }

    @Nullable
    @Override
    public String getMessage() {
        return message;
    }
}
