package com.example.first.learnenglishwordssmart.databases;

/**
 * Created by Vlad on 26-Jan-17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WordsDataBase {

    private static WordsDataBase mInstance;
    private static SQLiteDatabase myWritableDb;
    public static final String DATABASE_NAME = "words_database.db";

    private static final String TABLE_NAME = "words_table";

    private static final String WORD_RANK = "rank";
    private static final String WORD_SPELLING = "spelling";
    private static final String WORD_TRANSLATION = "translation";
    private static final String WORD_DEFINITIONS = "definitions";
    private static final String WORD_SAMPLES = "samples";
    private static final String WORD_IS_KNOWN = "isKnown";
    private static final String WORD_DATE = "date";

    private SQLiteDatabase getMyWritableDatabase(Context context) {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = SQLiteDatabase.openDatabase(context.getFilesDir().getAbsolutePath() +
                    "/databases/" + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        }
        return myWritableDb;
    }

    private static WordsDataBase getInstance() {
        if (mInstance == null) {
            mInstance = new WordsDataBase();
        }
        return mInstance;
    }

    private WordsDataBase() {}

    public static void createVoidDataBase(Context context) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        for (int i = 0; i < MainActivity.getPreference(context, R.string.last_rank, 12522); i++) {
            ContentValues cv = new ContentValues();
            cv.put(WORD_SPELLING, "");
            sqdb.insert(TABLE_NAME, null, cv);
        }
    }

    public static void addWordToDataBase(Word word, Context context) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put(WORD_SPELLING, word.getSpelling());
        cv.put(WORD_TRANSLATION, word.getTranslation());
        cv.put(WORD_DEFINITIONS, word.getDefinitions());
        cv.put(WORD_SAMPLES, word.getSamples());
        cv.put(WORD_RANK, word.getRank());
        cv.put(WORD_IS_KNOWN, word.isKnown() ? 1 : 0);
        cv.put(WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US).format(word.getDate()).toUpperCase());
        if (word.getRank() <= MainActivity.getPreference(context, R.string.last_rank, 12522))
            sqdb.update(TABLE_NAME, cv, "_id" + " =?", new String[]{String.valueOf(word.getRank())});
        else {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt(context.getString(R.string.last_rank), MainActivity
                            .getPreference(context, R.string.last_rank, 12522) + 1).apply();
            sqdb.insert(TABLE_NAME, null, cv);
        }
    }

    public static Word getWordFromDataBase(Context context, String spelling) {
        SQLiteDatabase sqdb = getInstance().getMyWritableDatabase(context);
        return cursorToList(sqdb.query(TABLE_NAME, null, WORD_SPELLING + " =?",
                new String[]{spelling}, null, null, null, null)).get(0);
    }

    private static ArrayList<Word> cursorToList(Cursor cursor) {
        ArrayList<Word> words = new ArrayList<>();
        while (cursor.moveToNext()) {
            Word word = new Word();
            word.setRank(cursor.getInt(cursor.getColumnIndex(WORD_RANK)));
            word.setSpelling(cursor.getString(cursor.getColumnIndex(WORD_SPELLING)));
            word.setTranslation(cursor.getString(cursor.getColumnIndex(WORD_TRANSLATION)));
            word.setDefinitions(cursor.getString(cursor.getColumnIndex(WORD_DEFINITIONS)));
            word.setSamples(cursor.getString(cursor.getColumnIndex(WORD_SAMPLES)));
            word.setKnown(cursor.getInt(cursor.getColumnIndex(WORD_IS_KNOWN)) == 1);
            try {
                word.setDate(new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US)
                        .parse(cursor.getString(cursor.getColumnIndex(WORD_DATE))));
            } catch (Exception e) {
                word.setDate(new Date());
            }
            words.add(word);
        }
        cursor.close();
        return words;
    }

    public static ArrayList<Word> getWords(Context context, int primeType, String amount) {
        SQLiteDatabase sqdb = getInstance().getMyWritableDatabase(context);
        Cursor mCursor;
        ArrayList<String> rankList = new ArrayList<>();
        if (primeType == 0) return cursorToList(sqdb.query(TABLE_NAME, null, WORD_IS_KNOWN + " =?",
                new String[]{"0"}, null, null, WORD_IS_KNOWN, amount));
        if (primeType == 1) {
            amount = String.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
                    .getInt(context.getString(R.string.number_of_words), 10));
            return cursorToList(sqdb.query(TABLE_NAME, null, WORD_IS_KNOWN + " =? OR " + WORD_IS_KNOWN + " =?",
                    new String[]{"0", "2"}, null, null, WORD_IS_KNOWN + " DESC", amount));
        } else mCursor = sqdb.query(TABLE_NAME, null, WORD_IS_KNOWN + " =?",
                new String[]{"3"}, null, null, null, amount);
        long dayStart = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.last_day_start), 0);
        while (mCursor.moveToNext()) {
            Date wordDate;
            try {
                wordDate = new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US)
                        .parse(mCursor.getString(mCursor.getColumnIndex(WORD_DATE)));
            } catch (Exception e) {
                wordDate = new Gson().fromJson(new String(mCursor.getBlob(mCursor
                        .getColumnIndex(WORD_DATE))), new TypeToken<Date>() {
                }.getType());
            }
            boolean c = false;
            if (MainActivity.getPreference(context, R.string.small_repetition, 0) != 4 &&
                    wordDate.getTime() > dayStart) c = true;
            int[] a = new int[]{1, 3, 7, 14, 30, 60};
            for (int i = 0; i < a.length; i++) {
                long time = wordDate.getTime() + 86400000 * a[i];
                if (time > dayStart && time < dayStart + 86400000) c = true;
            }
            if ((primeType == 2 && wordDate.getTime() > dayStart) || (primeType == 3 && c)) {
                rankList.add(String.valueOf(mCursor.getInt(mCursor.getColumnIndex(WORD_RANK))));
            }
        }
        mCursor.close();
        if (rankList.size() == 0) return new ArrayList<>();
        String selection = "";
        boolean isFirst = true;
        for (int i = 0; i < rankList.size(); i++) {
            if (isFirst) {
                selection += WORD_RANK + " =?";
                isFirst = false;
            } else selection += " OR " + WORD_RANK + " =?";
        }
        return cursorToList(sqdb.query(TABLE_NAME, null, selection,
                rankList.toArray(new String[rankList.size()]), null, null, null, null));
    }

    public static void success(Context context, int rank, Date date) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        long dayStart = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.last_day_start), 0);
        long time = date.getTime() + 86400000 * 14;
        if (time > dayStart && time < dayStart + 86400000) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(context
                    .getString(R.string.on_learning), MainActivity
                    .getPreference(context, R.string.on_learning, 0) - 1).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(context
                    .getString(R.string.learned), MainActivity
                    .getPreference(context, R.string.learned, 0) + 1).apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(context
                    .getString(R.string.vocabulary), MainActivity
                    .getPreference(context, R.string.vocabulary, 0) + 1).apply();
        }
        time = date.getTime() + 86400000 * 60;
        if (time > dayStart && time < dayStart + 86400000) {
            ContentValues cv = new ContentValues();
            cv.put(WORD_IS_KNOWN, 1);
            sqdb.update(TABLE_NAME, cv, WORD_RANK + " =?", new String[]{String.valueOf(rank)});
        }
    }

    public static void fail(Context context, int rank, Date date) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        long dayStart = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.last_day_start), 0);
        int[] a = new int[]{1, 3, 7, 14, 30, 60};
        for (int i = 0; i < a.length; i++) {
            long time = date.getTime() + 86400000 * a[i];
            if (time > dayStart && time < dayStart + 86400000) {
                ContentValues cv = new ContentValues();
                long newTime;
                if (i != 0) newTime = date.getTime() + 86400000 * (a[i] - a[i - 1] + 1);
                else newTime = date.getTime() + 86400000;
                cv.put(WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US)
                        .format(new Date(newTime)).toUpperCase());
                sqdb.update(TABLE_NAME, cv, WORD_RANK + " =?", new String[]{String.valueOf(rank)});
            }
        }
    }

    public static void setIsKnown(Context context, String spelling) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put(WORD_IS_KNOWN, 1);
        sqdb.update(TABLE_NAME, cv, WORD_SPELLING + " =?", new String[]{spelling});
    }

    public static void setAreKnown(Context context, int position) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put(WORD_IS_KNOWN, 1);
        sqdb.update(TABLE_NAME, cv, WORD_RANK + " <?", new String[]{String.valueOf(position)});
    }

    public static void setOnLearning(Context context, String spelling, Date date) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance().getMyWritableDatabase(context);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(
                context.getString(R.string.on_learning), MainActivity
                        .getPreference(context, R.string.on_learning, 0) + 1).apply();
        ContentValues cv = new ContentValues();
        cv.put(WORD_IS_KNOWN, 3);
        cv.put(WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US).format(date).toUpperCase());
        sqdb.update(TABLE_NAME, cv, WORD_SPELLING + " =?", new String[]{spelling});
    }

    public static String[] getSpellingArray(Context context, boolean all) {
        SQLiteDatabase sqdb = getInstance().getMyWritableDatabase(context);
        ArrayList<Word> words = getWords(context, 1, null);
        ArrayList<String> list = new ArrayList<>();
        for (Word word : words) {
            list.add(word.getSpelling());
        }
        Cursor cursor = sqdb.query(TABLE_NAME, new String[]{WORD_SPELLING}, null, null, null, null, null, null);
        ArrayList<String> spellings = new ArrayList<>();
        while (cursor.moveToNext()) {
            String spelling = cursor.getString(cursor.getColumnIndex(WORD_SPELLING));
            if (!list.contains(spelling) || all) spellings.add(spelling);
        }
        cursor.close();
        return spellings.toArray(new String[spellings.size()]);
    }

    public static void addUserWord(Context context, String spelling, String translation, boolean isContains) {
        SQLiteDatabase sqdb = getInstance().getMyWritableDatabase(context);
        ContentValues cv = new ContentValues();
        cv.put(WORD_SPELLING, spelling);
        if (translation != null) cv.put(WORD_TRANSLATION, translation);
        cv.put(WORD_IS_KNOWN, 2);
        if (isContains) sqdb.update(TABLE_NAME, cv, WORD_SPELLING + " =?", new String[]{spelling});
        else {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt(context.getString(R.string.last_rank), MainActivity
                            .getPreference(context, R.string.last_rank, 12522) + 1).apply();
            cv.put(WORD_RANK, MainActivity.getPreference(context, R.string.last_rank, 12522));
            sqdb.insert(TABLE_NAME, null, cv);
        }
    }
}

