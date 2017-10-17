package com.example.first.learnenglishwordssmart.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.SelectionActivity;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.providers.WordsHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class AddWordFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_word, container, false);
        final AutoCompleteTextView engView = (AutoCompleteTextView) rootView.findViewById(R.id.engWord);
        final TextView rusView = (TextView) rootView.findViewById(R.id.rusWord);
        final ArrayList<String> spellingArray = WordsHelper.getWordsSpelling(getActivity(), null,
                ((SelectionActivity) getActivity()).wordsSpelling);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, spellingArray);
        engView.setAdapter(adapter);
        engView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word word = WordsHelper.getWordFromDataBase(getActivity(), engView.getText().toString());
                rusView.setText(word.getTranslation());
            }
        });
        rootView.findViewById(R.id.declineButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        rootView.findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engView.getText().toString().length() != 0 && rusView.getText().toString().length() != 0) {
                    WordsHelper.addUserWord(getActivity(), engView.getText().toString().trim(),
                            rusView.getText().toString().trim(), spellingArray
                                    .contains(engView.getText().toString().trim()));
                    ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.container);
                    container.removeViewAt(0);
                    int lastPosition = container.getChildCount() - 1;
                    ((SelectionActivity) getActivity()).addWord(engView.getText().toString(),
                            rusView.getText().toString(), lastPosition);
                }
                cancel();
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        View engView = getView().findViewById(R.id.engWord);
        engView.setFocusableInTouchMode(true);
        engView.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(engView, InputMethodManager.SHOW_IMPLICIT);
        super.onStart();
    }

    public void cancel() {
        View focusView = getActivity().getCurrentFocus();
        if (focusView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
