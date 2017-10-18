package com.learn.english.smart.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.learn.english.smart.R;
import com.learn.english.smart.providers.WordsHelper;
import com.learn.english.smart.fragments.VocabularyFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import me.grantland.widget.AutofitHelper;

public class WordsActivity extends AppCompatActivity {

    private ArrayList<String> words;
    private RelativeLayout mRelativeLayout;
    private ArrayList<Integer> assessments = new ArrayList<>();
    private ArrayList<Integer> primeRanks = new ArrayList<>();
    private ArrayList<String> knownWords = new ArrayList<>();
    private ArrayList<ObjectAnimator> animators = new ArrayList<>();
    private ArrayList<Runnable> fallRunnables = new ArrayList<>();
    private ArrayList<Runnable> removeRunnables = new ArrayList<>();
    private Handler fallHandler = new Handler();
    private Handler removeHandler = new Handler();
    private String primeType;
    private int vocabulary;
    private int oldVocabulary;
    private int count = 0;
    private int length = 100;
    private int duration;
    private int multiplier = 1;
    private double realRatio = 1;
    private int backStepCount = 1;
    private double requiredRatio;
    private boolean isPaused = false;
    private transient boolean isForbidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
        AutofitHelper.create((TextView) findViewById(R.id.tip));
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ProgressBar) findViewById(R.id.progressBar)).setMax(10000);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.multipliers, R.layout.spinner_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int position = MainActivity.getPreference(this, R.string.speed, 1);
        spinner.setSelection(position);
        setDuration(position);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setDuration(position);
                PreferenceManager.getDefaultSharedPreferences(WordsActivity.this).edit()
                        .putInt(getString(R.string.speed), position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        oldVocabulary = MainActivity.getPreference(this, R.string.vocabulary, 0);
        primeType = getIntent().getExtras().getString(MainActivity.EXTRA_PRIME_TYPE);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        if (primeType.equals(MainActivity.GAME))
            getSupportActionBar().setTitle(R.string.game_title0);
        else getSupportActionBar().setTitle(R.string.game_title1);
        if (savedInstanceState == null) {
            isForbidden = true;
            new LoadTask(this).execute();
        } else {
            words = savedInstanceState.getStringArrayList("words");
            knownWords = savedInstanceState.getStringArrayList("knownWords");
            assessments = savedInstanceState.getIntegerArrayList("assessments");
            primeRanks = savedInstanceState.getIntegerArrayList("primeRanks");
            count = savedInstanceState.getInt("count");
            multiplier = savedInstanceState.getInt("multiplier");
            backStepCount = savedInstanceState.getInt("backStepCount");
            realRatio = savedInstanceState.getDouble("realRatio");
            requiredRatio = savedInstanceState.getDouble("requiredRatio");
            isPaused = savedInstanceState.getBoolean("isPaused");
            isForbidden = savedInstanceState.getBoolean("isForbidden");
            invalidateOptionsMenu();
        }
        if (savedInstanceState == null) getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new VocabularyFragment(), "fragment").commit();
        else if (!isPaused) ready();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("words", words);
        outState.putStringArrayList("knownWords", knownWords);
        outState.putIntegerArrayList("assessments", assessments);
        outState.putIntegerArrayList("primeRanks", primeRanks);
        outState.putInt("count", count);
        outState.putInt("multiplier", multiplier);
        outState.putInt("backStepCount", backStepCount);
        outState.putDouble("realRatio", realRatio);
        outState.putDouble("requiredRatio", requiredRatio);
        outState.putBoolean("isPaused", isPaused);
        outState.putBoolean("isForbidden", isForbidden);
        super.onSaveInstanceState(outState);
    }

    public void setVocabulary(View view) {
        multiplier = 20;
        switch (view.getId()) {
            case R.id.beginner:
                vocabulary = 500;
                break;
            case R.id.elementary:
                vocabulary = 1000;
                break;
            case R.id.pre_intermediate:
                vocabulary = 1500;
                break;
            case R.id.intermediate:
                vocabulary = 2000;
                break;
            case R.id.upper_intermediate:
                vocabulary = 3000;
                break;
            case R.id.advanced:
                vocabulary = 4000;
                break;
            case R.id.proficient:
                vocabulary = 6000;
                break;
            default:
                vocabulary = 0;
                break;
        }
        count = (vocabulary > length * multiplier) ? vocabulary - length * multiplier : 0;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("fragment");
        fragment.getView().findViewById(R.id.choose).setVisibility(View.GONE);
        fragment.getView().findViewById(R.id.tip).setVisibility(View.VISIBLE);
    }

    private void setDuration(int position) {
        duration = (int) (4000 / (1 + 0.2 * (1 - position)));
    }

    private void startFalling() {
        final Button view = new Button(this);
        int diameter = (int) (100 * getResources().getDisplayMetrics().density);
        int marginLeft = (int) ((getResources().getDisplayMetrics().widthPixels
                - diameter) * Math.random());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(diameter, diameter);
        params.setMargins(marginLeft, 0, 0, 0);
        mRelativeLayout.addView(view, params);
        view.setBackgroundResource(R.drawable.round_button);
        view.setTextColor(getResources().getColor(R.color.button_colors));
        view.setAllCaps(false);
        view.setMaxLines(1);
        view.setTextSize(30);
        view.setText(words.get(count + multiplier));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.INVISIBLE);
            }
        });
        AutofitHelper.create(view);
        count += multiplier;
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "y",
                getResources().getDisplayMetrics().heightPixels);
        objectAnimator.setDuration(duration);
        objectAnimator.start();
        animators.add(objectAnimator);
        remove(view);
    }

    private static class LoadTask extends AsyncTask<Void, Void, Void> {
        WeakReference<WordsActivity> weakActivity;

        LoadTask(WordsActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... Voids) {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                activity.requiredRatio = 1 - (double) MainActivity
                        .getPreference(activity, R.string.number_of_words, 10) / 50;
                activity.words = WordsHelper.getWordsSpelling(activity, (activity.primeType
                        .equals(MainActivity.GAME)) ? null : String.valueOf((int) (4 * 10 * MainActivity
                        .getPreference(activity, R.string.number_of_words, 10) / (1 - activity.requiredRatio))), null);
                if (activity.oldVocabulary > 12500)
                    PreferenceManager.getDefaultSharedPreferences(activity).edit()
                            .putInt(activity.getString(R.string.vocabulary),
                                    MainActivity.getPreference(activity, R.string.current_position,
                                            1) - MainActivity
                                            .getPreference(activity, R.string.on_learning, 0)).apply();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                if (!activity.isForbidden) {
                    activity.ready();
                } else activity.isForbidden = false;
            }
        }
    }

    private void fall() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    startFalling();
                } catch (IndexOutOfBoundsException e) {
                    if (primeType.equals(MainActivity.LEARN_NEW)) gameFinished();
                    else new EndTask(WordsActivity.this).execute();
                    return;
                }
                if (primeType.equals(MainActivity.GAME)) {
                    if (assessments.size() == length && realRatio < requiredRatio
                            && vocabulary > backStepCount * length * multiplier) {
                        backStepCount++;
                        count = (vocabulary > backStepCount * length * multiplier) ?
                                vocabulary - backStepCount * length * multiplier : 0;
                        assessments.clear();
                    }
                    if (realRatio > 0.5 || assessments.size() < length) fall();
                    else new EndTask(WordsActivity.this).execute();
                } else {
                    if (assessments.size() - knownWords.size() < 8 *
                            MainActivity.getPreference(WordsActivity.this, R.string.number_of_words, 10))
                        fall();
                    else new EndTask(WordsActivity.this).execute();
                }
                fallRunnables.remove(this);
            }
        };
        fallRunnables.add(runnable);
        fallHandler.postDelayed(runnable, duration / 5);
    }

    private void remove(final View view) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (view.getVisibility() == View.VISIBLE) {
                    knownWords.add(((Button) view).getText().toString());
                    assessments.add(1);
                } else assessments.add(0);
                double sum = 0;
                for (int i = (assessments.size() >= length) ? assessments.size() - length : 0; i < assessments.size(); i++) {
                    sum += assessments.get(i);
                }
                realRatio = sum / ((assessments.size() >= length) ? length : assessments.size());
                if (primeType.equals(MainActivity.GAME)) {
                    double a = realRatio >= 0.5 ? 20000 * (1 - realRatio) : 20000 * realRatio;
                    double b = 100 * assessments.size();
                    double c = realRatio >= 0.5 ? a : b + (a - b) / 2 * (1 - (double) assessments.size() / length);
                    animate((int) (assessments.size() > length ? c : c * (double) assessments.size() / length));
                } else animate(10000 * (assessments.size() - knownWords.size()) / 8 /
                        MainActivity.getPreference(WordsActivity.this, R.string.number_of_words, 10));
                if (assessments.size() >= length && realRatio == requiredRatio)
                    primeRanks.add(count - (int) (length * (1 - realRatio)) * multiplier);
                mRelativeLayout.removeView(view);
                animators.remove(0);
                removeRunnables.remove(this);
            }
        };
        removeRunnables.add(runnable);
        removeHandler.postDelayed(runnable, duration);
    }

    private void animate(int progress) {
        ProgressBar progressBar = ((ProgressBar) findViewById(R.id.progressBar));
        ObjectAnimator animation;
        animation = ObjectAnimator.ofInt(progressBar, "progress", progress);
        animation.setDuration(700);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
    }

    public void gameFinished() {
        VocabularyFragment vocabularyFragment = new VocabularyFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, vocabularyFragment, "fragment").commit();
        getSupportFragmentManager().executePendingTransactions();
        removeAll(this);
        vocabularyFragment.gameFinished();
    }

    private static void removeAll(WordsActivity activity) {
        for (ObjectAnimator objectAnimator : activity.animators)
            activity.mRelativeLayout.removeView((View) objectAnimator.getTarget());
        for (Runnable runnable : activity.removeRunnables)
            activity.removeHandler.removeCallbacks(runnable);
        for (Runnable runnable : activity.fallRunnables)
            activity.fallHandler.removeCallbacks(runnable);
        activity.isForbidden = true;
    }

    public static class EndTask extends AsyncTask<Void, Void, Void> {
        WeakReference<WordsActivity> weakActivity;

        public EndTask(WordsActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                activity.animate(10000);
                removeAll(activity);
                activity.findViewById(R.id.roundProgressBar).setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... Voids) {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                int rank;
                if (activity.primeType.equals(MainActivity.GAME)) {
                    activity.vocabulary = activity.count - (int) (activity.length * (1 - activity.realRatio))
                            * activity.multiplier + MainActivity
                            .getPreference(activity, R.string.current_position, 1) - MainActivity
                            .getPreference(activity, R.string.on_learning, 0);
                    PreferenceManager.getDefaultSharedPreferences(activity)
                            .edit().putInt(activity.getString(R.string.vocabulary), activity.vocabulary).apply();
                    if (activity.oldVocabulary != 0) return null;
                    if (activity.primeRanks.size() != 0) rank = (activity.primeRanks.get(0) +
                            activity.primeRanks.get(activity.primeRanks.size() - 1)) / 2;
                    else rank = 1;
                    WordsHelper.setAreKnown(activity, rank);
                } else {
                    rank = MainActivity.getPreference(activity, R.string.current_position, 1) + activity.count;
                    WordsHelper.setAreKnown(activity, activity.knownWords);
                }
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                        .putInt(activity.getString(R.string.current_position), rank).apply();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                if (activity.primeType.equals(MainActivity.GAME)) activity.finish();
                else {
                    Intent intent = new Intent();
                    intent.putExtra(MainActivity.EXTRA_PRIME_TYPE, MainActivity.LEARN_NEW);
                    intent.putExtra(MainActivity.EXTRA_WORDS,
                            WordsHelper.getWords(activity, MainActivity.LEARN_NEW, null));
                    intent.setClass(activity, SelectionActivity.class);
                    activity.startActivity(intent);
                }
            }
        }
    }

    public void start(View view) {
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager()
                .findFragmentByTag("fragment")).commit();
        if (!isForbidden) ready();
        else {
            isForbidden = false;
            findViewById(R.id.roundProgressBar).setVisibility(View.VISIBLE);
        }
    }

    private void ready() {
        try {
            startFalling();
            fall();
        } catch (IndexOutOfBoundsException e) {
            new EndTask(WordsActivity.this).execute();
        }
    }

    private void pause() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            for (ObjectAnimator objectAnimator : animators) objectAnimator.pause();
            for (Runnable runnable : removeRunnables) removeHandler.removeCallbacks(runnable);
            removeRunnables.clear();
        }
        for (Runnable runnable : fallRunnables) fallHandler.removeCallbacks(runnable);
        fallRunnables.clear();
        isPaused = !isPaused;
    }

    private void resume() {
        for (ObjectAnimator objectAnimator : animators) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                objectAnimator.resume();
            remove((View) objectAnimator.getTarget());
        }
        ready();
        isPaused = !isPaused;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.stop_or_resume).setIcon(isPaused ?
                R.drawable.ic_play_white_24dp : R.drawable.ic_pause_white_24dp);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_or_resume:
                if (!isForbidden) {
                    if (isPaused) {
                        resume();
                        item.setIcon(R.drawable.ic_pause_white_24dp);
                    } else {
                        pause();
                        item.setIcon(R.drawable.ic_play_white_24dp);
                    }
                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.words_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}