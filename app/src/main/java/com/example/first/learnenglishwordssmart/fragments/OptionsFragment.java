package com.example.first.learnenglishwordssmart.fragments;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
                    Collections.shuffle(((CardsActivity) getActivity()).words);
                } else {
                    ((CardsActivity) getActivity()).words = getActivity().getIntent()
                            .getExtras().getParcelableArrayList("words");
                    Log.e("first", getActivity().getIntent()
                            .getExtras().getParcelableArrayList("words").get(0).toString());
                }
                ViewPager pager = ((CardsActivity) getActivity()).mPager;
                int currentItem = pager.getCurrentItem();
                if (!(((CardsActivity) getActivity()).mPagerAdapter.instantiateItem(pager, currentItem)
                        instanceof RewardFragment)) ((CardsActivity) getActivity())
                        .setAdapter(((CardsActivity) getActivity()).primeType);
            }
        });
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putInt(getString(R.string.en_ru), isChecked ? 1 : 0).apply();
                ViewPager pager = ((CardsActivity) getActivity()).mPager;
                int currentItem = pager.getCurrentItem();
                for (int i = -1; i <= 1; i++) {
                    if (currentItem + i >= 0 && currentItem + i < ((CardsActivity) getActivity()).number)
                        ((ContainerFragment) pager.getAdapter().instantiateItem(pager, currentItem + i))
                                .flipCard(currentItem + i, isChecked);
                }
                pager.setCurrentItem(currentItem, false);
            }
        });
        if (((CardsActivity) getActivity()).primeType == 3)
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
