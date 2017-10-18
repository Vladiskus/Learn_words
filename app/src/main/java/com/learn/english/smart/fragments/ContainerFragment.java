package com.learn.english.smart.fragments;

import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.learn.english.smart.R;
import com.learn.english.smart.activities.CardsActivity;
import com.learn.english.smart.activities.MainActivity;

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
        position = getArguments().getInt(CardsActivity.POSITION);
        type = getArguments().getInt(CardsActivity.TYPE);
        if (MainActivity.getPreference(getActivity(), R.string.en_ru, 1) == 0 ||
                (type == 3 && ((CardsActivity) getActivity()).primeType.equals(MainActivity.SMALL_REPETITION))) {
            fragment = new CardBackFragment();
        } else {
            fragment = new CardFrontFragment();
            isShowingFront = true;
        }
        if (savedInstanceState != null) isShowingFront = savedInstanceState.getBoolean("isShowingFront");
        Bundle args = new Bundle();
        knownButton = (Button) rootView.findViewById(R.id.knownButton);
        unknownButton = (Button) rootView.findViewById(R.id.unknownButton);
        args.putInt(CardsActivity.POSITION, position);
        args.putInt(CardsActivity.TYPE, type);
        if (type == 1) {
            rootView.findViewById(R.id.buttons).setVisibility(View.GONE);
        } else turnOnButtons();
        knownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard(position);
                ((CardsActivity) getActivity()).markList.set(position, 1);
                makeSound(200, 800);
                turnOffButtons();

            }
        });
        unknownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard(position);
                ((CardsActivity) getActivity()).markList.set(position, 0);
                makeSound(200, 1600);
                turnOffButtons();
            }
        });
        boolean c = position == getActivity().getIntent().getExtras()
                .getParcelableArrayList(MainActivity.EXTRA_WORDS).size() - 1;
        if (!(savedInstanceState == null || savedInstanceState.getBoolean("localTrigger1") == globalTrigger1 ||
                (savedInstanceState.getBoolean("localTrigger2") == globalTrigger2 && c))) {
            fragment = (isShowingFront) ? new CardFrontFragment() : new CardBackFragment();
        }
        fragment.setArguments(args);
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, "fragment").commit();
        } else fragment = getChildFragmentManager().findFragmentByTag("fragment");
        isShowingFront = fragment instanceof CardFrontFragment;
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("localTrigger1", !globalTrigger1);
        outState.putBoolean("localTrigger2", !globalTrigger2);
        outState.putBoolean("isShowingFront", isShowingFront);
        changeGlobalTrigger(1);
        super.onSaveInstanceState(outState);
    }

    public void flipCard(int position) {
        Bundle args = new Bundle();
        args.putInt(CardsActivity.POSITION, position);
        args.putInt(CardsActivity.TYPE, type);
        if (!isShowingFront) {
            Fragment fragment = new CardFrontFragment();
            fragment.setArguments(args);
            this.fragment = fragment;
            getChildFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_left_in,
                    R.animator.card_flip_left_out,
                    R.animator.card_flip_right_in,
                    R.animator.card_flip_right_out)
                    .replace(R.id.fragmentContainer, fragment, "fragment").commit();
            getChildFragmentManager().executePendingTransactions();
        } else {
            Fragment fragment = new CardBackFragment();
            fragment.setArguments(args);
            this.fragment = fragment;
            getChildFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in,
                    R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in,
                    R.animator.card_flip_left_out)
                    .replace(R.id.fragmentContainer, fragment, "fragment").commit();
        }
        isShowingFront = !isShowingFront;
        makeSound(500, 0);
    }

    public void turnOffButtons() {
        knownButton.setClickable(false);
        unknownButton.setClickable(false);
        knownButton.setBackgroundResource(R.drawable.inactive_button);
        unknownButton.setBackgroundResource(R.drawable.inactive_button);
    }

    public void turnOnButtons() {
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

    public void makeSound(int time1, final int time2) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && fragment instanceof CardFrontFragment && time2 == 0 &&
                        MainActivity.getPreference(getActivity(), R.string.audio, 1) == 1 &&
                        ((CardsActivity) getActivity()).mPager.getCurrentItem() == position)
                    ((CardFrontFragment) fragment).makeSound();
                if (type != 1 && time2 != 0) slide(time2);
            }
        }, time1);
    }

    private void slide(int time) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((CardsActivity) getActivity()).mPager.setCurrentItem(position + 1, true);
            }
        }, time);
    }
}
