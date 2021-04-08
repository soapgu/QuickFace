package com.soapdemo.quickface.util;

import android.content.Context;
import android.widget.Toast;

public class MessageHelper {
    public static void ShowToast(Context context , String message ){
        Execute.getInstance().BeginOnUIThread(() -> {
            Toast toast = Toast.makeText(context, message , Toast.LENGTH_SHORT);
            toast.show();
        });
    }
}
