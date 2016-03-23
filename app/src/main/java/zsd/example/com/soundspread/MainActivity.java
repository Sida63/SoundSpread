package zsd.example.com.soundspread;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener ,SeekBar.OnSeekBarChangeListener{
    private MediaPlayer mMediaPlayer=null;//媒体播放器
    private AudioManager mAudioManager=null;//声音管理器
    private Button buttonShare=null;
    private Button buttonFace=null;
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
    private TextView starttime;
    private TextView finishtime;
    private TextView textbar;
    private TextMoveLayout textMoveLayout;
    private ViewGroup.LayoutParams layoutParams;
    private int screenWidth;
    private float moveStep = 0;
    //private int setStreamVolume;//设置的音量
    public DataEntity bookmarkentity;
    private DataList bookmarklist;
    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;
    private Thread thread;
    public static MainActivity instance;
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.RECORD_AUDIO,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
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

        mAudioManager = (AudioManager)this.getSystemService(AUDIO_SERVICE);
        buttonShare=(Button)findViewById(R.id.buttonshare);
       // buttonFace=(Button)findViewById(R.id.facebook);
        mPlayButton=(Button)findViewById(R.id.Play);
        mPauseButton=(Button)findViewById(R.id.Pause);
        mStopButton=(Button)findViewById(R.id.Stop);
        addbookmark=(Button)findViewById(R.id.bookmark);
        checkbookmark=(Button)findViewById(R.id.checkbookmark);
        mSoundSeekBar=(SeekBar)findViewById(R.id.SoundSeekBar);
        mSoundProcessBar=(SeekBar)findViewById(R.id.soundprocessseekBar);
        mVisualizerView = (VisualizerView) findViewById(R.id.myvisualizerview);
        starttime=(TextView)findViewById(R.id.starttime);
        finishtime=(TextView)findViewById(R.id.finishtime);
        starttime.setText("00:00:00");
        int hour=mMediaPlayer.getDuration()/(1000*60*60);
        int minute=(mMediaPlayer.getDuration()%(1000*60*60))/(1000*60);
        int second=((mMediaPlayer.getDuration()%(1000*60*60))%(1000*60))/1000;
        String temp=" ";
        if(hour<10&&minute<10&&second<10)
            temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour<10&&minute<10&&second>10)
            temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour<10&&minute>10&&second<10)
            temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour<10&&minute>10&&second>10)
            temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour>10&&minute<10&&second<10)
            temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour>10&&minute<10&&second>10)
            temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
        else if(hour>10&&minute>10&&second<10)
            temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
        else if(hour>10&&minute>10&&second>10)
            temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
        finishtime.setText(temp);
        textbar = new TextView(this);
        textbar.setBackgroundColor(Color.rgb(245, 245, 245));
        textbar.setTextColor(Color.rgb(0, 161, 229));
        textbar.setTextSize(16);
        moveStep = (float) (((float) screenWidth / (float) mMediaPlayer.getDuration()/1000) * 0.8);
        screenWidth=getWindowManager().getDefaultDisplay().getWidth();
        layoutParams = new ViewGroup.LayoutParams(screenWidth, 50);
        textMoveLayout = (TextMoveLayout) findViewById(R.id.textLayout);
        textMoveLayout.addView(textbar, layoutParams);
        textbar.layout(0, 20, screenWidth, 80);
        textbar.setText("00:00:00");
       // buttonFace.setOnClickListener(this);
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
        thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                setupVisualizerFxAndUI();
                mVisualizer.setEnabled(true);

            }
        });
        thread.start();

        mTimer.schedule(mTimerTask, 0, 1000);
        instance=this;
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
                    int transtime=tempal/1000;
                    int min = transtime/60;
                    int sec = transtime%60;
                    String showtime;
                    if (sec<10) {
                        showtime=Integer.toString(min)+":0"+Integer.toString(sec);
                    }else{
                        showtime=Integer.toString(min)+":"+Integer.toString(sec);
                    }
                    Toast.makeText(MainActivity.this, "Added bookmark at "+showtime, Toast.LENGTH_SHORT).show();
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
                String txtfile=musicname.replace(".mp3", ".txt");
                String content="";
                try {
                    File filename = new File(txtfile); // 要读取以上路径的input。txt文件
                    FileInputStream fileInputStream = new FileInputStream(filename);
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        byteArrayInputStream.write(buffer, 0, len);
                    }

                    content = new String(byteArrayInputStream.toByteArray());
/*
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(filename)); // 建立一个输入流对象reader
                    BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
                    content = br.readLine();
                    while (content != null) {
                        content = br.readLine(); // 一次读入一行数据
                    }
                    */
                } catch (Exception e) {
                       e.printStackTrace();
                }
              //  Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/*");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(Intent.EXTRA_TEXT,content);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(share, "Share Sound File"));
                break;
            /*
            case R.id.facebook:
                String urlToShare = "http://stackoverflow.com/questions/7545254";
                Intent facebookit = new Intent(Intent.ACTION_SEND);
                facebookit.setType("text/plain");
// intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB: has no effect!
                facebookit.putExtra(Intent.EXTRA_TEXT, urlToShare);

// See if official Facebook app is found
                boolean facebookAppFound = false;
                List<ResolveInfo> matches = getPackageManager().queryIntentActivities(facebookit, 0);
                for (ResolveInfo info : matches) {
                    if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                        facebookit.setPackage(info.activityInfo.packageName);
                        facebookAppFound = true;
                        break;
                    }
                }

// As fallback, launch sharer.php in a browser
                if (!facebookAppFound) {
                    String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
                    facebookit = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                }

                startActivity(facebookit);
                break;
                */
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
                mSoundProcessBar.setProgress(0);
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
           textbar.layout((int) (progress * moveStep), 20, screenWidth, 80);
           int hour=mMediaPlayer.getCurrentPosition()/(1000*60*60);
           int minute=(mMediaPlayer.getCurrentPosition()%(1000*60*60))/(1000*60);
           int second=((mMediaPlayer.getCurrentPosition()%(1000*60*60))%(1000*60))/1000;
           String temp=" ";
           if(hour<10&&minute<10&&second<10)
               temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
           else if(hour<10&&minute<10&&second>10)
               temp="0"+Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
           else if(hour<10&&minute>10&&second<10)
               temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
           else if(hour<10&&minute>10&&second>10)
               temp="0"+Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
           else if(hour>10&&minute<10&&second<10)
               temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
           else if(hour>10&&minute<10&&second>10)
               temp=Integer.toString(hour)+":"+"0"+Integer.toString(minute)+":"+Integer.toString(second);
           else if(hour>10&&minute>10&&second<10)
               temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+"0"+Integer.toString(second);
           else if(hour>10&&minute>10&&second>10)
               temp=Integer.toString(hour)+":"+Integer.toString(minute)+":"+Integer.toString(second);
           textbar.setText(temp);
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

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                        // mVisualizerView.updateVisualizer(bytes);
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
