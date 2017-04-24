package com.example.first.learnenglishwordssmart.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.classes.CustomViewPager;
import com.example.first.learnenglishwordssmart.classes.MyReceiver;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RewardFragment extends Fragment {

    SharedPreferences sharedPreferences;
    ArrayList<Word> words;
    int number;
    int type;
    int primeType;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reward, container, false);
        final CustomViewPager mPager = ((CardsActivity) getActivity()).mPager;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        type = getArguments().getInt("type");
        words = getActivity().getIntent().getParcelableArrayListExtra("words");
        number = words.size();
        primeType = getActivity().getIntent().getExtras().getInt("prime_type");
        rootView.findViewById(R.id.againButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContainerFragment.changeGlobalTrigger(0);
                for (int i = 0; i < number; i++) {
                    CardsActivity.markList.set(i, 1);
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
                if (type == 12) {
                    for (int i = 0; i < number; i++) {
                        WordsDataBase.setOnLearning(getActivity(), words.get(i).getSpelling(), new Date());
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(
                                getActivity().getString(R.string.on_learning), MainActivity
                                        .getPreference(getActivity(), R.string.on_learning, 0) + 1).apply();
                    }
                }
                if (type == 11) {
                    for (int i = 0; i < number; i++) {
                        CardsActivity.markList.set(i, 1);
                    }
                    mPager.setCurrentItem(number + 1, true);
                } else {
                    if (primeType == 1) {
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                .putInt(getString(R.string.learn_new), 1).apply();
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .edit().putInt(getString(R.string.small_repetition),
                                MainActivity.getPreference(getActivity(),
                                        R.string.small_repetition, 0) + 1).apply();
                    }
                    if (primeType == 2) {
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .edit().putInt(getString(R.string.small_repetition),
                                MainActivity.getPreference(getActivity(),
                                        R.string.small_repetition, 0) + 1).apply();
                    }
                    if (primeType == 3) {
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                .putInt(getString(R.string.big_repetition), 1).apply();
                        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                                .putInt(getString(R.string.small_repetition), 4).apply();
                        for (int i = 0; i < number; i++) {
                            Date wordDate;
                            wordDate = words.get(i).getDate();
                            if (CardsActivity.markList.get(i) == 1) {
                                WordsDataBase.success(getActivity(), words.get(i).getRank(), wordDate);
                            } else
                                WordsDataBase.fail(getActivity(), words.get(i).getRank(), wordDate);
                        }
                    }
                    setNotifications();
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    private void checkLevel() {
        int oldLevel = MainActivity.getPreference(getActivity(), R.string.level, 0);
        int exp = MainActivity.getPreference(getActivity(), R.string.to_next_level, 0);
        if (exp >= MainActivity.toNextLevel(oldLevel + 1)) {
            sharedPreferences.edit().putInt(getString(R.string.level), oldLevel + 1).apply();
            sharedPreferences.edit().putInt(getString(R.string.to_next_level),
                    exp - MainActivity.toNextLevel(oldLevel + 1)).apply();
            ((CardsActivity) getActivity()).pushNewLevel();
        }
    }

    public void setInfo() {
        int oldExp = MainActivity.getPreference(getActivity(), R.string.to_next_level, 0);
        int sum = 0;
        for (int i = 0; i < number; i++) {
            sum += CardsActivity.markList.get(i);
        }
        if ((double) (sum / number) >= 0.4) ((ImageView) rootView.findViewById(R.id.star1))
                .setImageResource((R.drawable.star_filled));
        if ((double) (sum / number) >= 0.6) ((ImageView) rootView.findViewById(R.id.star2))
                .setImageResource((R.drawable.star_filled));
        if ((double) (sum / number) >= 0.8) ((ImageView) rootView.findViewById(R.id.star3))
                .setImageResource((R.drawable.star_filled));
        if (String.valueOf(type).charAt(0) == '1') {
            sharedPreferences.edit().putInt(getString(R.string.to_next_level),
                    number * 3 * sum / number + oldExp).apply();
        } else sharedPreferences.edit().putInt(getString(R.string.to_next_level),
                number * 3 * sum / number / 2 + oldExp).apply();
        ((TextView) rootView.findViewById(R.id.reward)).setText(String.format(getActivity()
                        .getString(R.string.reward),
                MainActivity.getPreference(getActivity(), R.string.to_next_level, 0) - oldExp));
        checkLevel();
    }

    private void setNotifications() {
        for (int i = 1; i < 4; i++) {
            Calendar calendar = Calendar.getInstance();
            if (i != 2) {
                int hours = 0;
                if (i == 1)
                    hours = MainActivity.getPreference(getActivity(), R.string.day_start, 3) + 10;
                if (i == 3)
                    hours = MainActivity.getPreference(getActivity(), R.string.day_start, 3) - 2;
                if (hours == 24 || hours == 0) hours = 23;
                else if (hours > 24) hours -= 24;
                else if (hours < 0) hours += 24;
                calendar.set(Calendar.HOUR_OF_DAY, hours);
            }
            Intent myReceiver = new Intent(getActivity().getApplicationContext(), MyReceiver.class);
            myReceiver.putExtra("prime_type", i);
            PendingIntent pi = PendingIntent.getBroadcast(getActivity(),
                    i, myReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            if (calendar.getTimeInMillis() < System.currentTimeMillis())
                calendar.setTimeInMillis(calendar.getTimeInMillis() + 86400000);
            if (i != 2) am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
            else if (MainActivity.getPreference(getActivity(), R.string.small_repetition, 0) <= 3) {
                int[] time = new int[]{900000, 3600000, 10800000};
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
                        time[MainActivity.getPreference(getActivity(), R.string.small_repetition, 0) - 1], pi);
            }
        }
    }
}
