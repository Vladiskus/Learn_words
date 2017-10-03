package com.example.first.learnenglishwordssmart.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.activities.WordsActivity;

public class VocabularyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vocabulary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getIntent().getExtras().getString(MainActivity.EXTRA_PRIME_TYPE).
                equals(MainActivity.GAME)) {
            if (MainActivity.getPreference(getActivity(), R.string.vocabulary, 0) == 0)
                getView().findViewById(R.id.choose).setVisibility(View.VISIBLE);
            else ((WordsActivity) getActivity()).setVocabulary(new View(getActivity()));
        }
        else getView().findViewById(R.id.tip).setVisibility(View.VISIBLE);
    }

    public void gameFinished() {
        getView().findViewById(R.id.tip).setVisibility(View.VISIBLE);
        ((TextView) getView().findViewById(R.id.text)).setText(getString(R.string.game_finished));
        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(VocabularyFragment.this).commit();
                new WordsActivity.EndTask((WordsActivity) getActivity()).execute();
            }
        });
    }
}
