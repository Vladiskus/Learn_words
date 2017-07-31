package com.example.first.learnenglishwordssmart.activities;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.classes.SoundHelper;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;
import com.example.first.learnenglishwordssmart.services.DownloadDBService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private static final String DB_URL = "https://dl.dropboxusercontent.com/s/byku51pln471yhj/" +
            "words_database.db?dl=0";

    ArrayList<Word> words1;
    ArrayList<Word> words2;
    ArrayList<Word> words3;
    SoundHelper soundHelper;

    boolean isDownloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        soundHelper = new SoundHelper(this);
        //DataBaseFiller.FillWordsDataBase(mContext);
        //addWords();
        isDownloaded = new File(getFilesDir().getAbsolutePath() + "/databases/" +
                WordsDataBase.DATABASE_NAME).exists();
        if (!isDownloaded) {
            DownloadDBService.startDownload(this, DB_URL);
            findViewById(R.id.download_layout).setVisibility(View.VISIBLE);
            ((ProgressBar) findViewById(R.id.progressBar2)).setMax(100);
            ((TextView) findViewById(R.id.downloading)).setText(getString(R.string.downloading, 0));
        }
        final Intent intent = new Intent();
        findViewById(R.id.toGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(MainActivity.this, WordsActivity.class);
                intent.putExtra("prime_type", 0);
                startActivity(intent);
            }
        });
        findViewById(R.id.learnNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("last position", String.valueOf(words1.get(words1.size() - 1).getRank()));
                Log.e("current position", String.valueOf(MainActivity
                        .getPreference(MainActivity.this, R.string.current_position, 1)));
                intent.putExtra("prime_type", 1);
                intent.putExtra("words", words1);
                intent.setClass(MainActivity.this, SelectionActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.smallRepetition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("prime_type", 2);
                intent.putExtra("words", words2);
                intent.setClass(MainActivity.this, CardsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.bigRepetition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("prime_type", 3);
                intent.putExtra("words", words3);
                intent.setClass(MainActivity.this, CardsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isDownloaded) init();
    }

    public void init() {
        words1 = WordsDataBase.getWords(MainActivity.this, 1, null);
        words2 = WordsDataBase.getWords(MainActivity.this, 2, null);
        words3 = WordsDataBase.getWords(MainActivity.this, 3, null);
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
        if (dayStart.getTime() > PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(this.getString(R.string.last_day_start), 0)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putLong(getString(R.string.last_day_start), dayStart.getTime()).apply();
            turnOnButton(findViewById(R.id.smallRepetition));
            turnOnButton(findViewById(R.id.bigRepetition));
            turnOnButton(findViewById(R.id.learnNew));
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(getString(R.string.small_repetition), 0).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(getString(R.string.big_repetition), 0).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(getString(R.string.learn_new), 0).apply();
        }
        setStats();
        if (getPreference(this, R.string.learn_new, 0) == 1)
            turnOffButton(findViewById(R.id.learnNew));
        if (getPreference(this, R.string.small_repetition, 0) == 4 || words2.size() == 0)
            turnOffButton(findViewById(R.id.smallRepetition));
        if (getPreference(this, R.string.big_repetition, 0) == 1 || words3.size() == 0)
            turnOffButton(findViewById(R.id.bigRepetition));
        else
            ((Button) findViewById(R.id.bigRepetition)).setText(String.format(getString(R.string.big_repetition),
                    getNumberString(words3.size(), this)));
    }

    private void setStats() {
        /*PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(getString(R.string.current_position), 4000).apply();
        WordsDataBase.setAreKnown(this, 4000);*/
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(getString(R.string.number_of_words), 10).apply();
        int level = getPreference(this, R.string.level, 0);
        ((TextView) findViewById(R.id.level)).setText(String.format(getString(R.string.level), level));
        ((TextView) findViewById(R.id.toNextLevel)).setText(String.format(getString(R.string.to_next_level),
                toNextLevel(level + 1) - getPreference(this, R.string.to_next_level, 0)));
        ((TextView) findViewById(R.id.vocabulary)).setText(String.format(getString(R.string.vocabulary),
                getNumberString(getPreference(this, R.string.vocabulary, 0), this)));
        ((TextView) findViewById(R.id.learned)).setText(String.format(getString(R.string.learned),
                getPreference(this, R.string.learned, 0)));
        ((TextView) findViewById(R.id.onLearning)).setText(String.format(getString(R.string.on_learning),
                getPreference(this, R.string.on_learning, 0)));
        ((Button) findViewById(R.id.bigRepetition)).setText(String.format(getString(R.string.big_repetition),
                getNumberString(0, this)));
        ((ProgressBar) findViewById(R.id.progressBar))
                .setProgress(getPreference(this, R.string.to_next_level, 0) * 100 / toNextLevel(level + 1));
    }

    public static int toNextLevel(int nextLevel) {
        return (int) (0.25 * nextLevel * nextLevel + 10 * nextLevel + 139.75) / 10 * 10;
    }

    public static String getNumberString(int i, Context context) {
        if (i == 1) return String.format(context.getString(R.string.word), i);
        if (i > 1 && i < 5) return String.format(context.getString(R.string.words), i);
        else return String.format(context.getString(R.string.wordss), i);
    }

    public static int getPreference(Context context, int path, int def) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(path), def);
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

    private void addWords() {
        ArrayList<String> myWords = new ArrayList<>();
        final String[] spellingArray = WordsDataBase.getSpellingArray(this, false);
        myWords.add("savour");
        myWords.add("redemption");
        myWords.add("cripple");
        myWords.add("abyss");
        myWords.add("haunt");
        myWords.add("compel");
        myWords.add("strap");
        myWords.add("subliminal");
        WordsDataBase.addUserWord(this, "play around", "манипулировать",
                new ArrayList<>(Arrays.asList(spellingArray)).contains("play around"));
        WordsDataBase.addUserWord(this, "high-rise", "небоскрёб, высотный",
                new ArrayList<>(Arrays.asList(spellingArray)).contains("high-rise"));
        myWords.add("play around");
        myWords.add("high-rise");
        for (String s : myWords) {
            WordsDataBase.addUserWord(this, s, null, true);
            WordsDataBase.setOnLearning(this, s, new Date(System.currentTimeMillis() - 4 * 86400000));
        }
        myWords.clear();
        myWords.add("determination");
        myWords.add("dominance");
        myWords.add("formidable");
        myWords.add("indulge");
        myWords.add("cast");
        WordsDataBase.addUserWord(this, "devotion", "преданность",
                new ArrayList<>(Arrays.asList(spellingArray)).contains("devotion"));
        myWords.add("devotion");
        myWords.add("glacier");
        myWords.add("repent");
        WordsDataBase.addUserWord(this, "ravage", "опустошать, разорять, грабить",
                new ArrayList<>(Arrays.asList(spellingArray)).contains("ravage"));
        myWords.add("ravage");
        myWords.add("greed");
        myWords.add("agenda");
        WordsDataBase.addUserWord(this, "demise", "гибель, кончина, смерть", true);
        myWords.add("demise");
        for (String s : myWords) {
            WordsDataBase.addUserWord(this, s, null, true);
            WordsDataBase.setOnLearning(this, s, new Date(System.currentTimeMillis() - 3 * 86400000));
        }
        myWords.clear();
        myWords.add("derive");
        myWords.add("infuse");
        myWords.add("corpse");
        myWords.add("rid");
        myWords.add("poise");
        for (String s : myWords) {
            WordsDataBase.addUserWord(this, s, null, true);
            WordsDataBase.setOnLearning(this, s, new Date(System.currentTimeMillis() - 2 * 86400000));
        }
        myWords.clear();
        myWords.add("vocation");
        myWords.add("behold");
        WordsDataBase.addUserWord(this, "fuse", "фитиль, запал", true);
        myWords.add("fuse");
        myWords.add("embed");
        WordsDataBase.addUserWord(this, "jaded", "измученный, пресытившийся", true);
        myWords.add("jaded");
        myWords.add("dispel");
        myWords.add("adrift");
        myWords.add("rift");
        myWords.add("discretion");
        for (String s : myWords) {
            WordsDataBase.addUserWord(this, s, null, true);
            WordsDataBase.setOnLearning(this, s, new Date(System.currentTimeMillis() - 86400000));
        }
        myWords.clear();
        myWords.add("confusion");
        myWords.add("shatters");
        myWords.add("render");
        myWords.add("sever");
        myWords.add("outspoken");
        myWords.add("torment");
        myWords.add("swirl");
        for (String s : myWords) {
            WordsDataBase.addUserWord(this, s, null, true);
            //WordsDataBase.setOnLearning(this, s, new Date(System.currentTimeMillis()));
        }
        myWords.clear();
        myWords.add("loathe");
        myWords.add("deaf");
        myWords.add("dumb");
        myWords.add("limelight");
        myWords.add("discern");
    }
}
