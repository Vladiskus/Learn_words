package com.example.first.learnenglishwordssmart.classes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.ImageButton;

import com.example.first.learnenglishwordssmart.R;

import java.util.HashMap;
import java.util.Locale;

public class SoundHelper {

    private TextToSpeech tts;
    private Runnable onDoneRunnable;

    public SoundHelper(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.UK);
            }
        });
    }

    public void shutdown() {
        tts.shutdown();
    }

    public void spellIt(final Activity activity, final String word,
                        final ImageButton speaker, final int type) {
        if (onDoneRunnable != null && tts.isSpeaking()) activity.runOnUiThread(onDoneRunnable);
        final float density = activity.getResources().getDisplayMetrics().density;
        onDoneRunnable = new Runnable() {
            @Override
            public void run() {
                if (type == 0)
                    speaker.setImageResource(R.drawable.speaker_small_default);
                if (type == 1)
                    speaker.setImageResource(R.drawable.speaker_big_default);
                if (type == 2) {
                    speaker.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap
                            (BitmapFactory.decodeResource(activity.getResources(), R.drawable.background_pressed),
                                    (int) (density * 50), (int) (density * 50), true)));
                    speaker.setImageDrawable(new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap
                            (BitmapFactory.decodeResource(activity.getResources(), R.drawable.speaker_big_pressed),
                                    (int) (density * 30), (int) (density * 30), true)));
                }
            }
        };
        final Runnable onStartRunnable = new Runnable() {
            @Override
            public void run() {
                if (type == 0) speaker.setImageResource(R.drawable.speaker_small_pressed);
                if (type == 1) speaker.setImageResource(R.drawable.speaker_big_pressed);
                if (type == 2) {
                    speaker.setBackgroundDrawable(new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap
                            (BitmapFactory.decodeResource(activity.getResources(), R.drawable.background_stroked),
                                    (int) (density * 50), (int) (density * 50), true)));
                    speaker.setImageDrawable(new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap
                            (BitmapFactory.decodeResource(activity.getResources(), R.drawable.speaker_big_default),
                                    (int) (density * 30), (int) (density * 30), true)));
                }
            }
        };
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                activity.runOnUiThread(onStartRunnable);
            }

            @Override
            public void onDone(String utteranceId) {
                activity.runOnUiThread(onDoneRunnable);
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, params, "4312");
        } else {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "4312");
            tts.speak(word, TextToSpeech.QUEUE_ADD, params);
        }
    }
}
