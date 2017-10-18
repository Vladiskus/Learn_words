package com.learn.english.smart.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Vlad on 26-Jan-17.
 */

public class Word implements Parcelable{
    private int rank;
    private String spelling;
    private String translation;
    private String definitions;
    private String samples;
    private boolean isKnown;
    private Date date;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {String.valueOf(rank), spelling, translation, definitions,
                samples, String.valueOf(isKnown), new SimpleDateFormat("dd MMM yy HH:mm:ss z",
                Locale.US).format(date).toUpperCase()});
    }

    private Word(Parcel in){
        String[] data = new String[7];
        in.readStringArray(data);
        rank = Integer.parseInt(data[0]);
        spelling = data[1];
        translation = data[2];
        definitions = data[3];
        samples = data[4];
        isKnown = Boolean.parseBoolean(data[5]);
        try {
            date = new SimpleDateFormat("dd MMM yy HH:mm:ss z", Locale.US).parse(data[6]);
        } catch (Exception e) {
            date = new Gson().fromJson(data[6], new TypeToken<Date>() {}.getType());
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public int getRank() {
        return rank;
    }

    public String getSpelling() {
        return spelling;
    }

    public String getTranslation() {
        return translation;
    }

    public String getDefinitions() {
        return definitions;
    }

    public String getSamples() {
        return samples;
    }

    public boolean isKnown() {
        return isKnown;
    }

    public Date getDate() {
        return date;
    }

    public void setKnown(boolean known) {
        isKnown = known;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setSpelling(String spelling) {
        this.spelling = spelling;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    public void setSamples(String samples) {
        this.samples = samples;
    }

    public Word(int rank, String spelling, String translation,
                String definitions, String samples, boolean isKnown, Date date) {
        this.rank = rank;
        this.spelling = spelling;
        this.translation = translation;
        this.definitions = definitions;
        this.samples = samples;
        this.isKnown = isKnown;
        this.date = date;
    }

    public Word() {
    }

    @Override
    public String toString() {
        return "rank = " + rank + '\n' +
                "spelling = " + spelling +
                "translation = " + translation +
                "definitions = " + definitions + " " +
                "samples = " + samples + " " +
                "isKnown = " + isKnown + " " +
                "date = " + date;
    }
}

