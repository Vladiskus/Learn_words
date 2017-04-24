package com.example.first.learnenglishwordssmart.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v4.os.AsyncTaskCompat;
import android.widget.ImageButton;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.fragments.ContainerFragment;

import java.util.Locale;

/**
 * Created by Vlad on 07-Feb-17.
 */

public class SoundHelper {

    private static TextToSpeech tts;
    private float density;
    private int type;
    private Context mContext;

    public void spellIt(final Context mContext, final String string,
                        final ImageButton speaker, final int type) {
        this.type = type;
        this.mContext = mContext;
        density = mContext.getResources().getDisplayMetrics().density;
        //if (tts != null) tts.stop();
        if (type == 0) speaker.setImageResource(R.drawable.speaker_small_pressed);
        if (type == 1) speaker.setImageResource(R.drawable.speaker_big_pressed);
        if (type == 2) {
            speaker.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap
                    (BitmapFactory.decodeResource(mContext.getResources(),R.drawable.background_stroked),
                            (int) (density * 50), (int) (density * 50), true)));
            speaker.setImageDrawable(new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap
                    (BitmapFactory.decodeResource(mContext.getResources(),R.drawable.speaker_big_default),
                            (int) (density * 30), (int) (density * 30), true)));
        }
        tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.UK);
                tts.speak(string, TextToSpeech.QUEUE_ADD, null);
                new SoundTask().execute(speaker);
            }
        });
    }

    private class SoundTask extends AsyncTask<ImageButton, Void, ImageButton> {
        @Override
        protected ImageButton doInBackground(ImageButton... views) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
            while (tts.isSpeaking()) {}
            return views[0];
        }

        @Override
        protected void onPostExecute(ImageButton speaker) {
            if (type == 0) speaker.setImageResource(R.drawable.speaker_small_default);
            if (type == 1) speaker.setImageResource(R.drawable.speaker_big_default);
            if (type == 2) {
                speaker.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap
                        (BitmapFactory.decodeResource(mContext.getResources(),R.drawable.background_pressed),
                                (int) (density * 50), (int) (density * 50), true)));
                speaker.setImageDrawable(new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap
                        (BitmapFactory.decodeResource(mContext.getResources(),R.drawable.speaker_big_pressed),
                                (int) (density * 30), (int) (density * 30), true)));
                tts.stop();
            }
        }
    }
}
