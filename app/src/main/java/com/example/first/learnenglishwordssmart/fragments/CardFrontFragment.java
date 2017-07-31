package com.example.first.learnenglishwordssmart.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.classes.SoundHelper;
import com.example.first.learnenglishwordssmart.classes.Word;

import java.util.ArrayList;

public class CardFrontFragment extends Fragment {

    String spelling;
    ImageButton speaker;
    CardsActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_front, container, false);
        parentActivity = ((CardsActivity) getActivity());
        ArrayList<Word> words = parentActivity.words;
        int position = getArguments().getInt("position");
        speaker = (ImageButton) rootView.findViewById(R.id.imageSpeakerBig);
        spelling = words.get(position).getSpelling();
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                parentActivity.soundHelper.spellIt(parentActivity, spelling, speaker, 1);
            }
        });
        TextView spellingView = (TextView) rootView.findViewById(R.id.spelling);
        spellingView.setText(spelling);
        String definitions = words.get(position).getDefinitions();
        if (definitions == null) {
            spellingView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            definitions = "";
        } else definitions = definitions.replaceAll("\\n", "\n\n");
        ((TextView) rootView.findViewById(R.id.definitions)).setText(definitions);
        if (getArguments().getInt("type") == 1) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 0, 60);
            rootView.findViewById(R.id.margin50).setLayoutParams(lp);
            rootView.findViewById(R.id.clickableContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ContainerFragment) getParentFragment()).flipCard(getArguments().getInt("position"), false);
                }
            });
            rootView.findViewById(R.id.definitions).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ContainerFragment) getParentFragment()).flipCard(getArguments().getInt("position"), false);
                }
            });
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setCameraDistance(getResources().getDisplayMetrics().density * 10000);
    }

    public void makeSound() {
        parentActivity.soundHelper.spellIt(parentActivity, spelling, speaker, 1);
    }
}
