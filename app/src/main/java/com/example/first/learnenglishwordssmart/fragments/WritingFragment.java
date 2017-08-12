package com.example.first.learnenglishwordssmart.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.classes.Word;

import java.util.ArrayList;

import me.grantland.widget.AutofitHelper;

public class WritingFragment extends Fragment {

    TextView wordView;
    ImageButton speaker;
    CardsActivity parentActivity;
    String spelling;
    int number;
    int difference;
    int errors = 0;
    int position;
    static ArrayList<Character> allCharacters = new ArrayList<>();

    static {
        char[] charArray = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        for (char c : charArray) allCharacters.add(c);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_writing, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        wordView = (TextView) rootView.findViewById(R.id.word);
        position = getArguments().getInt("position");
        parentActivity = (CardsActivity) getActivity();
        ArrayList<Word> words = parentActivity.words;
        int position = getArguments().getInt("position");
        spelling = words.get(position).getSpelling();
        AutofitHelper.create(wordView);
        difference = (spelling.length() > 15) ? spelling.length() - 15 : 0;
        number = difference;
        speaker = (ImageButton) rootView.findViewById(R.id.imageSpeakerBig);
        float density = getResources().getDisplayMetrics().density;
        speaker.setBackgroundDrawable(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap
                (BitmapFactory.decodeResource(getResources(), R.drawable.background_pressed),
                        (int) (density * 50), (int) (density * 50), true)));
        speaker.setImageDrawable(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap
                (BitmapFactory.decodeResource(getResources(), R.drawable.speaker_big_pressed),
                        (int) (density * 30), (int) (density * 30), true)));
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                parentActivity.soundHelper.spellIt(parentActivity, spelling, speaker, 2);
            }
        });
        ((TextView) rootView.findViewById(R.id.translation)).setText(words.get(position).getTranslation());
        setWord();
        final ArrayList<Character> array = getArray();
        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 15;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final Button button = new Button(parentActivity);
                button.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                button.setText(array.get(position).toString());
                button.setAllCaps(false);
                button.setTextSize(30);
                button.setBackgroundColor(getResources().getColor(R.color.white));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (number < spelling.length()) {
                            if (array.get(position) == spelling.charAt(number)) {
                                button.setBackgroundColor(parentActivity.getResources().getColor(R.color.blue));
                                number++;
                                setWord();
                            } else {
                                errors++;
                                button.setBackgroundColor(parentActivity.getResources().getColor(R.color.red));
                                ((Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
                            }
                            resetColor(button);
                        }
                    }
                });
                return button;
            }
        });
        rootView.findViewById(R.id.questionView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number < spelling.length()) {
                    errors = errors + 2;
                    number++;
                    setWord();
                    checkWord();
                }
            }
        });
        return rootView;
    }

    private void resetColor(final Button button) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setBackgroundColor(parentActivity.getResources().getColor(R.color.white));
                checkWord();
            }
        }, 300);
    }

    private void checkWord() {
        if (wordView.getText().charAt(wordView.getText().length() - 1) ==
                spelling.charAt(spelling.length() - 1)) {
            if (errors > (spelling.length() - difference) / 2)
                CardsActivity.markList.set(position, 0);
            if (parentActivity.primeType.equals(MainActivity.LEARN_NEW))
                parentActivity.mPager.setCurrentItem(position + 2 + MainActivity
                        .getPreference(parentActivity, R.string.number_of_words, 10), true);
            else parentActivity.mPager.setCurrentItem(position + 1, true);
        }
    }

    private ArrayList<Character> getArray() {
        ArrayList<Character> array = new ArrayList<>();
        int plus = 0;
        for (int i = 0; i < 15; i++) array.add('+');
        for (int i = 0; i < spelling.length() - difference; i++) {
            if (array.contains(spelling.charAt(i))) {
                plus++;
                continue;
            }
            while (true) {
                int position = (int) (Math.random() * 14);
                if (array.get(position) == '+') {
                    array.set(position, spelling.charAt(i));
                    break;
                }
            }
        }
        for (int i = 0; i < 15 - spelling.length() + difference + plus; i++) {
            while (true) {
                char c = allCharacters.get((int) (Math.random() * allCharacters.size()));
                if (array.contains(c)) continue;
                if (array.contains('I') && (c == 'l')) continue;
                if (array.contains(String.valueOf(c).toUpperCase().charAt(0))) continue;
                int position = (int) (Math.random() * 15);
                if (array.get(position) == '+') {
                    array.set(position, c);
                    break;
                }
            }
        }
        return array;
    }

    private void setWord() {
        String word = "";
        for (int i = 0; i < number; i++) {
            word += spelling.charAt(i);
        }
        for (int i = 0; i < spelling.length() - number; i++) {
            word += "_";
        }
        wordView.setText(word);
    }

    public void makeSound(int time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                parentActivity.soundHelper.spellIt(parentActivity, spelling, speaker, 2);
            }
        }, time);
    }
}
