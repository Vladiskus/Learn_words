package com.example.first.learnenglishwordssmart.classes;

import android.content.Context;
import android.util.Log;

import com.example.first.learnenglishwordssmart.providers.WordsHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vlad on 27-Jan-17.
 */

public class DataBaseFiller {
    public static void FillWordsDataBase(Context mContext) {
        WordsHelper.createVoidDataBase(mContext);
        for (int q = 1; q <= 6; q++) {
            StringBuilder string = new StringBuilder("");
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(mContext.getAssets().open(q + ".txt")));
                String newLine;
                while ((newLine = reader.readLine()) != null) {
                    string.append(newLine);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Pattern p = Pattern.compile("[0-9]{5}\\t");
            Matcher m = p.matcher(string);
            ArrayList<String> positions = new ArrayList<>();
            positions.add("0");
            while (m.find()) positions.add(m.group());
            String[] strings1 = string.toString()
                    .replaceAll("&laquo;?", "").replaceAll("([’“”ˌˈ`‘]|&quot;|&quot)", "'")
                    .replaceAll("&raquo;?", "").split("[0-9]{5}\\t");
            for (int i = 0; i < strings1.length; i++) {
                if (strings1[i].contains("I отрицание")) continue;
                Word word = new Word();
                if (positions.get(i).replaceAll("^0*", "").trim().equals("")) continue;
                word.setRank(Integer.parseInt(positions.get(i).replaceAll("^0*", "").trim()));
                String[] strings2 = strings1[i].replaceAll("</b> .*English ]", "</b>").replaceAll("см\\..*]", "")
                        .replaceAll("ср\\..*]", "").replaceAll("<br /> *<br />", "\t").replaceAll("\\t *\\t", "\t")
                        .split("\\t[\\[/].*\\]\\t");
                for (int j = 0; j < strings2.length; j++) {
                    String[] strings3 = strings2[j].split("\\t");
                    if (j == 0) {
                        for (int k = 0; k < strings3.length; k++) {
                            Pattern p0 = Pattern.compile(",");
                            Matcher m0 = p0.matcher(strings3[k]);
                            int counter = 0;
                            while (m0.find()) counter++;
                            String[] strings4 = strings3[k].replaceAll("^.* - ", "").split(";");
                            String newString = "";
                            for (int l = 0; l < strings4.length; l++) {
                                if (l < 3) {
                                    if (l < 3 && strings4.length < 5) {
                                        if (counter > 1) newString += strings4[l].split(",")[0].replaceAll("\\(.*", "").replaceAll("<br>.*<br>", "") + ",";
                                        else newString += strings4[l].replaceAll("<br>.*<br>", "") + ",";
                                    } else if (l == 0 || l == 2 || l == 4) {
                                        if (counter > 1) newString += strings4[l].split(",")[0].replaceAll("\\(.*", "").replaceAll("<br>.*<br>", "") + ",";
                                        else newString += strings4[l].replaceAll("<br>.*<br>", "") + ",";
                                    }
                                }
                            }
                            if (k == 0) word.setTranslation((newString + "\n").replaceAll(",*\\n", "\n").replaceAll(" , ", ", ").replaceAll("\\n$", ""));
                            if (k == 1) word.setSpelling((newString + "\n").replaceAll(",*\\n", "\n").replaceAll(" , ", ", ").replaceAll("\\n$", ""));
                        }
                    } else {
                        for (int k = 0; k < strings3.length; k++) {
                            strings3[k] = "\n" + strings3[k] + "\n";
                            String newString = strings3[k].replaceAll("\\n.*(phrasal verb|↔|&gt|&lt|&amp).*\\n", "\n")
                                    .replaceAll("<br />.*(noun|adverb|adjective)", "<br />").replaceAll(";", ",").replaceAll("SYN.*\\n", "\n")
                                    .replaceAll("<br>[ ]*", "\n").replaceAll("<br />[ ]*", "\n").replaceAll("/[ ]*/", "").replaceAll("&nbsp,&nbsp,[ ]*", " ")
                                    .replaceAll("[ ]*&nbsp,[ ]*", " ").replaceAll("sfx.*wav", "").replaceAll("\\n<b>REGISTER</b>", "")
                                    .replaceAll("OPP.*\\n", "\n").replaceAll("\\n<b>GRAMMAR</b>", "").replaceAll("\\n.* = .*\\n", "\n")
                                    .replaceAll("\\n<b> *", "\n(").replaceAll(" <b>[ ]*", " (").replaceAll("[ ]*</b> ", ") ").replaceAll("<b>[ ]*\\n", "\n")
                                    .replaceAll("\\([1-9]", "1").replaceAll("</b><", ",<").replaceAll("</b>\\n", ")\n").replaceAll("\\n.*for Meaning.*\\n", "\n")
                                    .replaceAll("[ ]*<b>", "").replaceAll("[ ]*</b>[ ]*", "").replaceAll("\\n.*See main.*\\n", "")
                                    .replaceAll("—", " — ").replaceAll(" *— *", " — ").replaceAll("&.*where", "").replaceAll("&laquo,", "").replaceAll("&raquo,", "")
                                    .replaceAll("\\n—.*\\n", "\n").replaceAll("\\n—.*\\n", "\n").replaceAll(" \\. ", ". ")
                                    .replaceAll("\\n *[()] *\\n", "\n").replaceAll(" см\\..*\\n", "\n")
                                    .replaceAll("\\n[ ]*[0-9]?[()]?[.]?[ ]*\\n", "\n").replaceAll("⇨.*\\n", "\n").replaceAll("^\\n", "").replaceAll("\\n\\n", "\n")
                                    .replaceAll("[.] —", " —").replaceAll("-л\\n", "-л.\n").replaceAll("[↑≈►=·♦][ ]*", "")
                                    .replaceAll("[.]wav", " wav").replaceAll("\\(, ", "(").replaceAll("wavES", "waves")
                                    .replaceAll("[ ]*(especially )?(American|British) English\\)?[ ]*", " ").replaceAll("\\( *\\)", "")
                                    .replaceAll(" also \\+.*]", ",").replaceAll(" /.*/\\)", ")").replaceAll(" / ", "/")
                                    .replaceAll(" [\\[/]", " (").replaceAll("[]/] ", ") ").replaceAll("[]/]\\n", ")\n").replaceAll("[]/],", "),");
                            String[] array = newString.split("\\n");
                            String s = "";
                            for (int l = 0; l < array.length; l++) {
                                Pattern p0 = Pattern.compile(" [а-я]\\) ");
                                Matcher m0 = p0.matcher(array[l]);
                                StringBuffer sb0 = new StringBuffer();
                                while (m0.find()) {
                                    m0.appendReplacement(sb0, m0.group().replace(')', '|'));
                                }
                                m0.appendTail(sb0);
                                array[l] = sb0.toString();
                                int counter1 = 0;
                                int counter2 = 0;
                                Pattern p3 = Pattern.compile("[1-9A-Z/ ]{3,99}[A-Z]( |)");
                                Matcher m3 = p3.matcher(array[l]);
                                StringBuffer sb = new StringBuffer();
                                while (m3.find()) {
                                    m3.appendReplacement(sb, "(" + m3.group().trim().toLowerCase() + ") ");
                                }
                                m3.appendTail(sb);
                                array[l] = sb.toString();
                                Pattern p1 = Pattern.compile("[(]");
                                Matcher m1 = p1.matcher(array[l]);
                                Pattern p2 = Pattern.compile("[)]");
                                Matcher m2 = p2.matcher(array[l]);
                                while (m1.find()) counter1++;
                                while (m2.find()) counter2++;
                                if (counter1 == 1 && counter2 == 0)
                                    array[l] = array[l].replaceAll("[ ]*\\([ ]*", "/");
                                if (counter1 == 0 && counter2 == 1) array[l] = "(" + array[l];
                                if (counter1 == 2 && counter2 == 1) {
                                    int x1 = array[l].lastIndexOf('(');
                                    int x2 = array[l].lastIndexOf(')');
                                    if (x1 > x2)
                                        array[l] = new StringBuilder(array[l]).replace(x1, x1 + 1, "/").toString().replaceAll("[ ]*/[ ]*", "/");
                                    else
                                        array[l] = new StringBuilder(array[l]).replace(array[l].indexOf('('),
                                                array[l].indexOf('(') + 1, "/").toString().replaceAll("[ ]*/[ ]*", "/");
                                }
                                if (counter1 != counter2) {
                                    int x1 = array[l].indexOf('(');
                                    int x2 = array[l].indexOf(')');
                                    if (x1 > x2) array[l] = array[l].replaceFirst("\\)", "");
                                    x1 = array[l].lastIndexOf('(');
                                    x2 = array[l].lastIndexOf(')');
                                    if (x1 > x2) array[l] = array[l].replaceFirst("\\(", "");
                                }
                                if (!array[l].startsWith((l + 1) + ".") && k == 0 && !array[l].equals("") && !array[l].equals("\n"))
                                    array[l] = (l + 1) + ". " + array[l].replaceAll("[0-9] *\\. *", "");
                                Pattern p4 = Pattern.compile("\\( *\\(.*\\) *\\)");
                                Matcher m4 = p4.matcher(array[l]);
                                StringBuffer sb4 = new StringBuffer();
                                while (m4.find()) {
                                    m4.appendReplacement(sb4, m4.group().replaceAll("\\( *\\(", "(").replaceAll("\\) *\\)", ")"));
                                }
                                m4.appendTail(sb4);
                                array[l] = sb4.toString();
                                s += array[l] + "\n";
                            }
                            if (k == 0) word.setDefinitions(s.replaceAll("\\n\\n", "\n").replaceAll("^\\n", "").replaceAll("\\./also.*\\)", ".").replaceAll("\\. ,* ", ". ")
                                    .replaceAll("\\. \\(?old-fashioned\\)?", ". (old-fashioned)").replaceAll("\\) \\(?old-fashioned\\)?", ") (old-fashioned)")
                                    .replaceAll("\\. technical", ". (technical)").replaceAll("\\) technical", ") (technical)").replaceAll(",\\)", ")")
                                    .replaceAll(" informal", " (informal)").replaceAll(" formal", " (formal)").replaceAll("\\([0-9]*\\)", "")
                                    .replaceAll("\\.\\(", ". (").replaceAll(" , ", ", ").replaceAll(" ,", ", ").replaceAll("\\./", ". ")
                                    .replaceAll("/\\n", "\n").replaceAll("/!", "!").replaceAll(" *\\(us\\) *", " US ").replaceAll(" *\\(uk\\) *", " UK ")
                                    .replaceAll("\\n$", "").replaceAll("\\|", ")").replaceAll(" */ *", " / "));
                            if (k == 1) word.setSamples(s.replaceAll("\\n\\n", "\n").replaceAll("^\\n", "").replaceAll("\\./also.*\\)", ".").replaceAll("\\. ,* ", ". ")
                                    .replaceAll("\\. \\(?old-fashioned\\)?", ". (old-fashioned)").replaceAll("\\) \\(?old-fashioned\\)?", ") (old-fashioned)")
                                    .replaceAll("\\. technical", ". (technical)").replaceAll("\\) technical", ") (technical)").replaceAll(",\\)", ")")
                                    .replaceAll(" informal", " (informal)").replaceAll(" formal", " (formal)").replaceAll("\\([0-9]*\\)", "")
                                    .replaceAll("\\.\\(", ". (").replaceAll(" , ", ", ").replaceAll(" ,", ", ").replaceAll("\\./", ". ")
                                    .replaceAll("/\\n", "\n").replaceAll("/!", "!").replaceAll(" *\\(us\\) *", " US ").replaceAll(" *\\(uk\\) *", " UK ")
                                    .replaceAll("\\n$", "").replaceAll("\\|", ")").replaceAll(" */ *", " / "));
                        }
                    }
                }
                word.setKnown(false);
                word.setDate(new Date());
                WordsHelper.addWordToDataBase(word, mContext);
            }
        }
    }
}
