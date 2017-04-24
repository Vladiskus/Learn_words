package com.example.first.learnenglishwordssmart.classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.activities.SelectionActivity;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;

/**
 * Created by Vlad on 18-Mar-17.
 */

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent arg) {
        showNotification(context, arg);
    }

    private void showNotification(Context context, Intent arg) {
        int primeType = arg.getExtras().getInt("prime_type");
        Intent intent = new Intent();
        intent.putExtra("prime_type", primeType);
        intent.putExtra("words", WordsDataBase.getWords(context, primeType, null));
        NotificationCompat.Builder mBuilder;
        switch (primeType) {
            case 1:
                intent.setClass(context, SelectionActivity.class);
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.web_hi_res_512)
                        .setContentTitle(context.getString(R.string.title1))
                        .setContentText(context.getString(R.string.text1));
                break;
            case 2:
                intent.setClass(context, CardsActivity.class);
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.web_hi_res_512)
                        .setContentTitle(context.getString(R.string.title2))
                        .setContentText(context.getString(R.string.text2));
                break;
            case 3:
                intent.setClass(context, CardsActivity.class);
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.web_hi_res_512)
                        .setContentTitle(context.getString(R.string.title3))
                        .setContentText(context.getString(R.string.text3));
                break;
            default:
                mBuilder = new NotificationCompat.Builder(context);
                break;
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (WordsDataBase.getWords(context, primeType, null).size() != 0) {
            mNotificationManager.notify(1, mBuilder.build());
        }
    }
}
