package com.example.fft;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

public class ReadOut extends Thread {

    //Konfigurowanie kanału audio
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    double[] x;
    double[] y;
    double[] amplitude;
    android.app.Activity activity;
    FFT myFFT;
    MainActivity mainActivity;

    //Flaga, która wyznacza czas życia wątku
    boolean shouldRun = true;

    //Konstruktor
    public ReadOut(MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        activity = _mainActivity;
        x = _mainActivity.x;
        y = _mainActivity.y;
        amplitude = _mainActivity.amplitude;

        myFFT = new FFT(_mainActivity.blocksize);
    }
    @Override
    public void run() {
        for(int i = 0; i <mainActivity.blocksize; i++) {
            y[i] = 0;
        }
        short[] audioBuffer = new short[mainActivity.blocksize];
        int bufferSize = AudioRecord.getMinBufferSize(mainActivity.samplingFrequency, channelConfiguration, audioEncoding);

        //Sprawdzanie uprawnień
        if(ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] {android.Manifest.permission.RECORD_AUDIO}, 0);
            return;
        }
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, mainActivity.samplingFrequency, channelConfiguration, audioEncoding, bufferSize);
        audioRecord.startRecording();

        while(shouldRun) {
            //Odczytywanie danych
            int bufferReadResult = audioRecord.read(audioBuffer, 0, mainActivity.blocksize);

            for(int i = 0; i < mainActivity.blocksize && i < bufferReadResult; i++) {
                x[i] = (double) audioBuffer[i] / 32768.0;
            }
            //Obliczenia
            int newReading = ComputeFFT();
            mainActivity.temperature = (mainActivity.a * ComputeAVG(newReading)) + mainActivity.b;

            //Usypianie wątku
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        audioRecord.stop();
    }

    //Obliczanie amplitudy sygnału
    private int ComputeFFT() {
        myFFT.fft(x, y);
        int ymax = 0;
        double maxValue = Double.MIN_VALUE;

        for(int i = 0; i < mainActivity.blocksize / 2; i++) {
            amplitude[i] = x[i] * x[i] + y[i] * y[i];
            if (amplitude[i] > maxValue) {
                maxValue = amplitude[i];
                ymax = i;
            }
        }
         for (int i = 0; i < mainActivity.blocksize / 2; i++) {
             amplitude[i] = (amplitude[i] * 500) / maxValue;
         }
         return ymax;
    }
    //Obliczanie średniej ampltudy z odczytu
    private double ComputeAVG(int newReading)
    {
        //Przesuwanie pomiarów - średnia pełzająca
        for (int i = 0; i < mainActivity.readings.length - 1; i++) {
            mainActivity.readings[i] = mainActivity.readings[i + 1];
        }
        mainActivity.readings[mainActivity.readings.length - 1] = newReading;

        //Wyznaczanie średniej
        double avg = 0;
        for (int i = 0; i < mainActivity.readings.length; i++) {
            avg += mainActivity.readings[i];
        }
        return (avg / mainActivity.readings.length);
    }
}
