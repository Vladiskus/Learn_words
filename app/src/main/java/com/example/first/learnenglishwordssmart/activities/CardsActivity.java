package com.example.first.learnenglishwordssmart.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.classes.CustomViewPager;
import com.example.first.learnenglishwordssmart.classes.SoundHelper;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.fragments.ContainerFragment;
import com.example.first.learnenglishwordssmart.fragments.NewLevelFragment;
import com.example.first.learnenglishwordssmart.fragments.OptionsFragment;
import com.example.first.learnenglishwordssmart.fragments.RewardFragment;
import com.example.first.learnenglishwordssmart.fragments.WritingFragment;

import java.util.ArrayList;

public class CardsActivity extends AppCompatActivity {

    public CustomViewPager mPager;
    public PagerAdapter mPagerAdapter;
    private ViewPager.OnPageChangeListener pageChangeListener;
    public ArrayList<Word> words;
    public SoundHelper soundHelper;
    public int number;
    public String primeType;
    private ProgressBar progressBar;
    public ArrayList<Integer> markList = new ArrayList<>();

    public static final String TYPE = "type";
    public static final String POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        primeType = getIntent().getExtras().getString(MainActivity.EXTRA_PRIME_TYPE);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (primeType) {
            case MainActivity.LEARN_NEW:
                getSupportActionBar().setTitle(R.string.cards1);
                break;
            case MainActivity.SMALL_REPETITION:
                getSupportActionBar().setTitle(R.string.cards2);
                break;
            case MainActivity.BIG_REPETITION:
                getSupportActionBar().setTitle(R.string.cards3);
                break;
        }
        soundHelper = new SoundHelper(this);
        mPager = (CustomViewPager) findViewById(R.id.pager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(10000);
        words = getIntent().getExtras().getParcelableArrayList(MainActivity.EXTRA_WORDS);
        number = words.size();
        for (int i = 0; i < number; i++) {
            markList.add(1);
        }
        setAdapter();
        pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                ObjectAnimator animation;
                if (primeType.equals(MainActivity.LEARN_NEW)) animation = ObjectAnimator.ofInt(progressBar,
                        "progress", 10000 * i / (number * 2 + 1));
                else animation = ObjectAnimator.ofInt(progressBar, "progress", 10000 * i / number);
                animation.setDuration(200);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();
                Fragment fragment = (Fragment) mPager.getAdapter().instantiateItem(mPager,
                        mPager.getCurrentItem());
                if (fragment instanceof RewardFragment) ((RewardFragment) fragment).setInfo();
                if (fragment instanceof ContainerFragment && primeType.equals(MainActivity.LEARN_NEW)) {
                    mPager.setPagingEnabled(true);
                } else mPager.setPagingEnabled(false);
                if (MainActivity.getPreference(CardsActivity.this, R.string.audio, 1) == 1) {
                    if (fragment instanceof ContainerFragment)
                        ((ContainerFragment) fragment).makeSound(200, 0);
                    if (fragment instanceof WritingFragment)
                        ((WritingFragment) fragment).makeSound(200);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        };
        mPager.addOnPageChangeListener(pageChangeListener);
        mPager.post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(mPager.getCurrentItem());
            }
        });
    }

    public void setAdapter() {
        mPagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                int type = MainActivity.getPreference(CardsActivity.this, R.string.small_repetition, 0);
                Bundle args = new Bundle();
                Fragment fragment = new Fragment();
                switch (primeType) {
                    case MainActivity.LEARN_NEW:
                        if (position < number) {
                            args.putInt(POSITION, position);
                            args.putInt(TYPE, 1);
                            fragment = new ContainerFragment();
                        }
                        if (position == number) {
                            args.putInt(TYPE, 11);
                            fragment = new RewardFragment();
                        }
                        if (position > number && position < number * 2 + 1) {
                            args.putInt(POSITION, position - number - 1);
                            fragment = new WritingFragment();
                        }
                        if (position == number * 2 + 1) {
                            args.putInt(TYPE, 12);
                            fragment = new RewardFragment();
                        }
                        break;
                    case MainActivity.SMALL_REPETITION:
                        if (position < number) {
                            if (type == 1) {
                                args.putInt(POSITION, position);
                                args.putInt(TYPE, 2);
                                fragment = new ContainerFragment();
                            } else if (type == 2) {
                                args.putInt(POSITION, position);
                                args.putInt(TYPE, 3);
                                fragment = new ContainerFragment();
                            } else {
                                args.putInt(POSITION, position);
                                fragment = new WritingFragment();
                            }
                        } else {
                            args.putInt(TYPE, 2);
                            fragment = new RewardFragment();
                        }
                        break;
                    case MainActivity.BIG_REPETITION:
                        if (position < number) {
                            args.putInt(POSITION, position);
                            if (MainActivity.getPreference(CardsActivity.this, R.string.en_ru, 1) == 0)
                                args.putInt(TYPE, 3);
                            else args.putInt(TYPE, 2);
                            fragment = new ContainerFragment();
                        } else {
                            args.putInt(TYPE, 3);
                            fragment = new RewardFragment();
                        }
                        break;
                }
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getCount() {
                if (primeType.equals(MainActivity.LEARN_NEW)) return (number + 1) * 2;
                else return number + 1;
            }
        };
        if (primeType.equals(MainActivity.LEARN_NEW)) mPager.setPagingEnabled(true);
        else mPager.setPagingEnabled(false);
        mPager.setAdapter(mPagerAdapter);
        progressBar.setProgress(0);
    }

    public void pushNewLevel() {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out).add(R.id.fragmentContainer2,
                new NewLevelFragment()).addToBackStack(null).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundHelper.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cards_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2);
                if (!(fragment instanceof OptionsFragment) || !fragment.isAdded())
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer2, new OptionsFragment()).addToBackStack(null).commit();
                else getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                return true;
            case android.R.id.home:
                if (primeType.equals(MainActivity.LEARN_NEW)) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(MainActivity.EXTRA_PRIME_TYPE, MainActivity.LEARN_NEW);
                    intent.putExtra(MainActivity.EXTRA_WORDS, words);
                    intent.setClass(CardsActivity.this, SelectionActivity.class);
                    NavUtils.navigateUpTo(this, intent);
                    return true;
                } else {
                    Intent upIntent = NavUtils.getParentActivityIntent(this);
                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                        TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                    } else {
                        NavUtils.navigateUpTo(this, upIntent);
                    }
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
