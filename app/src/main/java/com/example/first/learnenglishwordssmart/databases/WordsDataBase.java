package com.example.first.learnenglishwordssmart.databases;

/**
 * Created by Vlad on 26-Jan-17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.classes.Word;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WordsDataBase extends SQLiteAssetHelper implements BaseColumns {

    private static WordsDataBase mInstance;
    private static SQLiteDatabase myWritableDb;
    private static final String DATABASE_NAME = "words_database.db";

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "words_table";

    public static final String WORD_RANK = "rank";
    public static final String WORD_SPELLING = "spelling";
    public static final String WORD_TRANSLATION = "translation";
    public static final String WORD_DEFINITIONS = "definitions";
    public static final String WORD_SAMPLES = "samples";
    public static final String WORD_IS_KNOWN = "isKnown";
    public static final String WORD_DATE = "date";

    /*private static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WORD_RANK + " INTEGER, "
            + WORD_IS_KNOWN + " INTEGER, "
            + WORD_SPELLING + " TEXT, "
            + WORD_TRANSLATION + " TEXT, "
            + WORD_DEFINITIONS + " TEXT, "
            + WORD_SAMPLES + " TEXT, "
            + WORD_DATE + " TEXT);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }*/

    private WordsDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        onCreate(sqLiteDatabase);
    }

    @Override
    public void close() {
        super.close();
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }

    private SQLiteDatabase getMyWritableDatabase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }
        return myWritableDb;
    }

    private static WordsDataBase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WordsDataBase(context);
        }
        return mInstance;
    }

    public static void createVoidDataBase(Context context) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
        for (int i = 0; i < MainActivity.getPreference(context, R.string.last_rank, 12522); i++) {
            ContentValues cv = new ContentValues();
            cv.put(WORD_SPELLING, "");
            sqdb.insert(TABLE_NAME, null, cv);
        }
    }

    public static void addWordToDataBase(Word word, Context context) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WORD_SPELLING, word.getSpelling());
        cv.put(WORD_TRANSLATION, word.getTranslation());
        cv.put(WORD_DEFINITIONS, word.getDefinitions());
        cv.put(WORD_SAMPLES, word.getSamples());
        cv.put(WORD_RANK, word.getRank());
        cv.put(WORD_IS_KNOWN, word.isKnown() ? 1 : 0);
        cv.put(WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US).format(word.getDate()).toUpperCase());
        if (word.getRank() <= MainActivity.getPreference(context, R.string.last_rank, 12522))
            sqdb.update(TABLE_NAME, cv, _ID + " =?", new String[]{String.valueOf(word.getRank())});
        else {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putInt(context.getString(R.string.last_rank), MainActivity
                            .getPreference(context, R.string.last_rank, 12522) + 1).apply();
            sqdb.insert(TABLE_NAME, null, cv);
        }
    }

    public static Word getWordFromDataBase(Context context, String spelling) {
        SQLiteDatabase sqdb = getInstance(context).getMyWritableDatabase();
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
        SQLiteDatabase sqdb = getInstance(context).getMyWritableDatabase();
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
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
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
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
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
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WORD_IS_KNOWN, 1);
        sqdb.update(TABLE_NAME, cv, WORD_SPELLING + " =?", new String[]{spelling});
    }

    public static void setAreKnown(Context context, int position) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(WORD_IS_KNOWN, 1);
        sqdb.update(TABLE_NAME, cv, WORD_RANK + " <?", new String[]{String.valueOf(position)});
    }

    public static void setOnLearning(Context context, String spelling, Date date) {
        SQLiteDatabase sqdb = WordsDataBase.getInstance(context).getMyWritableDatabase();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(
                context.getString(R.string.on_learning), MainActivity
                        .getPreference(context, R.string.on_learning, 0) + 1).apply();
        ContentValues cv = new ContentValues();
        cv.put(WORD_IS_KNOWN, 3);
        cv.put(WORD_DATE, new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US).format(date).toUpperCase());
        sqdb.update(TABLE_NAME, cv, WORD_SPELLING + " =?", new String[]{spelling});
    }

    public static String[] getSpellingArray(Context context, boolean all) {
        SQLiteDatabase sqdb = getInstance(context).getMyWritableDatabase();
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
        SQLiteDatabase sqdb = getInstance(context).getMyWritableDatabase();
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

