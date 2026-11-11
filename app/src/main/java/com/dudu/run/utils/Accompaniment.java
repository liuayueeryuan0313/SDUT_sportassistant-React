package com.dudu.run.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
public final class Accompaniment {
    private static Accompaniment f1619d;
    private Context a;
    private MediaPlayer b;
    private AudioManager f1620c;

    public enum RingerMode {
        SILENT,
        VIBRATE,
        NORMAL
    }

    private Accompaniment() {
    }

    public static Accompaniment a() {
        if (f1619d == null) {
            f1619d = new Accompaniment();
        }
        return f1619d;
    }
    public /* synthetic */ void d(MediaPlayer mediaPlayer) {
        e();
    }

    private synchronized void e() {
        MediaPlayer mediaPlayer = this.b;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            this.b.stop();
        }
        MediaPlayer mediaPlayer2 = this.b;
        if (mediaPlayer2 != null) {
            mediaPlayer2.release();
            this.b = null;
        }
    }

    private final synchronized boolean h(int i, boolean z) {
        if (this.f1620c == null) {
            this.f1620c = (AudioManager) this.a.getSystemService("audio");
        }
        if (this.f1620c.getStreamVolume(3) == 0) {
            return false;
        }
        if (!z && f() != RingerMode.NORMAL) {
            return false;
        }
        e();
        if (this.b == null) {
            MediaPlayer mediaPlayerCreate = MediaPlayer.create(this.a, i);
            this.b = mediaPlayerCreate;
            mediaPlayerCreate.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.dudu.run.utils.a
                @Override // android.media.MediaPlayer.OnCompletionListener
                public final void onCompletion(MediaPlayer mediaPlayer) {
                    Accompaniment.this.d(mediaPlayer);
                }
            });
            if (this.b == null) {
                return false;
            }
        }
        this.b.start();
        return true;
    }

    public void b(Context context) {
        this.a = context;
    }

    public RingerMode f() {
        if (this.f1620c == null) {
            this.f1620c = (AudioManager) this.a.getSystemService("audio");
        }
        return RingerMode.values()[this.f1620c.getRingerMode()];
    }

    public boolean g(int i) {
        return h(i, false);
    }
}
