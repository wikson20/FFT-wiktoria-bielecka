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
    final int samplingFrequency = 120000;
    //Częstotliwość sygnału
    final int f = 2800;
    // Rozmiar bloku danych dla FFT
    final int blocksize = 2048;
    // Rozmiar okna do przechowywania odczytów temperatury
    final int window = 25;


    //Tablice, które przechowują dane sygnału
    double[] x;
    double[] y;
    double[] amplitude;
    double[] readings = new double[window];


    // Zmienna przechowująca obliczoną temperaturę
    double temperature = 0;
    // Stałe do obliczania temperatury
    final double a = 0.8056;
    final double b = - 32.21;
    //Flaga, która oznacza stan działania programu
    boolean isRunning = false;

    //Elementy należące do interfejsu użytkownika
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    TextView tempOutputFinal;

    //Wielowątkowość
    MainActivity mainThread;
    ReadOut readoutProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicjalizacja tablic
        x = new double[blocksize];
        y = new double[blocksize];
        amplitude = new double[blocksize / 2];

        // Inicjalizacja elementów interfejsu
        tempOutputFinal = findViewById(R.id.tempOutputFinal);

        imageView = findViewById(R.id.wykres);
        bitmap = Bitmap.createBitmap(blocksize / 2, 520, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        imageView.setImageBitmap(bitmap);

        Button buttonStart = findViewById(R.id.ButtonStart);

        mainThread = this;

        //Ustawienie listenerów
        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!isRunning) {
                    //uruchomienie nowego wątku pobierającego pomiary
                    readoutProcess = new ReadOut(mainThread);
                    readoutProcess.start();
                    //Aktualizacja UI
                    buttonStart.setText("Stop");
                } else {
                    //Zmiana flagi procesu w celu jego wyłączenia
                    if(readoutProcess != null) {
                        readoutProcess.shouldRun = false;
                    }

                    //Aktualizacja UI
                    canvas.drawColor(Color.BLACK);
                    buttonStart.setText("Start");
                }

                isRunning = !isRunning;

            }
        });

        //Rysowanie wykresu
        DrawChart();
    }
    //Metoda rysująca wykres
    public void DrawChart()
    {
        //Czyszczenie - zamalowywanie na czarno
        canvas.drawColor(Color.BLACK);

        // Rysowanie danych amplitudy na niebiesko
        paint.setColor(Color.BLUE);
        for (int i = 0; i < amplitude.length; i++) {
            int downy = 510;
            int upy = 510 - (int) amplitude[i];

            canvas.drawLine(i, downy, i, upy, paint);
        }

        // Rysowanie linii siatki na różowo
        paint.setColor(Color.MAGENTA);
        for (int i = 0; i < amplitude.length; i++) {
            //Krótka linia co 10
            if (i % 10 == 0) {
                canvas.drawLine(i, 510, i, 500, paint);
            }
            //Długa linia co 100
            if (i % 100 == 0) {
                canvas.drawLine(i, 510, i, 450, paint);
            }
        }

        // Aktualizacja wyświetlania temperatury
        tempOutputFinal.setText(String.format("%.3f", temperature) + " C");
        Refresh(500); // Handler który wywoła tą funkcję za 500ms
    }
    // Metoda odświeżająca wykres po określonym czasie.
    public void Refresh(int millis) {
        //Handler uruchamiający metodę po 'millis' czasu
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