package com.example.first.learnenglishwordssmart.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
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

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.providers.WordsHelper;
import com.example.first.learnenglishwordssmart.fragments.VocabularyFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import me.grantland.widget.AutofitHelper;

public class WordsActivity extends AppCompatActivity {

    private ArrayList<String> words;
    private VocabularyFragment fragment;
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
    private int count = 0;
    private int length = 100;
    private int duration;
    private int multiplier = 1;
    private double realRatio = 1;
    private int backStepCount = 1;
    private double requiredRatio;
    private transient boolean isForbidden = false;
    private boolean isStopped = false;
    private boolean beginner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
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
        primeType = getIntent().getExtras().getString(MainActivity.EXTRA_PRIME_TYPE);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        if (savedInstanceState == null) {
            isForbidden = true;
            if (primeType.equals(MainActivity.GAME)) {
                getSupportActionBar().setTitle(R.string.game_title0);
                fragment = new VocabularyFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment).commit();
            } else getSupportActionBar().setTitle(R.string.game_title1);
        }
        new LoadTask(this).execute();
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
                        .equals(MainActivity.GAME)) ? null : String.valueOf((int) (2 * 8 * MainActivity
                        .getPreference(activity, R.string.number_of_words, 10) / (1 - activity.requiredRatio))), null);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                if (!activity.primeType.equals(MainActivity.GAME)) activity.isForbidden = false;
                if (!activity.isForbidden) {
                    activity.findViewById(R.id.roundProgressBar).setVisibility(View.GONE);
                    activity.startFalling();
                    activity.fall();
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
                    new EndTask(WordsActivity.this).execute();
                    return;
                }
                if (primeType.equals(MainActivity.GAME)) {
                    if (realRatio > 0.5 || assessments.size() < length) fall();
                    else if (assessments.size() >= length)
                        new EndTask(WordsActivity.this).execute();
                    if (assessments.size() == length && realRatio < requiredRatio && !beginner) {
                        if (vocabulary <= backStepCount * length * multiplier) {
                            beginner = true;
                            return;
                        }
                        backStepCount++;
                        count = (vocabulary > backStepCount * length * multiplier) ?
                                vocabulary - backStepCount * length * multiplier : 0;
                        assessments.clear();
                    }
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
                    double a = 20000 * (1 - realRatio);
                    double b = 100 * assessments.size();
                    double c = realRatio >= 0.5 ? a : b + (a - b) / 2 * (1 - assessments.size() / length);
                    animate((int) (assessments.size() > length ? c : c * assessments.size() / length));
                } else animate(10000 * (assessments.size() - knownWords.size()) / 8 /
                        MainActivity.getPreference(WordsActivity.this, R.string.number_of_words, 10));
                if (assessments.size() >= length && realRatio == requiredRatio)
                    primeRanks.add(count - (length / 2 + 3) * multiplier);
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

    private static class EndTask extends AsyncTask<Void, Void, Void> {
        WeakReference<WordsActivity> weakActivity;

        EndTask(WordsActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                activity.animate(10000);
                for (ObjectAnimator objectAnimator : activity.animators)
                    activity.mRelativeLayout.removeView((View) objectAnimator.getTarget());
                for (Runnable runnable : activity.removeRunnables)
                    activity.removeHandler.removeCallbacks(runnable);
                for (Runnable runnable : activity.fallRunnables)
                    activity.fallHandler.removeCallbacks(runnable);
                activity.isForbidden = true;
                activity.findViewById(R.id.roundProgressBar).setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... Voids) {
            WordsActivity activity = weakActivity.get();
            if (activity != null) {
                int rank;
                if (activity.primeType.equals(MainActivity.GAME)) {
                    if (activity.primeRanks.size() != 0) rank = (activity.primeRanks.get(0) +
                            activity.primeRanks.get(activity.primeRanks.size() - 1)) / 2;
                    else rank = 1;
                    WordsHelper.setAreKnown(activity, rank);
                    activity.vocabulary = activity.count - (activity.length / 2 + 3)
                            * activity.multiplier + 12522 - activity.words.size();
                    PreferenceManager.getDefaultSharedPreferences(activity)
                            .edit().putInt(activity.getString(R.string.vocabulary), activity.vocabulary).apply();
                } else
                    rank = MainActivity.getPreference(activity, R.string.current_position, 1) + activity.count;
                WordsHelper.setAreKnown(activity, activity.knownWords);
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

    public void setVocabulary(View view) {
        if (view.getId() == R.id.beginner) vocabulary = 500;
        if (view.getId() == R.id.elementary) vocabulary = 1000;
        if (view.getId() == R.id.pre_intermediate) vocabulary = 1500;
        if (view.getId() == R.id.intermediate) vocabulary = 2000;
        if (view.getId() == R.id.upper_intermediate) vocabulary = 3000;
        if (view.getId() == R.id.advanced) vocabulary = 4000;
        if (view.getId() == R.id.proficient) vocabulary = 6000;
        multiplier = 20;
        count = (vocabulary > length * multiplier) ? vocabulary - length * multiplier : 0;
        fragment.getView().findViewById(R.id.choose).setVisibility(View.GONE);
        fragment.getView().findViewById(R.id.tip).setVisibility(View.VISIBLE);
    }

    public void start(View view) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        if (!isForbidden) {
            findViewById(R.id.roundProgressBar).setVisibility(View.GONE);
            startFalling();
            fall();
        } else isForbidden = false;
    }

    private void pause() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            for (ObjectAnimator objectAnimator : animators) objectAnimator.pause();
            for (Runnable runnable : removeRunnables) removeHandler.removeCallbacks(runnable);
            removeRunnables.clear();
        }
        for (Runnable runnable : fallRunnables) fallHandler.removeCallbacks(runnable);
        fallRunnables.clear();
        isStopped = !isStopped;
    }

    private void resume() {
        for (ObjectAnimator objectAnimator : animators) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                objectAnimator.resume();
            remove((View) objectAnimator.getTarget());
        }
        startFalling();
        fall();
        isStopped = !isStopped;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isForbidden) pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isForbidden) resume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_or_resume:
                if (!isForbidden) {
                    if (isStopped) {
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
