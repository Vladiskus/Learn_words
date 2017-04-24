package com.example.first.learnenglishwordssmart.fragments;


import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.CardsActivity;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;


public class ContainerFragment extends Fragment {

    public static boolean globalTrigger1;
    public static boolean globalTrigger2;
    static int count;
    int position;
    int type;
    boolean isShowingFront;
    Button knownButton;
    Button unknownButton;
    Fragment fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_container, container, false);
        position = getArguments().getInt("position");
        type = getArguments().getInt("type");
        if (MainActivity.getPreference(getActivity(), R.string.en_ru, 1) == 0) {
            fragment = new CardBackFragment();
        } else {
            fragment = new CardFrontFragment();
            isShowingFront = true;
        }
        if (savedInstanceState != null) isShowingFront = savedInstanceState.getBoolean("isShowingFront");
        Bundle args = new Bundle();
        knownButton = (Button) rootView.findViewById(R.id.knownButton);
        unknownButton = (Button) rootView.findViewById(R.id.unknownButton);
        args.putInt("position", position);
        args.putInt("type", type);
        if (type == 1) {
            rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
        } else turnOnButtons();
        knownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard(position, MainActivity.getPreference(getActivity(), R.string.en_ru, 1) == 0);
                new SleepTask().execute(200, 600);
                turnOffButtons();

            }
        });
        unknownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard(position, MainActivity.getPreference(getActivity(), R.string.en_ru, 1) == 0);
                CardsActivity.markList.set(position, 0);
                new SleepTask().execute(200, 1600);
                turnOffButtons();
            }
        });
        boolean c = position == getActivity().getIntent().getExtras()
                .getParcelableArrayList("words").size() - 1;
        if (!(savedInstanceState == null || savedInstanceState.getBoolean("localTrigger1") == globalTrigger1 ||
                (savedInstanceState.getBoolean("localTrigger2") == globalTrigger2 && c))) {
            fragment = (isShowingFront) ? new CardFrontFragment() : new CardBackFragment();
        }
        fragment.setArguments(args);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment).commit();
        isShowingFront = fragment instanceof CardFrontFragment;
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("localTrigger1", !globalTrigger1);
        outState.putBoolean("localTrigger2", !globalTrigger2);
        outState.putBoolean("isShowingFront", isShowingFront);
        changeGlobalTrigger(1);
    }

    public void flipCard(int position, boolean isShowingBack) {
        if (isShowingBack) {
            Fragment fragment = new CardFrontFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putInt("type", type);
            fragment.setArguments(args);
            this.fragment = fragment;
            getChildFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_left_in,
                    R.animator.card_flip_left_out,
                    R.animator.card_flip_right_in,
                    R.animator.card_flip_right_out)
                    .replace(R.id.fragmentContainer, fragment).commit();
            getChildFragmentManager().executePendingTransactions();
        } else {
            Fragment fragment = new CardBackFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putInt("type", type);
            fragment.setArguments(args);
            this.fragment = fragment;
            getChildFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in,
                    R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in,
                    R.animator.card_flip_left_out)
                    .replace(R.id.fragmentContainer, fragment).commit();
        }
        isShowingFront = !isShowingFront;
        makeSound(600);
    }

    private class SlideTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... mSec) {
            try {
                Thread.sleep(mSec[0]);
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            ((CardsActivity) getActivity()).mPager.setCurrentItem(position + 1, true);
        }
    }

    private class SleepTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... mSec) {
            try {
                Thread.sleep(mSec[0]);
            } catch (InterruptedException e) {
            }
            return mSec[1];
        }

        @Override
        protected void onPostExecute(Integer mSec) {
            if (getActivity() != null && fragment instanceof CardFrontFragment &&
                    MainActivity.getPreference(getActivity(), R.string.audio, 1) == 1 && ((CardsActivity)
                    getActivity()).mPager.getCurrentItem() == position) ((CardFrontFragment) fragment).makeSound();
            if (type != 1 && mSec != 0) AsyncTaskCompat.executeParallel(new SlideTask(), mSec);
        }
    }

    private void turnOffButtons() {
        knownButton.setClickable(false);
        unknownButton.setClickable(false);
        knownButton.setBackgroundResource(R.drawable.inactive_button);
        unknownButton.setBackgroundResource(R.drawable.inactive_button);
    }

    private void turnOnButtons() {
        knownButton.setClickable(true);
        unknownButton.setClickable(true);
        knownButton.setBackgroundResource(R.drawable.button);
        unknownButton.setBackgroundResource(R.drawable.button);
    }

    public static void changeGlobalTrigger(int i) {
        if (i == 0) {
            globalTrigger1 = !globalTrigger1;
            count = 1;
        }
        if (i == 1 && count == 1) {
            globalTrigger2 = !globalTrigger2;
            count = 0;
        }
    }

    public void makeSound(int time) {
        if (isShowingFront) new SleepTask().execute(time, 0);
    }
}
