package com.learn.english.smart.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.learn.english.smart.R;
import com.learn.english.smart.activities.CardsActivity;
import com.learn.english.smart.classes.Word;

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
        int position = getArguments().getInt(CardsActivity.POSITION);
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
        if (getArguments().getInt(CardsActivity.TYPE) == 1) {
            String definitions = words.get(position).getDefinitions();
            if (definitions == null) {
                spellingView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                definitions = "";
            } else definitions = definitions.replaceAll("\\n", "\n\n");
            final TextView definitionsView = ((TextView) rootView.findViewById(R.id.definitions));
            definitionsView.setText(definitions);
            definitionsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getView() != null) {
                        View parent = getView().findViewById(R.id.scrollView);
                        if (parent.getHeight() != 0 && definitionsView.getHeight() > parent.getHeight())
                            definitionsView.setText(definitionsView.getText().subSequence(0,
                                    String.valueOf(definitionsView.getText()).lastIndexOf("\n\n")));
                    }
                }
            });
            rootView.findViewById(R.id.clickableContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ContainerFragment) getParentFragment()).flipCard(getArguments()
                            .getInt(CardsActivity.POSITION));
                }
            });
            definitionsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ContainerFragment) getParentFragment()).flipCard(getArguments()
                            .getInt(CardsActivity.POSITION));
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
