package com.example.hp.melody;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    private SpeechRecognizer speechRecognizer = null;
    private Intent speechRecognizerIntent;
    private String speechString;
    ArrayList<String> speechFromUser;
    Boolean canRead = false, containsSong = false;

    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    ArrayList<Long> IdList;
    ListView listView;
    MediaPlayerHelper mediaPlayerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //calling for voice command permission
        getPermissions();

        //setting listview
        listView = findViewById(R.id.lv);
        arrayList = new ArrayList<>();
        IdList = new ArrayList<>();

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);

        //set arraylists
        if(canRead) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri,null ,null ,null ,null ,null );

            if(cursor!=null && cursor.moveToFirst()){
                int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

                while(cursor.moveToNext()){
                    arrayList.add(cursor.getString(title)+"\nArtist: "+cursor.getString(artist));
                    IdList.add(cursor.getLong(id));
                }

            }
        }

        //floating action button
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                relativeLayout = findViewById(R.id.rel_lay);
                Snackbar snackbar = Snackbar.make(relativeLayout, "listening...", Snackbar.LENGTH_SHORT);
                View v = snackbar.getView();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                params.bottomMargin = 500;
                v.setLayoutParams(params);
                snackbar.show();
                speechRecognizer.startListening(speechRecognizerIntent);

            }
        });

        //onClick listview
        mediaPlayerHelper = new MediaPlayerHelper(MainActivity.this,IdList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                containsSong = true;
                mediaPlayerHelper.playSong(i);
                fab.setY(1300);
            }
        });
        mediaPlayerHelper.setController(findViewById(R.id.lv));


        //settings for speechRecognizer and speechRecognizerIntent
        speechRecognizer = speechRecognizer.createSpeechRecognizer(MainActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
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
                //listen();
            }

            @Override
            public void onResults(Bundle bundle) {
                speechFromUser = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                if(speechFromUser!= null){

                    boolean res = true;

                    if(containsSong){

                        if(speechFromUser.contains("play") ||
                                speechFromUser.contains("play the song") ||
                                speechFromUser.contains("play this song")){

                            if(mediaPlayerHelper.isPlaying()) {
                                Toast.makeText(getApplicationContext(),
                                        "already playing...", Toast.LENGTH_LONG).show();
                            }else{
                                mediaPlayerHelper.start();
                            }

                        }

                        else if(speechFromUser.contains("pause") ||
                                speechFromUser.contains("pause the song") ||
                                speechFromUser.contains("pause this song")){

                            if(mediaPlayerHelper.isPaused()){
                                Toast.makeText(getApplicationContext(),
                                        "already paused!!!", Toast.LENGTH_LONG).show();
                            }else{
                                mediaPlayerHelper.pause();
                            }

                        }

                        else if(speechFromUser.contains("next song") ||
                                speechFromUser.contains("play the next song") ||
                                speechFromUser.contains("play next song")){

                            mediaPlayerHelper.playNext();

                        }

                        else if(speechFromUser.contains("previous song") ||
                                speechFromUser.contains("play the previous song") ||
                                speechFromUser.contains("play previous song")){

                            mediaPlayerHelper.playPrev();
                        }

                        else{

                            Toast.makeText(getApplicationContext(),
                                    "Command not determined!!!", Toast.LENGTH_LONG).show();

                            res = false;
                        }

                    }else{

                        Toast.makeText(getApplicationContext(),
                                "No song selected!!!", Toast.LENGTH_LONG).show();

                        res = false;
                    }

                    if(res){
                        fab.setY(1300);
                    }


                }

//                speechRecognizer.stopListening();
//                Toast.makeText(getApplicationContext(), "stopped listening!!!",Toast.LENGTH_SHORT ).show();

               // listen();

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    public void getPermissions(){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest
//            .permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
//
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                        Uri.parse("package:"+getPackageName()));
//                startActivity(intent);
//                finish();
//            }
//        }

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    canRead = true;
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }


        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(),"Error Occured" , Toast.LENGTH_SHORT).show();
            }
        }).check();


    }


//    public void listen(){
//        if(speechRecognizer==null){
//            speechRecognizer = speechRecognizer.createSpeechRecognizer(MainActivity.this);
//        }
//        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
//                Locale.getDefault());
//
//        speechRecognizer.startListening(speechRecognizerIntent);
//        Toast.makeText(getApplicationContext(), "listening...",Toast.LENGTH_SHORT ).show();
//    }
}
