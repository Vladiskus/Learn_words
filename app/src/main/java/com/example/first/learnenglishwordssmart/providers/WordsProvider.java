package com.example.first.learnenglishwordssmart.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class WordsProvider extends ContentProvider {

    private static SQLiteDatabase wordsDB;
    public static final String DATABASE_NAME = "words_database.db";
    private static final UriMatcher uriMatcher = buildUriMatcher();
    public static final int WORDS = 100;
    public static final int WORD = 101;
    private Context mContext;

    protected static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(WordsContract.CONTENT_AUTHORITY, WordsContract.PATH_WORDS, WORDS);
        matcher.addURI(WordsContract.CONTENT_AUTHORITY, WordsContract.PATH_WORDS + "/#", WORD);
        return matcher;
    }

    private SQLiteDatabase getSQLiteDataBase(Context context) {
        if (wordsDB == null)
            wordsDB = SQLiteDatabase.openDatabase(context.getFilesDir().getAbsolutePath()
                    + "/databases/" + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        return wordsDB;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case WORDS:
                selection = addSelectionArgs(selection, selectionArgs, " OR ");
                break;
            case WORD:
                selection = addKeyIdCheckToWhereStatement(addSelectionArgs(selection,
                        selectionArgs, " OR "), ContentUris.parseId(uri));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        int returnCount = getSQLiteDataBase(mContext).delete(WordsContract.WordsEntry.TABLE_NAME,
                selection, selectionArgs);
        if (selection == null || returnCount > 0)
            mContext.getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case WORDS:
                return WordsContract.WordsEntry.CONTENT_ITEMS_TYPE;
            case WORD:
                return WordsContract.WordsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        switch (uriMatcher.match(uri)) {
            case WORDS:
                long id = getSQLiteDataBase(mContext)
                        .insert(WordsContract.WordsEntry.TABLE_NAME, null, values);
                if (id > 0) returnUri = WordsContract.WordsEntry.buildUri(id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        mContext.getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case WORDS:
                selection = addSelectionArgs(selection, selectionArgs, "OR");
                break;
            case WORD:
                selection = addKeyIdCheckToWhereStatement(addSelectionArgs(selection,
                        selectionArgs, " OR "), ContentUris.parseId(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Cursor cursor = getSQLiteDataBase(mContext).query(WordsContract.WordsEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(mContext.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case WORDS:
                if (!selection.contains("<?"))
                    selection = addSelectionArgs(selection, selectionArgs, "OR");
                break;
            case WORD:
                selection = addKeyIdCheckToWhereStatement(addSelectionArgs(selection,
                        selectionArgs, " OR "), ContentUris.parseId(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        int returnCount = getSQLiteDataBase(mContext).update(WordsContract.WordsEntry.TABLE_NAME,
                values, selection, selectionArgs);
        if (returnCount > 0) mContext.getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    private String addSelectionArgs(String selection, String[] selectionArgs, String operation) {
        if (selection == null || selectionArgs == null) return null;
        else {
            String selectionResult = "";
            for (int i = 0; i < selectionArgs.length - 1; i++)
                selectionResult += (selection + " = ? " + operation + " ");
            selectionResult += (selection + " = ?");
            return selectionResult;
        }
    }

    private static String addKeyIdCheckToWhereStatement(String whereStatement, long id) {
        String newWhereStatement = "";
        if (!TextUtils.isEmpty(whereStatement)) newWhereStatement = whereStatement + " AND ";
        return newWhereStatement + WordsContract.WordsEntry._ID + " = '" + id + "'";
    }


}
