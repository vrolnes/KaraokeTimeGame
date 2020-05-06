package com.example.karaoketime;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends YouTubeBaseActivity {
    //TEXT TO SPEECH VOICE CONTROL VARIALABLES
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecog;
    //TEXT TO SPEECH VOICE CONTROL VARIALABLES

    private YouTubePlayerView myoutube;
    YouTubePlayer.OnInitializedListener moninit;

    private TextView linkyazisi;//link yazısı
    private TextView sarkiyazisi;//sarki yazısı

    private TextView seskayitcikti;
    private TextView sarkisözü;
    private FloatingActionButton seskaydi;
    private int position = 0;

    private EditText linkname;//link to song
    private EditText songn; //song name for api

    private String lyric; // lyric itself

    List<String> unchangedlines = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start button
        TextView startt = findViewById(R.id.startt);
        myoutube = findViewById(R.id.oynatıcı);
        linkname = findViewById(R.id.lyazı);
        songn = findViewById(R.id.songn);
        linkyazisi = findViewById(R.id.linkk);
        sarkiyazisi = findViewById(R.id.textView);
        seskayitcikti = findViewById(R.id.seskayitcikti);
        sarkisözü = findViewById(R.id.sarkisözü);
        seskaydi = findViewById(R.id.seskaydi);

        //TEXT TO SPEECH VOICE CONTROL PART
        FloatingActionButton fab = findViewById(R.id.seskaydi);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });
        initializeTextToSpeech();
        initializeSpeechRecognizer();
        //TEXT TO SPEECH VOICE CONTROL PART

        moninit = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(linkname.getText().toString());

                String tmp = songn.getText().toString(); //get song name

                //convert song name into api parameter format
                String[] sns = tmp.split("\\s+");
                tmp = "";
                for (int i = 0; i < sns.length - 1; i++) {
                    tmp += sns[i];
                    tmp += "%2520";
                }
                tmp += sns[sns.length - 1];
                //convert song name into api parameter format


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://canarado-lyrics.p.rapidapi.com/lyrics/" + tmp)
                        .get()
                        .addHeader("x-rapidapi-host", "canarado-lyrics.p.rapidapi.com")
                        .addHeader("x-rapidapi-key", "9de60e07fdmsh3a897439aeccf37p176da5jsn7a547ef577fc")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String myResponse = response.body().string();
                            //get the lyrics
                            int sindex = myResponse.indexOf("lyrics");
                            int eindex = myResponse.indexOf("artist");
                            lyric = myResponse.substring(sindex + 9, eindex - 2);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void run() {
                                    //Start showing on screen
                                    settxt();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        startt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myoutube.initialize(YoutubeConfig.getApi(), moninit);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void settxt() {
        linkyazisi.setVisibility(View.GONE);
        linkname.setVisibility(View.GONE);
        sarkiyazisi.setVisibility(View.GONE);
        songn.setVisibility(View.GONE);

        String onscreen = lyric;//back up lyrics

        //remove unnecesseary characters and beautify the lyrics
        onscreen = onscreen.toLowerCase();
        onscreen = onscreen.replace(",", "");
        onscreen = onscreen.replace("?", "");
        onscreen = onscreen.replace("!", "");
        onscreen = onscreen.replace("\\n", System.getProperty("line.separator"));
        //remove unnecesseary characters and beautify the lyrics

        //get each line to arraylist
        String[] arrOfStr = onscreen.split(System.getProperty("line.separator"));
        List<String> strlist = new ArrayList<String>(Arrays.asList(arrOfStr));
        List<String> unchangedstrlist = new ArrayList<String>();
        //get each line to arraylist


        for (int i = 0; i < strlist.size(); i++) {

            String t = strlist.get(i);

            //remove the line if contains [] if contains () just remove the ()
            if (t.contains("[") || t.contains("]")) {
                strlist.set(i, "*");
            }
            if (t.contains("(") || t.contains(")")) {
                String temp = t.replace("(", "");
                temp = temp.replace(")", "");
                strlist.set(i, temp);
            }
            //remove the line if contains [] if contains () just remove the ()

            //start on working on the lyrics
            // && strlist.get(i).length()+1!=System.getProperty("line.separator").length()
            //System.out.println(strlist.get(i)+" length "+strlist.get(i).length() );
            //System.out.println(System.getProperty("line.seperator")+" length "+System.getProperty("line.seperator").length() );
            //if (!strlist.get(i).equalsIgnoreCase(System.getProperty("line.seperator")))
            if (!strlist.get(i).equalsIgnoreCase("*")&&strlist.get(i).length()!=0) {

                unchangedstrlist.add(strlist.get(i));//add to unchanged lyrics
                unchangedlines.add(strlist.get(i));
                //add line to screen string
            }
        }
    }

    //TEXT TO SPEECH VOICE CONTROL PART
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecog.startListening(intent);
        }
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecog = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecog.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float v) {

                }

                @Override
                public void onBufferReceived(byte[] bytes) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int i) {

                }

                @Override
                public void onResults(Bundle results) {
                    List<String> result_arr = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String res = result_arr.get(0);
                    seskayitcikti.setText(res);
                    if (position<unchangedlines.size()){
                        sarkisözü.setText(unchangedlines.get(position));
                        position+=1;
                    }else {
                        position=0;
                        sarkisözü.setText(unchangedlines.get(position));
                        position+=1;
                    }

                }

                @Override
                public void onPartialResults(Bundle bundle) {

                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });
        }
    }

    private void initializeTextToSpeech() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (tts.getEngines().size() == 0) {
                    Toast.makeText(MainActivity.this, "Ses kaydı başarısız", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Locale locale = new Locale("en", "US");
                    int result = tts.setLanguage(locale);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        //speak(tts, "Ses kaydı için sağ alttaki butonu kullanınız.");
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Reinitialize the recognizer and tts engines upon resuming from background such as after openning the browser
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }
    public void speak(TextToSpeech tts, String message) {
        if (Build.VERSION.SDK_INT >= 21) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
