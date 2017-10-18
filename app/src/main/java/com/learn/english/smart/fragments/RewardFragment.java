package com.learn.english.smart.fragments;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.learn.english.smart.R;
import com.learn.english.smart.activities.CardsActivity;
import com.learn.english.smart.activities.MainActivity;
import com.learn.english.smart.classes.CustomViewPager;
import com.learn.english.smart.receivers.NotificationsReceiver;
import com.learn.english.smart.classes.Word;
import com.learn.english.smart.providers.WordsHelper;

import java.util.ArrayList;
import java.util.Date;

public class RewardFragment extends Fragment {

    private ArrayList<Word> words;
    private int number;
    private int type;
    private int exp;
    private boolean isLevelUp = false;
    private String primeType;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reward, container, false);
        final CustomViewPager mPager = ((CardsActivity) getActivity()).mPager;
        type = getArguments().getInt(CardsActivity.TYPE);
        words = getActivity().getIntent().getParcelableArrayListExtra(MainActivity.EXTRA_WORDS);
        number = words.size();
        if (savedInstanceState != null) isLevelUp = savedInstanceState.getBoolean("isLevelUp");
        primeType = getActivity().getIntent().getExtras().getString(MainActivity.EXTRA_PRIME_TYPE);
        rootView.findViewById(R.id.againButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContainerFragment.changeGlobalTrigger(0);
                for (int i = 0; i < number; i++) {
                    ((CardsActivity) getActivity()).markList.set(i, 1);
                }
                if (type == 12) {
                    mPager.setCurrentItem(number + 1, true);
                } else {
                    mPager.setCurrentItem(0, true);
                }
            }
        });
        rootView.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 12) for (int i = 0; i < number; i++)
                    WordsHelper.setOnLearning(getActivity(), words.get(i).getSpelling(), new Date());
                if (type == 11) {
                    for (int i = 0; i < number; i++)
                        ((CardsActivity) getActivity()).markList.set(i, 1);
                    mPager.setCurrentItem(number + 1, true);
                } else {
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(getActivity()).edit();
                    switch (primeType) {
                        case MainActivity.LEARN_NEW:
                            editor.putInt(getString(R.string.learn_new), 1)
                                    .putInt(getString(R.string.small_repetition),
                                            MainActivity.getPreference(getActivity(),
                                                    R.string.small_repetition, 0) + 1);
                            MainActivity.isValid = false;
                            break;
                        case MainActivity.SMALL_REPETITION:
                            int type = MainActivity.getPreference(getActivity(),
                                    R.string.small_repetition, 0);
                            editor.putInt(getString(R.string.small_repetition), type + 1);
                            if (type == 3) MainActivity.isValid = false;
                            break;
                        case MainActivity.BIG_REPETITION:
                            editor.putInt(getString(R.string.big_repetition), 1)
                                    .putInt(getString(R.string.small_repetition), 4);
                            MainActivity.isValid = false;
                            for (int i = 0; i < number; i++) {
                                if (((CardsActivity) getActivity()).markList.get(i) == 1)
                                    WordsHelper.success(getActivity(), words.get(i));
                                else WordsHelper.fail(getActivity(), words.get(i));
                            }
                            break;
                    }
                    if (!isLevelUp) editor.putInt(getString(R.string.to_next_level), exp +
                            MainActivity.getPreference(getActivity(), R.string.to_next_level, 0));
                    editor.commit();
                    setNotifications();
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    public void setInfo() {
        double sum = 0;
        for (int i = 0; i < number; i++) sum += ((CardsActivity) getActivity()).markList.get(i);
        ((ImageView) rootView.findViewById(R.id.star1)).setImageResource((R.drawable.star_linear));
        ((ImageView) rootView.findViewById(R.id.star2)).setImageResource((R.drawable.star_linear));
        ((ImageView) rootView.findViewById(R.id.star3)).setImageResource((R.drawable.star_linear));
        if (sum / number >= 0.4) ((ImageView) rootView.findViewById(R.id.star1))
                .setImageResource((R.drawable.star_filled));
        if (sum / number >= 0.6) ((ImageView) rootView.findViewById(R.id.star2))
                .setImageResource((R.drawable.star_filled));
        if (sum / number >= 0.8) ((ImageView) rootView.findViewById(R.id.star3))
                .setImageResource((R.drawable.star_filled));
        exp = (int) (type == 11 ? sum * 2 : sum);
        ((TextView) rootView.findViewById(R.id.reward)).setText(String.format(getActivity()
                        .getString(R.string.reward), exp));
        int oldLevel = MainActivity.getPreference(getActivity(), R.string.level, 0);
        int newExp = exp + MainActivity.getPreference(getActivity(), R.string.to_next_level, 0);
        if (newExp >= MainActivity.toNextLevel(oldLevel + 1)) {
            isLevelUp = true;
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity()).edit();
            editor.putInt(getString(R.string.level), oldLevel + 1);
            editor.putInt(getString(R.string.to_next_level), newExp - MainActivity.toNextLevel(oldLevel + 1));
            editor.apply();
            ((CardsActivity) getActivity()).pushNewLevel();
        }
    }

    private void setNotifications() {
        ((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        int type = MainActivity.getPreference(getActivity(), R.string.small_repetition, 0);
        if (type < 4) {
            int[] time = new int[]{900000, 3600000, 10800000};
            Intent intent = new Intent(getActivity(), NotificationsReceiver.class);
            intent.putExtra(MainActivity.EXTRA_PRIME_TYPE, MainActivity.SMALL_REPETITION);
            intent.putExtra(CardsActivity.TYPE, type);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),
                    1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + time[type - 1], pendingIntent);
        }
        if (primeType.equals(MainActivity.LEARN_NEW) || primeType.equals(MainActivity.BIG_REPETITION)) {
            Intent intent = new Intent(getActivity(), NotificationsReceiver.class);
            intent.putExtra(MainActivity.EXTRA_PRIME_TYPE, primeType);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),
                    1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 79200000, pendingIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isLevelUp", isLevelUp);
        super.onSaveInstanceState(outState);
    }
}
