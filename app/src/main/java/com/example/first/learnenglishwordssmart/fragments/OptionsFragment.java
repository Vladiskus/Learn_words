package com.example.first.learnenglishwordssmart.fragments;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import java.util.Collections;

public class OptionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_options, container, false);
        final CardsActivity activity = (CardsActivity) getActivity();
        Switch switch1 = ((Switch) rootView.findViewById(R.id.switch1));
        Switch switch2 = ((Switch) rootView.findViewById(R.id.switch2));
        Switch switch3 = ((Switch) rootView.findViewById(R.id.switch3));
        switch1.setChecked(MainActivity.getPreference(getActivity(),
                R.string.audio, 1) == 1);
        switch2.setChecked(MainActivity.getPreference(getActivity(),
                R.string.random, 0) == 1);
        switch3.setChecked(MainActivity.getPreference(getActivity(),
                R.string.en_ru, 1) == 1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putInt(getString(R.string.audio), isChecked ? 1 : 0).apply();
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putInt(getString(R.string.random), isChecked ? 1 : 0).apply();
                if (isChecked) {
                    Collections.shuffle(activity.words);
                } else {
                    activity.words = getActivity().getIntent()
                            .getExtras().getParcelableArrayList(MainActivity.EXTRA_WORDS);
                }
                for (int i = 0; i < activity.number; i++) {
                    ((CardsActivity) getActivity()).markList.set(i, 1);
                }
                ViewPager pager = activity.mPager;
                int currentItem = pager.getCurrentItem();
                if (!(activity.mPagerAdapter.instantiateItem(pager, currentItem)
                        instanceof RewardFragment)) activity.setAdapter();
            }
        });
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putInt(getString(R.string.en_ru), isChecked ? 1 : 0).apply();
                ViewPager pager = activity.mPager;
                int currentItem = pager.getCurrentItem();
                for (int i = -1; i <= 1; i++) {
                    if (currentItem + i >= 0 && currentItem + i < activity.number)
                        ((ContainerFragment) pager.getAdapter().instantiateItem(pager, currentItem + i))
                                .flipCard(currentItem + i);
                }
                pager.setCurrentItem(currentItem, false);
            }
        });
        if (activity.primeType.equals(MainActivity.BIG_REPETITION))
            rootView.findViewById(R.id.transientLayout).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.clickableContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        return rootView;
    }

}
