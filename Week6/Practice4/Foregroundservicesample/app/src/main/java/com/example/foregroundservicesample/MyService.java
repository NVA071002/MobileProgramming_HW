package com.example.foregroundservicesample;

import static com.example.foregroundservicesample.MyApplication.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    private MediaPlayer mediaPlayer;
    @Nullable
    private static final int ACION_PAUSE = 1;
    private static final int ACION_RESUME = 2;
    private static final int ACION_CLEAR = 3;
    private boolean isPlaying;
    private  Song mSong;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("VietAnh", "MyService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Song song = (Song) bundle.get("object_song");

            if (song != null) {
                mSong = song;
                startMusic(song);
                sendNotification(song);
            }
        }
        int actionMusic = intent.getIntExtra("action_music_service",0);
        handleActionMusic(actionMusic);

        return START_NOT_STICKY;

    }

    private void startMusic(Song song) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), song.getResource());
        }
        mediaPlayer.start();
        isPlaying = true;
    }

    private void handleActionMusic(int action) {
        switch (action) {
            case ACION_PAUSE:
                pauseMusic();
                break;
            case ACION_RESUME:
                resumeMusic();
                break;
            case ACION_CLEAR:
                stopSelf();
                break;
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            sendNotification(mSong);
        }

    }

    private void resumeMusic() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            sendNotification(mSong);
        }

    }

    private void sendNotification(Song song) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), song.getImage());

        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.layout_custom_notification);
        remoteView.setTextViewText(R.id.tv_title_song, song.getTitle());
        remoteView.setTextViewText(R.id.tv_singer_song, song.getSinger());
        remoteView.setImageViewBitmap(R.id.img_song, bitmap);

        remoteView.setImageViewResource(R.id.img_play_or_pause, R.drawable.ic_pause);

        if (isPlaying) {
            remoteView.setOnClickPendingIntent(R.id.img_play_or_pause, getPendingIntent(this, ACION_PAUSE));
            remoteView.setImageViewResource(R.id.img_play_or_pause, R.drawable.ic_pause);
        } else {
            remoteView.setOnClickPendingIntent(R.id.img_play_or_pause, getPendingIntent(this, ACION_RESUME));
            remoteView.setImageViewResource(R.id.img_play_or_pause, R.drawable.ic_play);
        }
        remoteView.setOnClickPendingIntent(R.id.img_clear, getPendingIntent(this, ACION_CLEAR));


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteView)
                .setSound(null)
                .build();

        startForeground(1, notification);
    }

    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("action_music", action);
        return PendingIntent.getBroadcast(context.getApplicationContext(),action,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("VietAnh", "MyService onDestroy");

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }
}
