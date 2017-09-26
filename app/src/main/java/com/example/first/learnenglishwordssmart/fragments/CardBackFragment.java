package com.example.first.learnenglishwordssmart.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.classes.Word;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.grantland.widget.AutofitHelper;

public class CardBackFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_back, container, false);
        ArrayList<Word> words = ((CardsActivity) getActivity()).words;
        int position = getArguments().getInt(CardsActivity.POSITION);
        TextView titleView = (TextView) rootView.findViewById(R.id.translation);
        String title = words.get(position).getTranslation();
        titleView.setText(title);
        titleView.setMaxLines(title.contains(",") ? 2 : 1);
        AutofitHelper.create(titleView);
        if (getArguments().getInt(CardsActivity.TYPE) == 1) {
            final TextView samplesView = (TextView) rootView.findViewById(R.id.samples);
            String spelling = words.get(position).getSpelling();
            String sample = words.get(position).getSamples();
            if (sample == null) {
                titleView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                sample = "";
            }
            Pattern p = Pattern.compile("[^ \\n]*" + spelling + "[^ \\n]*");
            Matcher m = p.matcher(sample);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, "<i>" + m.group() + "</i>");
            }
            m.appendTail(sb);
            sample = "<font color=\"#000000\">" + sb.toString().replaceAll(" — ", "</font><br>")
                    .replaceAll("\\n", "<br><br><font color=\"#000000\">");
            samplesView.setText(Html.fromHtml(sample));
            samplesView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getView() != null) {
                        View parent = getView().findViewById(R.id.scrollView);
                        if (parent.getHeight() != 0 && samplesView.getHeight() > parent.getHeight())
                            samplesView.setText(samplesView.getText().subSequence(0,
                                    String.valueOf(samplesView.getText()).lastIndexOf("\n\n")));
                    }
                }
            });
            rootView.findViewById(R.id.clickableContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ContainerFragment) getParentFragment()).flipCard(getArguments().getInt(CardsActivity.POSITION));
                }
            });
            samplesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ContainerFragment) getParentFragment()).flipCard(getArguments().getInt(CardsActivity.POSITION));
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

}
