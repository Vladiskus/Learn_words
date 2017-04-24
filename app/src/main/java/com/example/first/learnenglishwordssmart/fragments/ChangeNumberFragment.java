package com.example.first.learnenglishwordssmart.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.activities.SelectionActivity;


public class ChangeNumberFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_number, container, false);
        final TextView numberView = (TextView) rootView.findViewById(R.id.number);
        numberView.setText(MainActivity.getNumberString(
                ((SelectionActivity) getActivity()).number, getActivity()));
        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setProgress(((SelectionActivity) getActivity()).number);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int number = progress + 1;
                numberView.setText(MainActivity.getNumberString(number, getActivity()));
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
        rootView.findViewById(R.id.clickableContainer).setOnClickListener(new View.OnClickListener() {
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