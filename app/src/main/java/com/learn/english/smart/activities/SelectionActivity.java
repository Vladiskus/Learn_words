package com.learn.english.smart.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.learn.english.smart.R;
import com.learn.english.smart.classes.SoundHelper;
import com.learn.english.smart.classes.Word;
import com.learn.english.smart.providers.WordsHelper;
import com.learn.english.smart.fragments.AddWordFragment;
import com.learn.english.smart.fragments.ChangeNumberFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import me.grantland.widget.AutofitHelper;

public class SelectionActivity extends AppCompatActivity {

    private ViewGroup container;
    private Button numberButton;
    private SoundHelper soundHelper;
    private String primeType;
    private ArrayList<Word> words;
    public ArrayList<String> wordsSpelling = new ArrayList<>();
    public ArrayList<String> spellingArray;
    private int number;
    private static boolean isReversed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        AutofitHelper.create((TextView) findViewById(R.id.choosing));
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.selection);
        spellingArray = WordsHelper.getWordsSpelling(this, null, wordsSpelling);
        if (soundHelper == null) soundHelper = new SoundHelper(this);
        number = MainActivity.getPreference(this, R.string.number_of_words, 10);
        primeType = getIntent().getExtras().getString(MainActivity.EXTRA_PRIME_TYPE);
        numberButton = (Button) findViewById(R.id.numberButton);
        numberButton.setText(String.valueOf(number));
        numberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in,
                                android.R.animator.fade_out).add(R.id.fragmentContainer,
                        new ChangeNumberFragment()).addToBackStack(null).commit();
            }
        });
        findViewById(R.id.continueButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isReversed) {
                    Collections.reverse(words);
                    isReversed = true;
                }
                Intent intent = new Intent();
                intent.putExtra(MainActivity.EXTRA_PRIME_TYPE, primeType);
                intent.putExtra(MainActivity.EXTRA_WORDS, words);
                intent.setClass(SelectionActivity.this, CardsActivity.class);
                startActivity(intent);
            }
        });
        container = (ViewGroup) findViewById(R.id.container);
        View endView = View.inflate(this, R.layout.last_position, null);
        endView.findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addClick();
            }
        });
        container.addView(endView, 0);
        fillContainer();
    }

    public void addClick() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in,
                        android.R.animator.fade_out).add(R.id.fragmentContainer,
                new AddWordFragment()).addToBackStack(null).commit();
    }

    public void fillContainer() {
        number = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.number_of_words), 10);
        int oldNumber = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.number_of_words_old), 0);
        numberButton.setText(String.valueOf(number));
        words = WordsHelper.getWords(this, primeType, null);
        isReversed = false;
        for (Word word : words) wordsSpelling.add(word.getSpelling());
        if (number > oldNumber) {
            for (int i = oldNumber; i < words.size(); i++) {
                addWord(words.get(i).getSpelling(), words.get(i).getTranslation(), 0);
            }
        } else {
            for (int i = 0; i < oldNumber - number; i++) {
                container.removeViewAt(0);
            }
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(getString(R.string.number_of_words_old), 0).apply();
    }

    public void addWord(final String spelling, String translation, int position) {
        final View convertView = View.inflate(this, R.layout.list_position, null);
        ((TextView) convertView.findViewById(R.id.spelling)).setText(spelling);
        if (translation.length() > 30) {
            String[] parts = translation.split(", ");
            translation = "";
            boolean isFirst = true;
            for (int i = 0; i < parts.length - 1; i++) {
                if (isFirst) {
                    translation += parts[i];
                    isFirst = false;
                } else translation += ", " + parts[i];
            }
        }
        ((TextView) convertView.findViewById(R.id.translation)).setText(translation);
        final ImageButton speaker = (ImageButton) convertView.findViewById(R.id.imageSpeakerSmall);
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                soundHelper.spellIt(SelectionActivity.this, spelling, speaker, 0);
            }
        });
        convertView.findViewById(R.id.imageCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeView(convertView);
                new RefreshTask(SelectionActivity.this, true).execute(spelling);
            }
        });
        container.addView(convertView, position);
    }

    public static class RefreshTask extends AsyncTask<String, Void, Void> {
        WeakReference<SelectionActivity> weakActivity;
        int last;
        boolean isRemove;

        public RefreshTask(SelectionActivity activity, boolean isRemove) {
            weakActivity = new WeakReference<>(activity);
            this.isRemove = isRemove;
        }

        @Override
        protected Void doInBackground(String... spellings) {
            SelectionActivity activity = weakActivity.get();
            if (activity != null) {
                if (isRemove) WordsHelper.setIsKnown(activity, spellings[0]);
                activity.words = WordsHelper.getWords(activity, activity.primeType, null);
                last = activity.words.size() - 1;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            SelectionActivity activity = weakActivity.get();
            if (activity != null) {
                if (isRemove) activity.addWord(activity.words.get(last).getSpelling(),
                        activity.words.get(last).getTranslation(), 0);
                else activity.addWord(activity.words.get(0).getSpelling(),
                        activity.words.get(0).getTranslation(), last);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundHelper.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selection_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment == null) addClick();
                else if (fragment instanceof AddWordFragment)
                    ((AddWordFragment) fragment).cancel();
                else getSupportFragmentManager().popBackStack();
                return true;
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
