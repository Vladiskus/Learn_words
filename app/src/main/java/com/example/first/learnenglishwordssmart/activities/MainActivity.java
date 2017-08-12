package com.example.first.learnenglishwordssmart.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.classes.SoundHelper;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.providers.WordsHelper;
import com.example.first.learnenglishwordssmart.providers.WordsProvider;
import com.example.first.learnenglishwordssmart.services.DownloadDBService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://dl.dropboxusercontent.com/s/qsgbqcbkfaih1y7/" +
            "words_database.db?dl=0";

    public static final String EXTRA_PRIME_TYPE = "prime_type";
    public static final String EXTRA_WORDS = "words";
    public static final String GAME = "game";
    public static final String LEARN_NEW = "learnNew";
    public static final String SMALL_REPETITION = "small_repetition";
    public static final String BIG_REPETITION = "big_repetition";

    private ArrayList<Word> words1;
    private ArrayList<Word> words2;
    private ArrayList<Word> words3;
    private SoundHelper soundHelper;
    private boolean isDownloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        soundHelper = new SoundHelper(this);
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
                if (words1.get(0).getRank() < getPreference(MainActivity.this, R.string.current_position, 0)) {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDownloaded) init();
    }

    public void init() {
        words1 = WordsHelper.getWords(MainActivity.this, LEARN_NEW, null);
        words2 = WordsHelper.getWords(MainActivity.this, SMALL_REPETITION, null);
        words3 = WordsHelper.getWords(MainActivity.this, BIG_REPETITION, null);
        Calendar calStart = new GregorianCalendar();
        calStart.setTime(new Date());
        int hours = calStart.get(Calendar.HOUR_OF_DAY);
        calStart.set(Calendar.HOUR_OF_DAY, getPreference(this, R.string.day_start, 0));
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        Date dayStart;
        if (hours >= getPreference(this, R.string.day_start, 2))
            dayStart = calStart.getTime();
        else dayStart = new Date(calStart.getTimeInMillis() - 86400000);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (dayStart.getTime() > sharedPreferences.getLong(this.getString(R.string.last_day_start), 0)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(getString(R.string.last_day_start), dayStart.getTime());
            editor.putInt(getString(R.string.small_repetition), 0);
            editor.putInt(getString(R.string.big_repetition), 0);
            editor.putInt(getString(R.string.learn_new), 0);
            editor.apply();
            turnOnButton(findViewById(R.id.smallRepetition));
            turnOnButton(findViewById(R.id.bigRepetition));
            turnOnButton(findViewById(R.id.learnNew));
        }
        setStats();
        if (getPreference(this, R.string.vocabulary, 0) == 0)
            turnOffButton(findViewById(R.id.learnNew));
        else turnOnButton(findViewById(R.id.learnNew));
        if (getPreference(this, R.string.learn_new, 0) == 1)
            turnOffButton(findViewById(R.id.learnNew));
        if (getPreference(this, R.string.small_repetition, 0) == 4 || words2.size() == 0)
            turnOffButton(findViewById(R.id.smallRepetition));
        if (getPreference(this, R.string.big_repetition, 0) == 1 || words3.size() == 0)
            turnOffButton(findViewById(R.id.bigRepetition));
        else ((Button) findViewById(R.id.bigRepetition))
                .setText(String.format(getString(R.string.big_repetition), getResources().getQuantityString(R.plurals.words, words3.size(), words3.size())));
    }

    private void setStats() {
        int level = getPreference(this, R.string.level, 0);
        ((TextView) findViewById(R.id.level)).setText(String.format(getString(R.string.level), level));
        ((TextView) findViewById(R.id.toNextLevel)).setText(String.format(getString(R.string.to_next_level),
                toNextLevel(level + 1) - getPreference(this, R.string.to_next_level, 0)));
        int number = getPreference(this, R.string.vocabulary, 0);
        ((TextView) findViewById(R.id.vocabulary)).setText(String.format(getString(R.string.vocabulary),
                getResources().getQuantityString(R.plurals.words, number, number)));
        ((TextView) findViewById(R.id.learned)).setText(String.format(getString(R.string.learned),
                getPreference(this, R.string.learned, 0)));
        ((TextView) findViewById(R.id.onLearning)).setText(String.format(getString(R.string.on_learning),
                getPreference(this, R.string.on_learning, 0)));
        ((Button) findViewById(R.id.bigRepetition)).setText(String.format(getString(R.string.big_repetition),
                getResources().getQuantityString(R.plurals.words, 0, 0)));
        ((ProgressBar) findViewById(R.id.progressBar))
                .setProgress(getPreference(this, R.string.to_next_level, 0) * 100 / toNextLevel(level + 1));
    }

    public static int toNextLevel(int nextLevel) {
        return (int) (0.25 * nextLevel * nextLevel + 10 * nextLevel + 139.75) / 10 * 10;
    }

    public static int getPreference(Context context, int path, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(path), def);
    }

    private void turnOffButton(View view) {
        view.setClickable(false);
        view.setBackgroundResource(R.drawable.inactive_button);
    }

    private void turnOnButton(View view) {
        if (view.getId() != R.id.smallRepetition) {
            view.setClickable(true);
            view.setBackgroundResource(R.drawable.button);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundHelper.shutdown();
    }
}
