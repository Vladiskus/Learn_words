package com.example.first.learnenglishwordssmart.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.activities.SelectionActivity;
import com.example.first.learnenglishwordssmart.providers.WordsHelper;

public class NotificationsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg) {
        String primeType = arg.getExtras().getString(MainActivity.EXTRA_PRIME_TYPE);
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_PRIME_TYPE, primeType);
        intent.putExtra(MainActivity.EXTRA_WORDS, WordsHelper.getWords(context, primeType, null));
        NotificationCompat.Builder mBuilder;
        switch (primeType) {
            case MainActivity.LEARN_NEW:
                if (MainActivity.getPreference(context, R.string.learn_new, 0) == 1) return;
                intent.setClass(context, SelectionActivity.class);
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.web_hi_res_512)
                        .setContentTitle(context.getString(R.string.title1))
                        .setContentText(context.getString(R.string.text1));
                break;
            case MainActivity.SMALL_REPETITION:
                if (MainActivity.getPreference(context, R.string.small_repetition, 0)
                        != arg.getExtras().getInt(CardsActivity.TYPE)) return;
                intent.setClass(context, CardsActivity.class);
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.web_hi_res_512)
                        .setContentTitle(context.getString(R.string.title2))
                        .setContentText(context.getString(R.string.text2));
                break;
            case MainActivity.BIG_REPETITION:
                if (MainActivity.getPreference(context, R.string.big_repetition, 0) == 1) return;
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
        if (WordsHelper.getWords(context, primeType, null).size() != 0) {
            mNotificationManager.notify(1, mBuilder.build());
        }
    }
}
