package com.learn.english.smart.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.learn.english.smart.R;

public class NewLevelFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_level, container, false);
        ((TextView) rootView.findViewById(R.id.level)).setText(String.format(getActivity()
                        .getString(R.string.level),
                PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(R.string.level), 0)));
        rootView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        return rootView;
    }

}
