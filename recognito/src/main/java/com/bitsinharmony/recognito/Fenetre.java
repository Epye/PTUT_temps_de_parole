package com.bitsinharmony.recognito;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Fenetre extends JFrame implements ActionListener{

    private JButton buttonStart;
    private JButton buttonStop;
    private JButton buttonInitialize;
    private JButton buttonInitRecognito;
    private JButton buttonStopInitialize;
    private JButton buttonDelineRecognition;
    private JButton buttonJohanRecognition;
    private JLabel labelChrono;
    private JLabel labelChronoReco;
    private JLabel labelInit;
    private JLabel labelConv;
    private JLabel labelReferent;
    private JLabel labelDelineRecognition;
    private JLabel labelJohanRecognition;
    private Chrono chronoReco;
    private JPanel pan;
    private Timer timer;
    private Timer timerChrono;
    private TimerTask taskRepeat;
    private TimerTask taskRepeatChrono;
    private TimerTask taskInit;
    private AudioFormat audioFormat;
    private DataLine.Info info;
    private TargetDataLine line;
    private String previousFile;
    private String currentFile;
    private AudioInputStream audioInputStream;
    private AudioFileFormat.Type fileType;
    private Recognito<String> recognito;
    private List<MatchResult<String>> matches;
    private String tmpChrono;
    private CaptureThread thread;
    private int tempsCapture = 10;

    public Fenetre(){
        initLayout();
        initTask();
        initVar();
    }

    private void initTask(){
        taskRepeat = new TimerTask() {
            @Override public void run() {
                System.out.println("COPIE");
                line.stop();
                line.close();
                thread.interrupt();
                String tmp = currentFile;
                currentFile = previousFile;
                previousFile = tmp;
                try {
                    verifVoice();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
                thread = new CaptureThread();
                thread.start();
            }
        };

        taskRepeatChrono = new TimerTask() {
            @Override public void run() {
                chronoReco.pause();
                labelChronoReco.setText(chronoReco.getDureeTxt());
                if(chronoReco.getDureeSec() > 30){
                    buttonInitRecognito.setEnabled(true);
                }
                chronoReco.resume();
            }
        };

        taskInit = new TimerTask() {
            @Override public void run() {
                line.stop();
                line.close();
                buttonStart.setEnabled(true);
                buttonInitialize.setEnabled(true);
                buttonStopInitialize.setEnabled(false);
                chronoReco.stop();
            }
        };
    }

    private void initLayout(){
        //Instanciation d'un objet JPanel
        pan = new JPanel();
        pan.setLayout(new GridBagLayout());
        GridBagConstraints cont = new GridBagConstraints();
        cont.fill = GridBagConstraints.BOTH;

        buttonStart = new JButton("START");
        buttonStart.addActionListener(this);
        buttonStart.setEnabled(false);

        buttonInitialize = new JButton("Initialiser");
        buttonInitialize.addActionListener(this);

        buttonInitRecognito = new JButton("Initialiser Recognito");
        buttonInitRecognito.addActionListener(this);

        buttonStopInitialize = new JButton("STOP");
        buttonStopInitialize.setEnabled(false);
        buttonStopInitialize.addActionListener(this);

        buttonStop = new JButton("STOP");
        buttonStop.setEnabled(false);
        buttonStop.addActionListener(this);

        buttonDelineRecognition = new JButton("Recognito with Adeline");
        buttonDelineRecognition.addActionListener(this);
        buttonDelineRecognition.setEnabled(false);

        buttonJohanRecognition = new JButton("Recognito with Johan");
        buttonJohanRecognition.addActionListener(this);
        buttonJohanRecognition.setEnabled(false);

        labelInit = new JLabel("Initialisation");
        labelConv = new JLabel("Lancer la Conv");
        labelChrono = new JLabel("0");
        labelChronoReco = new JLabel("0s");
        labelDelineRecognition = new JLabel("...");
        labelJohanRecognition = new JLabel("...");
        labelReferent = new JLabel("...");

        cont.gridx=1;
        cont.gridy=0;
        pan.add(labelInit, cont);

        cont.gridx=0;
        cont.gridy=1;
        pan.add(buttonInitialize, cont);

        cont.gridx = 2;
        pan.add(buttonStopInitialize, cont);

        cont.gridx = 3;
        pan.add(buttonInitRecognito, cont);

        cont.gridx = 1;
        cont.gridy = 2;
        pan.add(labelChronoReco, cont);

        cont.gridy=3;
        cont.gridx=1;
        pan.add(labelConv, cont);

        cont.gridy = 4;
        cont.gridx = 0;
        pan.add(buttonStart, cont);

        cont.gridx = 2;
        pan.add(buttonStop, cont);

        cont.gridx = 3;
        pan.add(labelReferent, cont);

        cont.gridy = 5;
        cont.gridx = 1;
        pan.add(labelChrono, cont);

        cont.gridy = 6;
        cont.gridx = 0;
        pan.add(buttonDelineRecognition, cont);

        cont.gridx = 2;
        pan.add(buttonJohanRecognition, cont);

        cont.gridy = 7;
        cont.gridx = 1;
        pan.add(labelDelineRecognition, cont);

        cont.gridx = 3;
        pan.add(labelJohanRecognition, cont);

        this.setTitle("Recognito");
        this.setSize(700, 250);
        this.setLocationRelativeTo(null);
        this.setContentPane(pan);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setVisible(true);
    }

    private void initVar(){
        currentFile = "fichier1.wav";
        previousFile = "fichier2.wav";
        fileType = AudioFileFormat.Type.WAVE;
        tmpChrono = "0";
        chronoReco = new Chrono();
        timer = new Timer();
        timerChrono = new Timer();
        timerChrono.scheduleAtFixedRate(taskRepeatChrono, 0, 1000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == buttonStart){
            currentFile = "fichier1.wav";
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
            initTask();
            captureAudio();
            timer = new Timer();
            timer.scheduleAtFixedRate(taskRepeat, tempsCapture*1000, tempsCapture*1000);
        }else if(e.getSource() == buttonStop){
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
            timer.cancel();
            line.stop();
            line.close();
            thread.interrupt();
        } else if (e.getSource() == buttonInitialize){
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(false);
            buttonInitRecognito.setEnabled(false);
            buttonInitialize.setEnabled(false);
            buttonStopInitialize.setEnabled(true);
            currentFile = "referent.wav";
            captureAudio();
            chronoReco.start();
            initTask();
            timer = new Timer();
            timer.schedule(taskInit, 60000);
        } else if (e.getSource() == buttonStopInitialize){
            buttonInitialize.setEnabled(true);
            buttonStart.setEnabled(true);
            buttonStopInitialize.setEnabled(false);
            chronoReco.stop();
            line.stop();
            line.close();
            timer.cancel();
        } else if (e.getSource() == buttonInitRecognito){
            initRecognito();
            buttonStart.setEnabled(true);
            buttonDelineRecognition.setEnabled(true);
            buttonJohanRecognition.setEnabled(true);
        } else if (e.getSource() == buttonDelineRecognition){
            try {
                matches = recognito.identify(new File("Voice/Deline10.wav"));
                labelDelineRecognition.setText(matches.get(0).getKey() + " " + matches.get(0).getLikelihoodRatio() + "%");
            } catch (UnsupportedAudioFileException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if (e.getSource() == buttonJohanRecognition){
            try {
                matches = recognito.identify(new File("Voice/Johan30.wav"));
                labelJohanRecognition.setText(matches.get(0).getKey() + " " + matches.get(0).getLikelihoodRatio() + "%");
            } catch (UnsupportedAudioFileException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void initRecognito(){
        recognito = new Recognito(44100.0f);

        try {
            VoicePrint print1 = recognito.createVoicePrint("Referent", new File("referent.wav"));

            VoicePrint print2 = recognito.createVoicePrint("Brian", new File("Voice/brian.wav"));
            VoicePrint print3 = recognito.createVoicePrint("Nico", new File("Voice/Nico.wav"));
            VoicePrint print4 = recognito.createVoicePrint("Johan", new File("Voice/Johan.wav"));
            VoicePrint print5 = recognito.createVoicePrint("Gaga", new File("Voice/Gaga.wav"));
            VoicePrint print6 = recognito.createVoicePrint("Vanessa", new File("Voice/Vanessa.wav"));
            VoicePrint print7 = recognito.createVoicePrint("Femme1", new File("Voice/Femme1.wav"));
            VoicePrint print8 = recognito.createVoicePrint("Femme2", new File("Voice/Femme2.wav"));
            //VoicePrint print9 = recognito.createVoicePrint("Deline", new File("Voice/Deline.wav"));
            VoicePrint print10 = recognito.createVoicePrint("Boutin", new File("Voice/Boutin.wav"));
            VoicePrint print13 = recognito.createVoicePrint("Femme5", new File("Voice/Femme5.wav"));
            VoicePrint print14 = recognito.createVoicePrint("Femme6", new File("Voice/Femme6.wav"));
            VoicePrint print15 = recognito.createVoicePrint("Femme7", new File("Voice/Femme7.wav"));
            VoicePrint print16 = recognito.createVoicePrint("Femme8", new File("Voice/Femme8.wav"));
            VoicePrint print17 = recognito.createVoicePrint("Femme9", new File("Voice/Femme9.wav"));
            VoicePrint print18 = recognito.createVoicePrint("Femme10", new File("Voice/Femme10.wav"));
            VoicePrint print19 = recognito.createVoicePrint("Femme11", new File("Voice/Femme11.wav"));
            VoicePrint print20 = recognito.createVoicePrint("Femme12", new File("Voice/Femme12.wav"));
            VoicePrint print21 = recognito.createVoicePrint("Femme13", new File("Voice/Femme13.wav"));
            VoicePrint print22 = recognito.createVoicePrint("Femme14", new File("Voice/Femme14.wav"));
            VoicePrint print23 = recognito.createVoicePrint("Femme15", new File("Voice/Femme15.wav"));
            VoicePrint print24 = recognito.createVoicePrint("Femme16", new File("Voice/Femme16.wav"));
            VoicePrint print25 = recognito.createVoicePrint("Femme17", new File("Voice/Femme17.wav"));
            VoicePrint print26 = recognito.createVoicePrint("Femme18", new File("Voice/Femme18.wav"));
            VoicePrint print27 = recognito.createVoicePrint("Femme19", new File("Voice/Femme19.wav"));
            VoicePrint print28 = recognito.createVoicePrint("Femme20", new File("Voice/Femme20.wav"));
            VoicePrint print29 = recognito.createVoicePrint("Homme4", new File("Voice/Homme4.wav"));
            VoicePrint print30 = recognito.createVoicePrint("Homme5", new File("Voice/Homme5.wav"));
            VoicePrint print31 = recognito.createVoicePrint("Homme6", new File("Voice/Homme6.wav"));
            VoicePrint print32 = recognito.createVoicePrint("Homme7", new File("Voice/Homme7.wav"));
            VoicePrint print33 = recognito.createVoicePrint("Homme8", new File("Voice/Homme8.wav"));
            VoicePrint print34 = recognito.createVoicePrint("Homme9", new File("Voice/Homme9.wav"));
            VoicePrint print35 = recognito.createVoicePrint("Homme10", new File("Voice/Homme10.wav"));
            VoicePrint print36 = recognito.createVoicePrint("Homme11", new File("Voice/Homme11.wav"));
            VoicePrint print37 = recognito.createVoicePrint("Homme12", new File("Voice/Homme12.wav"));
            VoicePrint print38 = recognito.createVoicePrint("Homme13", new File("Voice/Homme13.wav"));
            VoicePrint print39 = recognito.createVoicePrint("Homme14", new File("Voice/Homme14.wav"));
            VoicePrint print40 = recognito.createVoicePrint("Homme15", new File("Voice/Homme15.wav"));
            VoicePrint print41 = recognito.createVoicePrint("Homme16", new File("Voice/Homme16.wav"));
            VoicePrint print42 = recognito.createVoicePrint("Homme17", new File("Voice/Homme17.wav"));
            VoicePrint print43 = recognito.createVoicePrint("Homme18", new File("Voice/Homme18.wav"));
            VoicePrint print44 = recognito.createVoicePrint("Homme19", new File("Voice/Homme19.wav"));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void captureAudio(){
        try{
            //Get things set up for capture
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            thread = new CaptureThread();
            thread.start();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }//end catch
    }//end captureAudio method

    private AudioFormat getAudioFormat(){
        float sampleRate = 44100f;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 2;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = true;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }//end getAudioFormat

    class CaptureThread extends Thread{
        public void run(){
            try{
                line.open(audioFormat);
                line.start();
                audioInputStream=new AudioInputStream(line);
                AudioSystem.write(audioInputStream, fileType, new File(currentFile));
            }catch (Exception e){
                e.printStackTrace();
            }//end catch

        }//end run
    }//end inner class CaptureThread

    private void verifVoice() throws IOException, UnsupportedAudioFileException {
        matches = recognito.identify(new File(previousFile));
        labelReferent.setText(matches.get(0).getKey() + " " + matches.get(0).getLikelihoodRatio() + "%");
        if(matches.get(0).getKey().equals("Referent") && matches.get(0).getLikelihoodRatio() > 90) {
            int tmp = Integer.parseInt(tmpChrono);
            tmp += tempsCapture;
            tmpChrono = Integer.toString(tmp);
            labelChrono.setText(tmpChrono);
        }
    }
}
