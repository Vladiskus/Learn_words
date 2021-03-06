package com.learn.english.smart.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.learn.english.smart.R;
import com.learn.english.smart.activities.MainActivity;
import com.learn.english.smart.activities.SelectionActivity;


public class ChangeNumberFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_number, container, false);
        final TextView numberView = (TextView) rootView.findViewById(R.id.number);
        int number = MainActivity.getPreference(getActivity(), R.string.number_of_words, 10);
        numberView.setText(MainActivity.makeString(number));
        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setProgress(number);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int number = progress + 1;
                numberView.setText(MainActivity.makeString(number));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        rootView.findViewById(R.id.declineButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        rootView.findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getString(R.string.number_of_words_old),
                        MainActivity.getPreference(getActivity(), R.string.number_of_words, 10)).apply();
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getString(R.string.number_of_words),
                                Integer.parseInt(numberView.getText().toString().replaceAll(" .*", ""))).apply();
                ((SelectionActivity) getActivity()).fillContainer();
                //((SelectionActivity) getActivity()).updateAdapterHard();
                getFragmentManager().popBackStack();
            }
        });
        return rootView;
    }
}