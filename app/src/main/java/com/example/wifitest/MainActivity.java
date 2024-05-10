package com.example.wifitest;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private AudioTrack audioTrack;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initAudio();
        playToneBasedOnWifiSignal();
    }

    private void initAudio() {
        int sampleRate = 44100;
        int minSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    private void playToneBasedOnWifiSignal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        int rssi = wifiInfo.getRssi();
                        int frequency = calculateFrequency(rssi);
                        playTone(frequency, 500); // Play for 500 ms
                    }
                    try {
                        Thread.sleep(1000); // Check every second
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private int calculateFrequency(int rssi) {
        // This is just an example formula and might need tuning
        return 440 + (Math.max(rssi, -100) + 100) * 2; // A4 is 440 Hz
    }

    private void playTone(int frequency, int durationMs) {
        int sampleRate = 44100;
        double dFreq = ((double) frequency) / sampleRate;
        short[] buffer = new short[sampleRate * durationMs / 1000];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (short) (Math.sin(2 * Math.PI * i * dFreq) * Short.MAX_VALUE);
        }
        audioTrack.write(buffer, 0, buffer.length);
    }
}
