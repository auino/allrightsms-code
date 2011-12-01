package com.allrightsms;

import java.util.regex.Pattern;

import com.allrightsms.shared.SmsChange;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SmsApplication extends Application {

    interface SmsListener {
        void onSmsUpdated(String message, long id);
    }

    private SmsListener listener;
    private SmsAdapter adapter;

    public void setSMSListener(SmsListener listener) {
        this.listener = listener;
    }

    public SmsAdapter getAdapter(Context context) {
        if (adapter == null) {
            adapter = new SmsAdapter(context);
        }

        return adapter;
    }

    public void notifyListener(Intent intent) {
        if (listener != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String message = (String) extras.get("message");
                String[] messages = message.split(Pattern
                        .quote(SmsChange.SEPARATOR));
                listener.onSmsUpdated(messages[0], Long.parseLong(messages[1]));
            }
        }
    }
}
