package com.learn.english.smart.providers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.learn.english.smart.R;
import com.learn.english.smart.activities.MainActivity;
import com.learn.english.smart.classes.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WordsHelper {

    public static void createVoidDataBase(Context context) {
        for (int i = 0; i < MainActivity.getPreference(context, R.string.last_rank, 12522); i++) {
            ContentValues cv = new ContentValues();
            cv.put(WordsContract.WordsEntry.WORD_SPELLING, "");
            context.getContentResolver().insert(WordsContract.WordsEntry.CONTENT_URI, cv);
        }
    }

    public static void addWordToDataBase(Word word, Context context) {
        ContentValues cv = new ContentValues();
        cv.put(WordsContract.WordsEntry.WORD_SPELLING, word.getSpelling());
        cv.put(WordsContract.WordsEntry.WORD_TRANSLATION, word.getTranslation());
        cv.put(WordsContract.WordsEntry.WORD_DEFINITIONS, word.getDefinitions());
        cv.put(WordsContract.WordsEntry.WORD_SAMPLES, word.getSamples());
        cv.put(WordsContract.WordsEntry.WORD_RANK, word.getRank());
        cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, word.isKnown() ? 1 : 0);
        cv.put(WordsContract.WordsEntry.WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z",
                Locale.US).format(word.getDate()).toUpperCase());
        if (word.getRank() <= MainActivity.getPreference(context, R.string.last_rank, 12522))
            context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI,
                    cv, WordsContract.WordsEntry._ID, new String[]{String.valueOf(word.getRank())});
        else {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt(context.getString(R.string.last_rank), MainActivity
                            .getPreference(context, R.string.last_rank, 12522) + 1).apply();
            context.getContentResolver().insert(WordsContract.WordsEntry.CONTENT_URI, cv);
        }
    }

    public static Word getWordFromDataBase(Context context, String spelling) {
        return cursorToList(context.getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI,
                null, WordsContract.WordsEntry.WORD_SPELLING, new String[]{spelling}, null), null).get(0);
    }

    private static ArrayList<Word> cursorToList(Cursor cursor, ArrayList<Integer> ranksList) {
        ArrayList<Word> words = new ArrayList<>();
        while (cursor.moveToNext()) {
            int rank = cursor.getInt(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_RANK));
            if (ranksList != null && !ranksList.contains(rank)) continue;
            Word word = new Word();
            word.setRank(rank);
            word.setSpelling(cursor.getString(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_SPELLING)));
            word.setTranslation(cursor.getString(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_TRANSLATION)));
            word.setDefinitions(cursor.getString(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_DEFINITIONS)));
            word.setSamples(cursor.getString(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_SAMPLES)));
            word.setKnown(cursor.getInt(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_IS_KNOWN)) == 1);
            try {
                word.setDate(new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US)
                        .parse(cursor.getString(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_DATE))));
            } catch (Exception e) {
                word.setDate(new Date());
            }
            words.add(word);
        }
        cursor.close();
        return words;
    }

    public static ArrayList<String> getWordsSpelling(Context context, String amount,
                                                     ArrayList<String> exceptionsList) {
        amount = amount != null ? " LIMIT " + amount : "";
        Cursor cursor = context.getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI,
                new String[]{WordsContract.WordsEntry.WORD_SPELLING}, WordsContract.WordsEntry.WORD_IS_KNOWN,
                        exceptionsList == null ? new String[]{"0"} : new String[]{"0", "1"},
                        WordsContract.WordsEntry.WORD_IS_KNOWN + amount);
        ArrayList<String> words = new ArrayList<>();
        while (cursor.moveToNext()) {
            String spelling = cursor.getString(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_SPELLING));
            if (exceptionsList == null || !exceptionsList.contains(spelling)) words.add(spelling);
        }
        cursor.close();
        return words;
    }

    public static ArrayList<Word> getWords(Context context, String primeType, String amount) {
        amount = amount != null ? " LIMIT " + amount : "";
        Cursor mCursor;
        switch (primeType) {
            case MainActivity.GAME:
                return cursorToList(context.getContentResolver()
                        .query(WordsContract.WordsEntry.CONTENT_URI, null, WordsContract.WordsEntry.WORD_IS_KNOWN,
                                new String[]{"0"}, WordsContract.WordsEntry.WORD_IS_KNOWN + amount), null);
            case MainActivity.LEARN_NEW:
                amount = " LIMIT " + String.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(context.getString(R.string.number_of_words), 10));
                return cursorToList(context.getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI,
                        null, WordsContract.WordsEntry.WORD_IS_KNOWN, new String[]{"0", "2"},
                        WordsContract.WordsEntry.WORD_IS_KNOWN + " DESC " + amount), null);
            default:
                mCursor = context.getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI,
                        null, WordsContract.WordsEntry.WORD_IS_KNOWN, new String[]{"3"},
                        WordsContract.WordsEntry._ID + " ASC " + amount);
                break;
        }
        long dayStart = (PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.last_day_start), 0));
        ArrayList<Integer> ranksList = new ArrayList<>();
        while (mCursor.moveToNext()) {
            Date wordDate;
            try {
                wordDate = new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US)
                        .parse(mCursor.getString(mCursor.getColumnIndex(WordsContract.WordsEntry.WORD_DATE)));
            } catch (Exception e) {
                wordDate = new Gson().fromJson(new String(mCursor.getBlob(mCursor
                        .getColumnIndex(WordsContract.WordsEntry.WORD_DATE))), new TypeToken<Date>() {
                }.getType());
            }
            boolean c = false;
            if (MainActivity.getPreference(context, R.string.small_repetition, 0) != 4 &&
                    wordDate.getTime() > dayStart) c = true;
            int[] a = new int[]{1, 3, 7, 14, 30, 60};
            for (int j = 0; j <= MainActivity.getPreference(context, R.string.days_missed, 0); j++) {
                for (int i = 0; i < a.length; i++) {
                    long time = wordDate.getTime() + 86400000 * (a[i] + j);
                    if (time > dayStart && time < dayStart + 86400000) c = true;
                }
            }
            if ((primeType.equals(MainActivity.SMALL_REPETITION) && wordDate.getTime() > dayStart) ||
                    (primeType.equals(MainActivity.BIG_REPETITION) && c)) {
                ranksList.add(mCursor.getInt(mCursor.getColumnIndex(WordsContract.WordsEntry.WORD_RANK)));
            }
        }
        mCursor.moveToPosition(-1);
        return cursorToList(mCursor, ranksList);
    }

    public static void missedDay(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(context
                .getString(R.string.days_missed), MainActivity.getPreference(context,
                R.string.days_missed, 0) + 1).apply();
    }

    public static void missedDaysLearned(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(context
                .getString(R.string.days_missed), 0).apply();
    }

    public static void success(Context context, Word word) {
        long dayStart = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.last_day_start), 0);
        long time = word.getDate().getTime() + 86400000 * 14;
        if (time > dayStart && time < dayStart + 86400000) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putInt(context
                    .getString(R.string.on_learning), MainActivity
                    .getPreference(context, R.string.on_learning, 0) - 1)
                    .putInt(context
                            .getString(R.string.learned), MainActivity
                            .getPreference(context, R.string.learned, 0) + 1)
                    .putInt(context
                            .getString(R.string.vocabulary), MainActivity
                            .getPreference(context, R.string.vocabulary, 0) + 1).apply();
        }
        time = word.getDate().getTime() + 86400000L * 60L;
        if (time > dayStart && time < dayStart + 86400000) {
            ContentValues cv = new ContentValues();
            cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, 1);
            context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                    WordsContract.WordsEntry.WORD_RANK, new String[]{String.valueOf(word.getRank())});
        }
    }

    public static void fail(Context context, Word word) {
        long dayStart = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.last_day_start), 0);
        int[] a = new int[]{1, 3, 7, 14, 30, 60};
        for (int i = 0; i < a.length; i++) {
            long time = word.getDate().getTime() + 86400000 * a[i];
            if (time > dayStart && time < dayStart + 86400000) {
                ContentValues cv = new ContentValues();
                long newTime;
                if (i != 0) newTime = word.getDate().getTime() + 86400000 * (a[i] - a[i - 1] + 1);
                else newTime = word.getDate().getTime() + 86400000;
                cv.put(WordsContract.WordsEntry.WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US)
                        .format(new Date(newTime)).toUpperCase());
                context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                        WordsContract.WordsEntry.WORD_RANK, new String[]{String.valueOf(word.getRank())});
            }
        }
    }

    public static void setIsKnown(Context context, String spelling) {
        ContentValues cv = new ContentValues();
        cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, 1);
        context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                WordsContract.WordsEntry.WORD_SPELLING, new String[]{spelling});
    }

    public static void setAreKnown(Context context, int position) {
        ContentValues cv = new ContentValues();
        cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, 1);
        context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                WordsContract.WordsEntry.WORD_RANK + " <?", new String[]{String.valueOf(position)});
    }

    public static void setAreKnown(Context context, ArrayList<String> words) {
        ContentValues cv = new ContentValues();
        cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, 1);
        context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                WordsContract.WordsEntry.WORD_SPELLING, words.toArray(new String[]{}));
    }

    public static void setOnLearning(Context context, String spelling, Date date) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(
                context.getString(R.string.on_learning), MainActivity
                        .getPreference(context, R.string.on_learning, 0) + 1).apply();
        ContentValues cv = new ContentValues();
        cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, 3);
        cv.put(WordsContract.WordsEntry.WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z",
                Locale.US).format(date).toUpperCase());
        context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                WordsContract.WordsEntry.WORD_SPELLING, new String[]{spelling});
    }

    public static void addUserWord(Context context, String spelling, String translation, boolean isContains) {
        ContentValues cv = new ContentValues();
        cv.put(WordsContract.WordsEntry.WORD_SPELLING, spelling);
        if (translation != null) cv.put(WordsContract.WordsEntry.WORD_TRANSLATION, translation);
        cv.put(WordsContract.WordsEntry.WORD_IS_KNOWN, 2);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        int rank;
        if (isContains) {
            context.getContentResolver().update(WordsContract.WordsEntry.CONTENT_URI, cv,
                    WordsContract.WordsEntry.WORD_SPELLING, new String[]{spelling});
            Cursor cursor = context.getContentResolver().query(WordsContract.WordsEntry.CONTENT_URI,
                    new String[]{WordsContract.WordsEntry.WORD_RANK},
                    WordsContract.WordsEntry.WORD_SPELLING, new String[]{spelling}, null, null);
            cursor.moveToFirst();
            rank = cursor.getInt(cursor.getColumnIndex(WordsContract.WordsEntry.WORD_RANK));
            cursor.close();
        } else {
            rank = MainActivity.getPreference(context, R.string.last_rank, 12522) + 1;
            editor.putInt(context.getString(R.string.last_rank), rank);
            cv.put(WordsContract.WordsEntry.WORD_RANK, rank);
            context.getContentResolver().insert(WordsContract.WordsEntry.CONTENT_URI, cv);
        }
        Set<String> addedWords = PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(context.getString(R.string.added_words), new HashSet<String>());
        addedWords.add(String.valueOf(rank));
        editor.putStringSet(context.getString(R.string.added_words), addedWords).apply();
    }
}