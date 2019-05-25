package com.example.hp.melody;

import android.content.ContentUris;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;

public class MediaPlayerHelper extends AppCompatActivity implements MediaController.MediaPlayerControl,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    private int songPosn = 0;
    Context context;
    ArrayList<Long> IdList;
    MediaPlayer player = new MediaPlayer();
    private MusicController controller;
    private boolean playbackPaused=false;

    public MediaPlayerHelper(Context c, ArrayList<Long> IdList){
        context = c;
        this.IdList = IdList;
        player.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void start() {
        playbackPaused = false;
        player.start();
        controller.show(0);
    }

    @Override
    public void pause() {
        playbackPaused = true;
        player.pause();
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public int getDuration() {
        if(player!=null){
            return player.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(player!=null)
            return player.getCurrentPosition();
        return 0;
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void playSong(int pos){
        songPosn = pos;
        player.reset();
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                IdList.get(pos));
        try{
            player.setDataSource(context,uri);
            player.prepare();
        }catch (Exception e){
            Toast.makeText(context,"error setting data source" ,Toast.LENGTH_SHORT ).show();
        }

        start();
    }

    public void playNext(){
        songPosn++;
        if(songPosn==IdList.size())
            songPosn=0;
        playSong(songPosn);
        if(playbackPaused){
            playbackPaused=false;
        }

        controller.show(0);
    }

    public void playPrev(){
        songPosn--;
        if(songPosn== -1)
            songPosn = IdList.size()-1;
        playSong(songPosn);
        if(playbackPaused){
            playbackPaused=false;
        }
        controller.show(0);
    }

    public void setController(View lv){
        controller = new MusicController(context);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(lv);
        controller.setEnabled(true);
    }

    public boolean isPaused(){
        return playbackPaused;
    }
}
