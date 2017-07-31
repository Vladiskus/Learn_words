package com.example.first.learnenglishwordssmart.services;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.first.learnenglishwordssmart.R;
import com.example.first.learnenglishwordssmart.activities.MainActivity;
import com.example.first.learnenglishwordssmart.databases.WordsDataBase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadDBService extends IntentService {
    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_MESSENGER = "extra_messenger";
    private static final double total = 6017024;

    public DownloadDBService() {
        super("DownloadDBService");
    }

    public static void startDownload(final MainActivity activity, String url) {
        Messenger messenger = new Messenger(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                ((ProgressBar) activity.findViewById(R.id.progressBar2))
                        .setProgress(msg.arg1);
                ((TextView) activity.findViewById(R.id.downloading))
                        .setText(String.format(activity.getString(R.string.downloading), msg.arg1));
                if (msg.arg1 == 100) {
                    activity.findViewById(R.id.download_layout).setVisibility(View.GONE);
                    activity.init();
                }
                return true;
            }
        }));
        Intent intent = new Intent(activity, DownloadDBService.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_MESSENGER, messenger);
        activity.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Messenger messenger = intent.getExtras().getParcelable(EXTRA_MESSENGER);
            URL url = new URL(intent.getExtras().getString(EXTRA_URL));
            InputStream inputStream = url.openStream();
            FileOutputStream outputStream;
            File directory = new File(getFilesDir().getAbsolutePath() + "/databases/");
            directory.mkdirs();
            File file = new File(directory, WordsDataBase.DATABASE_NAME);
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            double downloadedSize = 0;
            double lastState = 0;
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                downloadedSize += length;
                if (downloadedSize/total - lastState > 0.01 || downloadedSize/total == 1) {
                    lastState = downloadedSize/total;
                    Message message = Message.obtain();
                    message.arg1 = (int) (lastState * 100);
                    messenger.send(message);
                }
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
