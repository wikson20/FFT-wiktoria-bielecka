package com.example.fft;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.widget.TextView;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

/**
 * Główna aktywność aplikacji, która wyświetla wykres sygnału oraz temperaturę.
 */

public class MainActivity extends AppCompatActivity {
    //Częstotliwość próbkowania
    int samplingFrequency = 120000;
    //Częstotliwość sygnału
    int f = 2800;
    //Rozmiar
    int blocksize = 2048;
    //Tablice, które przechowują dane
    double[] x;
    double[] y;
    double[] amplitude;
    int window = 100;
    double[] readings = new double[window];
    double temperature = 0;
    double a = 0.5;
    double b = -111.1;
    //Flaga, która oznacza stan działania programu
    boolean isRunning = false;

    //Elementy należące do interfejsu użytkownika
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    TextView tempOutputFinal;
    TextView chartMin;
    TextView chartMax;

    //Wielowątkowość
    MainActivity mainThread;
    ReadOut readoutProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Wyszukiwanie elementów interfejsu
        tempOutputFinal = findViewById(R.id.tempOutputFinal);
        chartMin = findViewById(R.id.chartMin);
        chartMax = findViewById(R.id.chartMax);

        imageView = findViewById(R.id.wykres);
        bitmap = Bitmap.createBitmap(blocksize / 2, 520, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        imageView.setImageBitmap(bitmap);
        canvas.drawColor(Color.RED);

        Button buttonStart = findViewById(R.id.ButtonStart);

        mainThread = this;

        //Ustawienie listenerów
        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!isRunning) {
                    buttonStart.setText("Stop");

                    readoutProcess = new ReadOut(mainThread);
                    readoutProcess.start();
                } else {
                    readoutProcess.shouldRun = false;
                    canvas.drawColor(Color.BLACK);
                    buttonStart.setText("Start");
                }

                isRunning = !isRunning;

            }
        });

        //Inicjalizacja tablic
        x = new double[blocksize];
        y = new double[blocksize];
        amplitude = new double[blocksize / 2];

        //Rysowanie wykresu
        DrawChart();
    }
    //Metoda rysująca wykres
    public void DrawChart()
    {
        canvas.drawColor(Color.BLACK);

        // Rysowanie danych amplitudy na zielono
        paint.setColor(Color.GREEN);
        for (int i = 0; i < amplitude.length; i++) {
            int downy = 510;
            int upy = 510 - (int) amplitude[i];

            canvas.drawLine(i, downy, i, upy, paint);
        }

        // Rysowanie linii siatki na czerwono
        paint.setColor(Color.RED);
        for (int i = 0; i < amplitude.length; i++) {
            if (i % 10 == 0) {
                canvas.drawLine(i, 510, i, 500, paint);
            }
            if (i % 100 == 0) {
                canvas.drawLine(i, 510, i, 475, paint);
            }
        }
        // Aktualizacja etykiet wykresu
        chartMin.setText("0");
        chartMax.setText(String.valueOf(amplitude.length));

        // Aktualizacja wyświetlania temperatury
        tempOutputFinal.setText(String.format("%.2f", temperature) + " C");
        Refresh(500); // Odświeżanie wykresu co 500 ms
    }
    // Metoda odświeżająca wykres po określonym czasie.
    public void Refresh(int millis) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DrawChart();
            }
        };
        handler.postDelayed(runnable, millis);
    }







}