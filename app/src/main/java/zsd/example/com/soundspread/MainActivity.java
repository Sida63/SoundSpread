package zsd.example.com.soundspread;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener ,SeekBar.OnSeekBarChangeListener{
    private MediaPlayer mMediaPlayer=null;//媒体播放器
    private AudioManager mAudioManager=null;//声音管理器
    private Button buttonShare=null;
    private Button mPlayButton=null;
    private Button mPauseButton=null;
    private Button mStopButton=null;
    private Button addbookmark=null;
    private Button checkbookmark=null;
    private SeekBar mSoundSeekBar=null;
    private SeekBar mSoundProcessBar=null;
    private Timer mTimer=new Timer();
    private int maxStreamVolume;//最大音量
    private int currentStreamVolume;//当前音量
    private String musicname;
    //private int setStreamVolume;//设置的音量
    public DataEntity bookmarkentity;
    private DataList bookmarklist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaPlayer=MediaPlayer.create(this, R.raw.happyis);//加载res/raw的happyis.mp3文件
        //Uri uri=Uri.parse("/mnt/sdcard/soundspread/happyis.mp3");
        bookmarklist = new DataList();
        bookmarkentity = new DataEntity();
        bookmarkentity.setBookmarktime(0);
        bookmarklist.additem(bookmarkentity);
        Uri uri=null;
        Intent intent=getIntent();
        if(intent.getExtras()!=null) {
            Bundle bundle = intent.getExtras();
            musicname = (String) bundle.getSerializable("uri");
            uri=Uri.parse(musicname);
        }
        if (uri!=null) mMediaPlayer=MediaPlayer.create(this,uri);
        TextView musicName= (TextView)findViewById(R.id.musicname);
        Pattern p = Pattern.compile("[^/]+\\..+");
        Matcher m=p.matcher(musicname);
        if(m.find()==true)
        {
            musicName.setText(m.group().toString());
        }
        mAudioManager=(AudioManager)this.getSystemService(AUDIO_SERVICE);
        buttonShare=(Button)findViewById(R.id.buttonshare);
        mPlayButton=(Button)findViewById(R.id.Play);
        mPauseButton=(Button)findViewById(R.id.Pause);
        mStopButton=(Button)findViewById(R.id.Stop);
        addbookmark=(Button)findViewById(R.id.bookmark);
        checkbookmark=(Button)findViewById(R.id.checkbookmark);
        mSoundSeekBar=(SeekBar)findViewById(R.id.SoundSeekBar);
        mSoundProcessBar=(SeekBar)findViewById(R.id.soundprocessseekBar);
        mPlayButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        addbookmark.setOnClickListener(this);
        checkbookmark.setOnClickListener(this);
        buttonShare.setOnClickListener(this);
        maxStreamVolume=mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentStreamVolume=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mSoundSeekBar.setMax(maxStreamVolume);
        mSoundSeekBar.setProgress(currentStreamVolume);
        mSoundSeekBar.setOnSeekBarChangeListener(this);
        mSoundProcessBar.setProgress(0);
        mSoundProcessBar.setMax(mMediaPlayer.getDuration());
        mSoundProcessBar.setOnSeekBarChangeListener(this);
        final Handler handleProgress = new Handler() {
            public void handleMessage(Message msg) {

                double position = mMediaPlayer.getCurrentPosition();
                double duration = mMediaPlayer.getDuration();

                if (duration > 0) {
                    double pos = mSoundProcessBar.getMax() * position / duration;
                    mSoundProcessBar.setProgress((int) pos);
                }
            };
        };
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer==null)
                    return;
                if (mMediaPlayer.isPlaying() && mSoundProcessBar.isPressed() == false) {
                    handleProgress.sendEmptyMessage(0);
                }
            }
        };

        mTimer.schedule(mTimerTask, 0, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){
            case R.id.bookmark:
                bookmarkentity = new DataEntity();
                int tempal=0;
                tempal=mMediaPlayer.getCurrentPosition();
                if(tempal!=0) {
                    bookmarkentity.setBookmarktime(tempal);
                    bookmarklist.additem(bookmarkentity);
                    Toast.makeText(MainActivity.this, Integer.toString(tempal), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.checkbookmark:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("datalist", bookmarklist);
                bundle.putSerializable("uri", musicname);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.buttonshare:
                String sharePath=musicname;
                //Uri uri = Uri.parse(sharePath);
                Uri uri = Uri.fromFile(new File(sharePath));
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(share, "Share Sound File"));
                break;
            case R.id.Play:
                mMediaPlayer.start();
                break;
            case R.id.Pause:
                mMediaPlayer.pause();
                break;
            case R.id.Stop:
                System.out.println("Stop");
                mMediaPlayer.stop();
                try{
                    mMediaPlayer.prepare();
                }catch(IllegalStateException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
                mMediaPlayer.seekTo(0);
                break;
            default:
                break;
        }
    }

    //进度条变化
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO Auto-generated method stub
       if(seekBar.equals(mSoundSeekBar)) {
           System.out.println("progress:" + String.valueOf(progress));
           mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
       }
        else if(seekBar.equals(mSoundProcessBar))
       {
          /* mMediaPlayer.pause();
           mMediaPlayer.seekTo(progress);
           mMediaPlayer.start();*/
           if(mMediaPlayer.getCurrentPosition()!=progress) {
               mMediaPlayer.seekTo(progress);
               mSoundProcessBar.setProgress(progress);
           }
          /* double position = mMediaPlayer.getCurrentPosition();
           double duration = mMediaPlayer.getDuration();

           if (duration > 0) {
               double pos = mSoundProcessBar.getMax() * position / duration;
               mSoundProcessBar.setProgress((int) pos);
           }*/
       }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        super.onBackPressed();
    }
}
