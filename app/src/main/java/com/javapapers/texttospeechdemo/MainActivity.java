package com.javapapers.texttospeechdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_WRITE_STORAGE_REQUEST_CODE =200 ;
    private TextToSpeech textToSpeech;

    private Handler threadHandler = new Handler();
    private int mStatus = 0;
    private MediaPlayer mMediaPlayer;
    private boolean mProcessed = false;
    private final String FILENAME = "/wpta_tts.wav";
    private ProgressDialog mProgressDialog;
    private EditText etContent;

    private SeekBar mSeekBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAppPermissions();

        // Instantiating TextToSpeech class
        textToSpeech = new TextToSpeech(this, this);

        this.mSeekBar= (SeekBar) this.findViewById(R.id.sekbDuracao);
        this.mSeekBar.setClickable(false);



        // Getting reference to the button btn_speek
        Button btnSpeek = (Button) findViewById(R.id.btn_speak);

        // Creating a progress dialog window
        mProgressDialog = new ProgressDialog(this);

        // Creating an instance of MediaPlayer
        mMediaPlayer = new MediaPlayer();

        etContent = (EditText) findViewById(R.id.edt_text);
        // Close the dialog window on pressing back button
        mProgressDialog.setCancelable(true);

        // Setting a horizontal style progress bar
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        /** Setting a message for this progress dialog
         * Use the method setTitle(), for setting a title
         * for the dialog window
         *  */
        mProgressDialog.setMessage("Por favor, aguarde ...");

        // Defining click event listener for the button btn_speak
        View.OnClickListener btnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(mStatus==TextToSpeech.SUCCESS){

                    // Getting reference to the Button
                    Button btnSpeak = (Button) findViewById(R.id.btn_speak);

                    btnSpeak.setText("Pause");

                    if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                        playMediaPlayer(1);
                        btnSpeak.setText("Speak");
                        return;
                    }

                    mProgressDialog.show();

                    // Getting reference to the EditText et_content

                    HashMap<String, String> myHashRender = new HashMap();
                    String utteranceID = "wpta";
                    myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);

                    String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + FILENAME;
                    File soundFile = new File(fileName);
                    if (soundFile.exists())
                        soundFile.delete();


                    if(!mProcessed){
                        //textToSpeech.getMaxSpeechInputLength();


                        int status = textToSpeech.synthesizeToFile(etContent.getText().toString(), myHashRender, fileName);

                        Log.d("STATUS", "OK " + status);
                    }else{
                        playMediaPlayer(0);
                    }
                }else{
                    String msg = "TextToSpeech Engine is not initialized";
                    Toast.makeText(getBaseContext(),msg, Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Set Click event listener for the button btn_speak
        btnSpeek.setOnClickListener(btnClickListener);

        // Getting reference to the EditText et_content
        EditText etContent = (EditText) findViewById(R.id.edt_text);
        //etContent.setText(getStringFromFile());


        etContent.setText(new ReadFileAsync().execute().toString());

        etContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                mProcessed=false;
                mMediaPlayer.reset();

                // Getting reference to the button btn_speek
                Button btnSpeek = (Button) findViewById(R.id.btn_speak);

                // Changing button Text to Speek
                btnSpeek.setText("Speak");
            }
        });

        MediaPlayer.OnCompletionListener mediaPlayerCompletionListener = new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // Getting reference to the button btn_speek
                Button btnSpeek = (Button) findViewById(R.id.btn_speak);

                // Changing button Text to Speek
                btnSpeek.setText("Speak");
            }
        };

        mMediaPlayer.setOnCompletionListener(mediaPlayerCompletionListener);
    }


    @Override
    protected void onDestroy() {

        // Stop the TextToSpeech Engine
        textToSpeech.stop();

        // Shutdown the TextToSpeech Engine
        textToSpeech.shutdown();

        // Stop the MediaPlayer
        mMediaPlayer.stop();

        // Release the MediaPlayer
        mMediaPlayer.release();

        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {


            int result = textToSpeech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS", "This Language is not supported");
            }

        }
        mStatus = status;


            if (status==0){
            Toast.makeText(getApplicationContext(),
                    "There is no TTS engine on your device", Toast.LENGTH_LONG).show();
           // finish();
//            Intent install = new Intent();
//            install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//            startActivity(install);

        }

        setTts(this.textToSpeech);
    }


    public void setTts(TextToSpeech tts) {
        this.textToSpeech = tts;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            this.textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId){
                    // Speech file is created
                    mProcessed = true;

                    // Initializes Media Player
                    initializeMediaPlayer();

                    // Start Playing Speech
                    playMediaPlayer(0);


                    Log.d("STATUS", "DONE: " + utteranceId );

                }

                @Override
                public void onError(String utteranceId){
                    Log.d("STATUS", "ERROR: " + utteranceId );

                }

                @Override
                public void onStart(String utteranceId){
                }
            });
        }else{
            this.textToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    // Speech file is created
                    mProcessed = true;

                    // Initializes Media Player
                    initializeMediaPlayer();

                    // Start Playing Speech
                    playMediaPlayer(0);

                    Log.d("STATUS", "onUtteranceCompleted: " + utteranceId );
                }
            });
        }
    }


    private void initializeMediaPlayer(){
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + FILENAME;

        //Uri uri  = Uri.parse("file://"+fileName);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {

            FileInputStream is = new FileInputStream(fileName);
            mMediaPlayer.setDataSource(is.getFD());
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMediaPlayer(int status){
        mProgressDialog.dismiss();

        // Start Playing
        if(status==0){
            //mMediaPlayer.start();
            doStart();
        }

        // Pause Playing
        if(status==1){
            mMediaPlayer.pause();
        }
    }

    public void doStart(){
        int duration = mMediaPlayer.getDuration();
        int currentPosition = mMediaPlayer.getCurrentPosition();
        if(currentPosition== 0) {
            mSeekBar.setMax(duration);
            String maxTimeString = this.millisecondsToString(duration);
            //this.textMaxTime.setText(maxTimeString);
        } else if(currentPosition== duration){
        // Resets the MediaPlayer to its uninitialized state.
            mMediaPlayer.reset();
        }
        mMediaPlayer.start();
        // Create a thread to update position of SeekBar.
        UpdateSeekBarThread updateSeekBarThread= new UpdateSeekBarThread();
        threadHandler.postDelayed(updateSeekBarThread,50);
        //this.buttonPause.setEnabled(true);
        //this.buttonStart.setEnabled(false);

    }


    // Convert millisecond to string.
    private String millisecondsToString(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes((long) milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((long) milliseconds) ;
        return minutes+":"+ seconds;
    }






    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }



    public class ReadFileAsync extends AsyncTask<Void, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(Void... params) {

            StringBuilder response = new StringBuilder();
            try {

                URL url = new URL("https://concursosnamaohospedagem.xyz/TXT/Atos%20Administrativo.txt");
                //Log.d("URL",url.toString());
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        url.openStream(),"Cp1252"));
                String line = null;
                while ((line = in.readLine()) != null) {
                    // get lines
                    response.append(line);
                    response.append('\n');

                }
                in.close();
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return response.toString();
        }

        protected void onProgressUpdate() {
            // called when the background task makes any progress
        }



        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getApplicationContext());
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progressDialog.dismiss();

            Log.d("tag", "Response is " + result);
            //set your Textview here
            etContent.setText(result);
        }
    }


    class UpdateSeekBarThread implements Runnable {

        @Override
        public void run() {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            String currentPositionStr = millisecondsToString(currentPosition);
            //textCurrentPosition.setText(currentPositionStr);

            mSeekBar.setProgress(currentPosition);
            // Delay thread 50 milisecond.
            threadHandler.postDelayed(this, 50);
        }
    }




}
