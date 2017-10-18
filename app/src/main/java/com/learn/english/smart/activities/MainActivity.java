package com.learn.english.smart.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.learn.english.smart.R;
import com.learn.english.smart.classes.SoundHelper;
import com.learn.english.smart.classes.Word;
import com.learn.english.smart.providers.WordsHelper;
import com.learn.english.smart.providers.WordsProvider;
import com.learn.english.smart.services.DownloadDBService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://dl.dropboxusercontent.com/s/qsgbqcbkfaih1y7/" +
            "words_database.db?dl=0";

    public static final String EXTRA_PRIME_TYPE = "extra_prime_type";
    public static final String EXTRA_WORDS = "extra_words";
    public static final String GAME = "game";
    public static final String LEARN_NEW = "learn_new";
    public static final String SMALL_REPETITION = "small_repetition";
    public static final String BIG_REPETITION = "big_repetition";

    public static boolean isValid = false;

    private ArrayList<Word> words1;
    private ArrayList<Word> words2;
    private ArrayList<Word> words3;
    private SoundHelper soundHelper;
    public boolean isDownloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DataBaseFiller.FillWordsDataBase(MainActivity.this);
        isDownloaded = new File(getFilesDir().getAbsolutePath() + "/databases/" +
                WordsProvider.DATABASE_NAME).exists();
        if (!isDownloaded) {
            findViewById(R.id.download_layout).setVisibility(View.VISIBLE);
            ((ProgressBar) findViewById(R.id.progressBar2)).setMax(100);
            ((TextView) findViewById(R.id.downloading)).setText(getString(R.string.downloading, 0));
            DownloadDBService.startDownload(this, DB_URL);
        }
        final Intent intent = new Intent();
        findViewById(R.id.toGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(MainActivity.this, WordsActivity.class);
                intent.putExtra(EXTRA_PRIME_TYPE, GAME);
                startActivity(intent);
            }
        });
        findViewById(R.id.learnNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> addedWords = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .getStringSet(getString(R.string.added_words), new HashSet<String>());
                if (words1.get(0).getRank() < getPreference(MainActivity.this, R.string.current_position, 0) ||
                        addedWords.contains(String.valueOf(words1.get(0).getRank()))) {
                    intent.putExtra(EXTRA_WORDS, words1);
                    intent.setClass(MainActivity.this, SelectionActivity.class);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setClass(MainActivity.this, WordsActivity.class);
                }
                intent.putExtra(EXTRA_PRIME_TYPE, LEARN_NEW);
                startActivity(intent);
            }
        });
        findViewById(R.id.smallRepetition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_PRIME_TYPE, SMALL_REPETITION);
                intent.putExtra(EXTRA_WORDS, words2);
                intent.setClass(MainActivity.this, CardsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.bigRepetition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(EXTRA_PRIME_TYPE, BIG_REPETITION);
                intent.putExtra(EXTRA_WORDS, words3);
                intent.setClass(MainActivity.this, CardsActivity.class);
                startActivity(intent);
            }
        });
        if (isValid) words3 = savedInstanceState.getParcelableArrayList("words3");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("words3", words3);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isDownloaded) init();
    }

    public void init() {
        if (soundHelper == null) soundHelper = new SoundHelper(this);
        Calendar calStart = new GregorianCalendar();
        calStart.setTime(new Date());
        int hours = calStart.get(Calendar.HOUR_OF_DAY);
        calStart.set(Calendar.HOUR_OF_DAY, getPreference(this, R.string.day_start, 5));
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        Date dayStart;
        if (hours >= getPreference(this, R.string.day_start, 5)) {
            dayStart = calStart.getTime();
        } else dayStart = new Date(calStart.getTimeInMillis() - 86400000);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (dayStart.getTime() > sharedPreferences.getLong(this.getString(R.string.last_day_start), 0)) {
            if (getPreference(this, R.string.big_repetition, 0) == 0) WordsHelper.missedDay(this);
            else WordsHelper.missedDaysLearned(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(getString(R.string.last_day_start), dayStart.getTime());
            editor.putInt(getString(R.string.small_repetition), 0);
            editor.putInt(getString(R.string.big_repetition), 0);
            editor.putInt(getString(R.string.learn_new), 0);
            editor.commit();
        }
        words1 = WordsHelper.getWords(this, LEARN_NEW, null);
        words2 = WordsHelper.getWords(this, SMALL_REPETITION, null);
        if (!isValid) {
            words3 = WordsHelper.getWords(this, BIG_REPETITION, null);
            isValid = true;
        }
        setStats();
        checkButton(getPreference(this, R.string.vocabulary, 0) == 0 ||
                getPreference(this, R.string.learn_new, 0) == 1, findViewById(R.id.learnNew));
        checkButton(getPreference(this, R.string.small_repetition, 0) == 4 || words2.size() == 0,
                findViewById(R.id.smallRepetition));
        checkButton(getPreference(this, R.string.big_repetition, 0) == 1 || words3.size() == 0,
                findViewById(R.id.bigRepetition));
    }

    private void checkButton(boolean isForbidden, View button) {
        if (isForbidden) button.setBackgroundResource(R.drawable.inactive_button);
        else button.setBackgroundResource(R.drawable.button);
        button.setClickable(!isForbidden);
        if (!isForbidden && button.getId() == R.id.bigRepetition) ((Button) button)
                .setText(String.format(getString(R.string.big_repetition), makeString(words3.size())));
    }

    private void setStats() {
        int level = getPreference(this, R.string.level, 0);
        ((TextView) findViewById(R.id.level)).setText(String.format(getString(R.string.level), level));
        ((TextView) findViewById(R.id.toNextLevel)).setText(String.format(getString(R.string.to_next_level),
                toNextLevel(level + 1) - getPreference(this, R.string.to_next_level, 0)));
        int number = getPreference(this, R.string.vocabulary, 0);
        ((TextView) findViewById(R.id.vocabulary)).setText(String.format(getString(R.string.vocabulary),
                makeString(number)));
        ((TextView) findViewById(R.id.learned)).setText(String.format(getString(R.string.learned),
                getPreference(this, R.string.learned, 0)));
        ((TextView) findViewById(R.id.onLearning)).setText(String.format(getString(R.string.on_learning),
                getPreference(this, R.string.on_learning, 0)));
        ((Button) findViewById(R.id.bigRepetition)).setText(String.format(getString(R.string.big_repetition),
                makeString(0)));
        ((ProgressBar) findViewById(R.id.progressBar)).setProgress(getPreference(this,
                R.string.to_next_level, 0) * 100 / toNextLevel(level + 1));
    }

    public static int toNextLevel(int nextLevel) {
        return (int) (0.25 * nextLevel * nextLevel + 10 * nextLevel + 139.75) / 10 * 10;
    }

    public static String makeString(int i) {
        if (i % 10 == 1 && i % 100 != 11) return i + " слово";
        else if (i % 10 >= 2 && i % 10 <= 4 && (i % 100 < 12 || i % 100 > 14)) return i + " слова";
        else if (i % 10 == 0 || (i % 10 >= 5 && i % 10 <= 9) || (i % 100 >= 11 && i % 100 <= 14))
            return i + " слов";
        else return i + " слова";
    }

    public static int getPreference(Context context, int path, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(path), def);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundHelper.shutdown();
    }
}