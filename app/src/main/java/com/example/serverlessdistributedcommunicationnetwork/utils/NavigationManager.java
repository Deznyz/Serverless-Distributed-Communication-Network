package com.example.serverlessdistributedcommunicationnetwork.utils;

import android.content.Context;
import android.content.Intent;

public class NavigationManager {

    // Ved at man skal give destinationClass med som et argument, gør klassen mere generisk
    public static void navigateTo(Context context, Class<?> destinationClass) {
        Intent intent = new Intent(context, destinationClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}