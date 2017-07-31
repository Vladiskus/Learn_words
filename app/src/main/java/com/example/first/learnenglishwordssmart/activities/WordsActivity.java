package com.example.first.learnenglishwordssmart.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.IntentCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;
import com.example.first.learnenglishwordssmart.fragments.VocabularyFragment;

import java.util.ArrayList;

import me.grantland.widget.AutofitHelper;

public class WordsActivity extends AppCompatActivity {

    RelativeLayout mRelativeLayout;
    VocabularyFragment fragment;
    ArrayList<Word> words;
    ArrayList<Integer> assessments = new ArrayList<>();
    ArrayList<Integer> primeRanks = new ArrayList<>();
    ArrayList<String> knownWords = new ArrayList<>();
    ArrayList<ObjectAnimator> animators = new ArrayList<>();
    ArrayList<FallingTask> fallingTasks = new ArrayList<>();
    ArrayList<RemoveTask> removeTasks = new ArrayList<>();
    Context mContext;
    int vocabulary;
    int primeType;
    int count = 0;
    int length = 100;
    int duration = 3800;
    int multiplier = 1;
    double realRatio = 1;
    int backStepCount = 1;
    double requiredRatio;
    transient boolean isForbidden = false;
    boolean isStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ProgressBar) findViewById(R.id.progressBar)).setMax(10000);
        mContext = this;
        primeType = getIntent().getExtras().getInt("prime_type");
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        if (primeType == 0) {
            getSupportActionBar().setTitle(R.string.game_title0);
            if (savedInstanceState == null) {
                isForbidden = true;
                //findViewById(R.id.main).setVisibility(View.INVISIBLE);
                fragment = new VocabularyFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment).commit();
            }
        } else getSupportActionBar().setTitle(R.string.game_title1);
        new LoadTask().execute();
    }

    private void startFalling() {
        final Button view = new Button(this);
        int diameter = 100 * (int) getResources().getDisplayMetrics().density;
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
        view.setText(words.get(count + multiplier).getSpelling());
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
        removeTasks.add(new RemoveTask());
        AsyncTaskCompat.executeParallel(removeTasks.get(removeTasks.size() - 1), view);
    }

    private class LoadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... Voids) {
            requiredRatio = 1 - (double) MainActivity.getPreference(mContext, R.string.number_of_words, 10) / 50;
            words = WordsDataBase.getWords(mContext, 0, (primeType == 0) ? null : String.valueOf((int) (2 * 8 *
                    MainActivity.getPreference(mContext, R.string.number_of_words, 10) / (1 - requiredRatio))));
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            if (!isForbidden) {
                startFalling();
                fallingTasks.add(new FallingTask());
                AsyncTaskCompat.executeParallel(fallingTasks.get(fallingTasks.size() - 1));
            } else isForbidden = false;
        }
    }

    private class FallingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... Voids) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            fallingTasks.add(new FallingTask());
            try {
                startFalling();
            } catch (IndexOutOfBoundsException e) {
                end();
                return;
            }
            if (primeType == 0) {
                if (assessments.size() == length && realRatio < requiredRatio) {
                    if (vocabulary <= backStepCount * length * multiplier) {
                        count = (length / 2 + 3) * multiplier;
                        end();
                        return;
                    }
                    backStepCount++;
                    count = (vocabulary > backStepCount * length * multiplier) ?
                            vocabulary - backStepCount * length * multiplier : 0;
                    assessments.clear();
                }
                if (realRatio > 0.5 || assessments.size() < length)
                    AsyncTaskCompat.executeParallel(fallingTasks.get(fallingTasks.size() - 1));
                else if (assessments.size() >= length) end();
            } else {
                int sum = 0;
                for (int i : assessments) {
                    if (i == 0) sum++;
                }
                if (sum < 8 * MainActivity.getPreference(mContext, R.string.number_of_words, 10))
                    AsyncTaskCompat.executeParallel(fallingTasks.get(fallingTasks.size() - 1));
                else {
                    PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                            .putInt(getString(R.string.current_position), MainActivity
                                    .getPreference(mContext, R.string.current_position, 1) + count).apply();
                    for (String s : knownWords) {
                        WordsDataBase.setIsKnown(mContext, s);
                    }
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("prime_type", 1);
                    intent.putExtra("words", WordsDataBase.getWords(mContext, 1, null));
                    intent.setClass(mContext, SelectionActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    private class RemoveTask extends AsyncTask<View, Void, View> {

        @Override
        protected View doInBackground(View... views) {
            try {
                Thread.sleep((long) (duration * 0.8));
            } catch (InterruptedException e) {
            }
            return views[0];
        }

        @Override
        protected void onPostExecute(View view) {
            if (view.getVisibility() == View.VISIBLE) {
                knownWords.add(((Button) view).getText().toString());
                assessments.add(1);
            } else assessments.add(0);
            double sum = 0;
            for (int i = (assessments.size() >= length) ? assessments.size() - length : 0; i < assessments.size(); i++) {
                sum += assessments.get(i);
            }
            realRatio = sum / ((assessments.size() >= length) ? length : assessments.size());
            int sum2 = 0;
            for (int i : assessments) {
                if (i == 0) sum2++;
            }
            ProgressBar progressBar = ((ProgressBar) findViewById(R.id.progressBar));
            ObjectAnimator animation;
            if (primeType == 0) animation = ObjectAnimator.ofInt(progressBar, "progress", (int)
                    ((assessments.size() > 40) ? 20000 * (1 - realRatio) : 2000 * assessments.size() / 40));
            else animation = ObjectAnimator.ofInt(progressBar, "progress", 10000 * sum2 / 8 /
                    MainActivity.getPreference(mContext, R.string.number_of_words, 10));
            animation.setDuration(700);
            animation.setInterpolator(new LinearInterpolator());
            animation.start();
            /*Log.e("progress", String.valueOf((int) ((assessments.size() > 40)
                            ? 20000 * (1 - realRatio) : 2000 * assessments.size() / 40)));*/
            if (assessments.size() >= length && realRatio == requiredRatio)
                primeRanks.add(count - (length / 2 + 3) * multiplier);
            mRelativeLayout.removeView(view);
            animators.remove(0);
        }
    }

    private void end() {
        int rank;
        vocabulary = count - (length / 2 + 3) * multiplier;
        if (vocabulary != 0) {
            rank = (primeRanks.get(0) + primeRanks.get(primeRanks.size() - 1)) / 2;
            WordsDataBase.setAreKnown(mContext, rank);
        } else rank = 1;
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(getString(R.string.current_position), rank).apply();
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putInt(getString(R.string.vocabulary), vocabulary).apply();
        for (String s : knownWords) {
            WordsDataBase.setIsKnown(mContext, s);
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(mContext, MainActivity.class);
        startActivity(intent);
    }

    public void setVocabulary(View view) {
        if (view.getId() == R.id.beginner) vocabulary = 500;
        if (view.getId() == R.id.elementary) vocabulary = 1000;
        if (view.getId() == R.id.pre_intermediate) vocabulary = 2000;
        if (view.getId() == R.id.intermediate) vocabulary = 3000;
        if (view.getId() == R.id.upper_intermediate) vocabulary = 4000;
        if (view.getId() == R.id.advanced) vocabulary = 5000;
        if (view.getId() == R.id.proficient) vocabulary = 7000;
        multiplier = 20;
        count = (vocabulary > length * multiplier) ? vocabulary - length * multiplier : 0;
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        findViewById(R.id.main).setVisibility(View.VISIBLE);
        if (!isForbidden) {
            startFalling();
            AsyncTaskCompat.executeParallel(new FallingTask());
        } else isForbidden = false;
    }

    private void pause() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            for (ObjectAnimator objectAnimator : animators) {
                objectAnimator.pause();
            }
            for (RemoveTask removeTask : removeTasks) {
                removeTask.cancel(true);
            }
            removeTasks.clear();
        }
        for (FallingTask fallingTask : fallingTasks) {
            fallingTask.cancel(true);
        }
        fallingTasks.clear();
        isStopped = !isStopped;
    }

    private void resume() {
        for (ObjectAnimator objectAnimator : animators) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                objectAnimator.resume();
            removeTasks.add(new RemoveTask());
            AsyncTaskCompat.executeParallel(removeTasks.get(removeTasks.size() - 1),
                    (View) objectAnimator.getTarget());
        }
        startFalling();
        fallingTasks.add(new FallingTask());
        AsyncTaskCompat.executeParallel(fallingTasks.get(fallingTasks.size() - 1));
        isStopped = !isStopped;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
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
                if (isStopped) {
                    resume();
                    item.setIcon(R.drawable.ic_pause_white_24dp);
                } else {
                    pause();
                    item.setIcon(R.drawable.ic_play_white_24dp);
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
