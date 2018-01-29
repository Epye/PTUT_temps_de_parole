package fr.lannier.iem.record_audio_test;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by iem on 17/01/2018.
 */

public class MainActivity extends AppCompatActivity {
    SoundMeter sm;
    Thread th;
    Chronometer chrono;
    boolean isRunning=true;
    long timeWhenStopped = 0;
    Button btnStop;
    Button btnStart;
    Chronometer totalChrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chrono=(Chronometer) findViewById(R.id.chronometer);
        totalChrono=(Chronometer) findViewById(R.id.chronometerTotal);
        sm=new SoundMeter(MainActivity.this);
        th=new Thread(sm);


        btnStart = (Button)findViewById(R.id.button);
        btnStart.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalChrono.setBase(SystemClock.elapsedRealtime());
                totalChrono.start();
                sm=new SoundMeter(MainActivity.this);
                timeWhenStopped=0;
                th=new Thread(sm);
                th.start();
                chrono.setBase(SystemClock.elapsedRealtime());
                btnStart.setTextSize(10);
                btnStop.setTextSize(25);
                btnStop.setEnabled(true);
                btnStart.setEnabled(false);
                btnStart.setBackgroundResource(R.drawable.round_button_start_disabled);
                btnStop.setBackgroundResource(R.drawable.round_button_stop);
            }
        });

        btnStop = (Button)findViewById(R.id.button2);
        btnStop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalChrono.stop();
                chrono.stop();
                writeToFile(sm.getData());
                sm.stopChrono();
                btnStop.setTextSize(10);
                btnStart.setTextSize(25);
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
                btnStart.setBackgroundResource(R.drawable.round_button_start);
                btnStop.setBackgroundResource(R.drawable.round_button_stop_disabled);

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Temps de parole: "+chrono.getText()+"\n\nDurée totale: "+totalChrono.getText());
                builder.setTitle("Récapitulatif");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                Toast.makeText(MainActivity.this, "Temps de parole: "+chrono.getText()+", total: "+totalChrono.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Stop(){

        if(isRunning) {
            chrono.stop();
            timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
            isRunning = false;
        }
    }
    public void Resume(){
        if(!isRunning) {
            chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            chrono.start();
            isRunning = true;
        }
    }

    private void writeToFile(String data) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.csv");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
        } catch(Exception e){

        }
    }
}