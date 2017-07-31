package com.example.first.learnenglishwordssmart.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.classes.CustomViewPager;
import com.example.first.learnenglishwordssmart.classes.SoundHelper;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;
import com.example.first.learnenglishwordssmart.fragments.ChangeNumberFragment;
import com.example.first.learnenglishwordssmart.fragments.ContainerFragment;
import com.example.first.learnenglishwordssmart.fragments.NewLevelFragment;
import com.example.first.learnenglishwordssmart.fragments.OptionsFragment;
import com.example.first.learnenglishwordssmart.fragments.RewardFragment;
import com.example.first.learnenglishwordssmart.fragments.WritingFragment;

import java.util.ArrayList;
import java.util.Collections;

public class CardsActivity extends AppCompatActivity {

    public CustomViewPager mPager;
    public PagerAdapter mPagerAdapter;
    public ViewPager.OnPageChangeListener pageChangeListener;
    public ArrayList<Word> words;
    public SoundHelper soundHelper;
    public int number;
    public int primeType;
    Context mContext;
    ProgressBar progressBar;
    public static ArrayList<Integer> markList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);
        primeType = getIntent().getExtras().getInt("prime_type");
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (primeType) {
            case 1:
                getSupportActionBar().setTitle(R.string.cards1);
                break;
            case 2:
                getSupportActionBar().setTitle(R.string.cards2);
                break;
            case 3:
                getSupportActionBar().setTitle(R.string.cards3);
                break;
        }
        soundHelper = new SoundHelper(this);
        mPager = (CustomViewPager) findViewById(R.id.pager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(10000);
        mContext = this;
        words = getIntent().getExtras().getParcelableArrayList("words");
        number = words.size();
        for (int i = 0; i < number; i++) {
            markList.add(1);
        }
        setAdapter(primeType);
        pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                ObjectAnimator animation;
                if (primeType == 1) animation = ObjectAnimator.ofInt(progressBar,
                        "progress", 10000 * i / (number * 2 + 1));
                else animation = ObjectAnimator.ofInt(progressBar, "progress", 10000 * i / number);
                animation.setDuration(200);
                animation.setInterpolator(new LinearInterpolator());
                animation.start();
                Fragment fragment = (Fragment) mPager.getAdapter().instantiateItem(mPager,
                        mPager.getCurrentItem());
                if (fragment instanceof RewardFragment) ((RewardFragment) fragment).setInfo();
                if (fragment instanceof ContainerFragment && primeType == 1) {
                    mPager.setPagingEnabled(true);
                } else mPager.setPagingEnabled(false);
                if (MainActivity.getPreference(mContext, R.string.audio, 1) == 1) {
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

    public void setAdapter(final int i) {
        mPagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                int type = MainActivity.getPreference(mContext, R.string.small_repetition, 0);
                Bundle args = new Bundle();
                Fragment fragment = new Fragment();
                switch (i) {
                    case 1:
                        if (position < number) {
                            args.putInt("position", position);
                            args.putInt("type", 1);
                            fragment = new ContainerFragment();
                        }
                        if (position == number) {
                            args.putInt("type", 11);
                            fragment = new RewardFragment();
                        }
                        if (position > number && position < number * 2 + 1) {
                            args.putInt("position", position - number - 1);
                            fragment = new WritingFragment();
                        }
                        if (position == number * 2 + 1) {
                            args.putInt("type", 12);
                            fragment = new RewardFragment();
                        }
                        break;
                    case 2:
                        if (position < number) {
                            if (type == 1) {
                                args.putInt("position", position);
                                args.putInt("type", 2);
                                fragment = new ContainerFragment();
                            } else if (type == 2) {
                                args.putInt("position", position);
                                args.putInt("type", 3);
                                fragment = new ContainerFragment();
                            } else {
                                args.putInt("position", position);
                                fragment = new WritingFragment();
                            }
                        } else {
                            args.putInt("type", 2);
                            fragment = new RewardFragment();
                        }
                        break;
                    case 3:
                        if (position < number) {
                            args.putInt("position", position);
                            if (MainActivity.getPreference(mContext, R.string.en_ru, 1) == 0)
                                args.putInt("type", 3);
                            else args.putInt("type", 2);
                            fragment = new ContainerFragment();
                        } else {
                            args.putInt("type", 3);
                            fragment = new RewardFragment();
                        }
                        break;
                }
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getCount() {
                if (i == 1) return (number + 1) * 2;
                else return number + 1;
            }
        };
        if (i == 1) mPager.setPagingEnabled(true);
        else mPager.setPagingEnabled(false);
        mPager.setAdapter(mPagerAdapter);
        progressBar.setProgress(0);
    }

    public void pushNewLevel() {
        getFragmentManager().beginTransaction().setCustomAnimations(android.R.animator.fade_in,
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
                if (primeType == 1) {
                    Intent intent = new Intent();
                    intent.putExtra("prime_type", 1);
                    intent.putExtra("words", words);
                    intent.setClass(mContext, SelectionActivity.class);
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
