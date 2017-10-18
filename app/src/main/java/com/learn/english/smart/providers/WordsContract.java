package com.learn.english.smart.providers;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class WordsContract {
    public static final String CONTENT_AUTHORITY = "com.learn.english.smart.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WORDS = WordsEntry.TABLE_NAME;

    public static class WordsEntry implements BaseColumns {

        public static final String TABLE_NAME = "words_table";
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WORDS).build();
        public static final String CONTENT_ITEMS_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WORDS;

        static final String WORD_RANK = "rank";
        static final String WORD_SPELLING = "spelling";
        static final String WORD_TRANSLATION = "translation";
        static final String WORD_DEFINITIONS = "definitions";
        static final String WORD_SAMPLES = "samples";
        static final String WORD_IS_KNOWN = "isKnown";
        static final String WORD_DATE = "date";

        public static Uri buildUri(Long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
