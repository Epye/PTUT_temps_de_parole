package fr.lannier.iem.record_audio_test;

import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Created by iem on 17/01/2018.
 */

public class SoundMeter implements Runnable {

    private MediaRecorder mRecorder = null;
    String data="";
    Boolean isRunning=false;
    MainActivity ctx;

    int cpt=0;

    public SoundMeter(MainActivity ctx) {
        this.ctx=ctx;
    }

    public void startChrono() {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //format 3gp
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null"); //le fichier son n'est pas enregistré sur l'appareil
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
            isRunning=true;
    }

    public void stopChrono() {
        if (mRecorder != null) {
            mRecorder.stop();
            isRunning=false;
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;

    }

    public String getData(){
        data=data.replace(".",",");
        return data;
    }

    @Override
    public void run() {
        this.startChrono();
        while (isRunning){
            double tmp=this.getAmplitude();
            data+=tmp+"\n"; //sauvegarde des données
            if(tmp>Global.seuilVolume){
                ctx.runOnUiThread(new Runnable() {
                    public void run() {
                        ctx.Resume();
                    }
                });
                cpt=0;
                System.out.println(tmp);
            }else{
                cpt++;
                if(cpt>Global.intervalle){
                    ctx.runOnUiThread(new Runnable() {
                        public void run() {
                            ctx.Stop();
                        }
                    });
                    cpt=0;
                }
            }
        }
    }
}