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

    boolean isRunning=true;

    long timeWhenStopped = 0; //temps de pause

    Button btnStop;
    Button btnStart;

    Chronometer totalChrono; //chronomètre calculant le temps total de la réunion
    Chronometer chrono; //chronomètre calculant le temps de parole de la personne écoutée

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //ATTENTION: les permissions ne sont pas gérées, il faut les activer manuellement dans les paramètres du téléphone (espace Applications)

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(); //initialisation des éléments de l'interface

        sm=new SoundMeter(MainActivity.this);
        th=new Thread(sm);

        //BOUTON START
        btnStart.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initialisation du chrono de temps total
                totalChrono.setBase(SystemClock.elapsedRealtime());
                totalChrono.start();

                //initialisation et lancement du thread de calcul du volume de la voix
                sm=new SoundMeter(MainActivity.this);
                timeWhenStopped=0;
                th=new Thread(sm);
                th.start();

                chrono.setBase(SystemClock.elapsedRealtime()); //reset temps de parole

                //interface
                btnStart.setTextSize(10);
                btnStop.setTextSize(25);
                btnStop.setEnabled(true);
                btnStart.setEnabled(false);
                btnStart.setBackgroundResource(R.drawable.round_button_start_disabled);
                btnStop.setBackgroundResource(R.drawable.round_button_stop);
            }
        });

        //BOUTON STOP
        btnStop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Arrêt des deux chronomètres
                totalChrono.stop();
                chrono.stop();
                sm.stopChrono();

                //Écriture des données dans un fichier "test.csv" à la racine du téléphone
                writeToFile(sm.getData());

                //Interface
                btnStop.setTextSize(10);
                btnStart.setTextSize(25);
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
                btnStart.setBackgroundResource(R.drawable.round_button_start);
                btnStop.setBackgroundResource(R.drawable.round_button_stop_disabled);

                //Affichage des résultats
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
                //Toast.makeText(MainActivity.this, "Temps de parole: "+chrono.getText()+", total: "+totalChrono.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void init(){
        chrono=(Chronometer) findViewById(R.id.chronometer);
        totalChrono=(Chronometer) findViewById(R.id.chronometerTotal);
        btnStop = (Button)findViewById(R.id.button2);
        btnStart = (Button)findViewById(R.id.button);
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
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), Global.pathData);
        try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data.getBytes());
        } catch(Exception e){

        }
    }
}