package zsd.example.com.soundspread;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class EditActivity extends AppCompatActivity implements MarkerView.MarkerListener,
        WaveformView.WaveformListener{
    private Button clipaudio;
    private Button checkclipfile;
    private Spinner spinner;
    private Spinner spinner1;
    private String musicname;
    private long firstbookmark;
    private long secondbookmark;
    private long curretntime;


    private DataList dataList;
    public static EditActivity instance = null;
    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private long mRecordingLastUpdateTime;
    private boolean mRecordingKeepGoing;
    private double mRecordingTime;
    private boolean mFinishActivity;
    private TextView mTimerTextView;
    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private SoundFile mSoundFile;
    private File mFile;
    private String mFilename;
    private String mArtist;
    private String mTitle;
    private int mNewFileKind;
    private boolean mWasGetContentIntent;
    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private TextView mStartText;
    private TextView mEndText;
    private TextView mInfo;
    private String mInfoContent;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private boolean mKeyDown;
    private String mCaption = "";
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private Handler mHandler;
    private boolean mIsPlaying;
    //private SamplePlayer mPlayer;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private ArrayList<TextView> bookmarklist;
    private int mMarkerBottomOffset;

    private Thread mLoadSoundFileThread;
    private Thread mRecordAudioThread;
    private Thread mSaveSoundFileThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        curretntime=System.currentTimeMillis();
        bookmarklist=new ArrayList<TextView>();
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        musicname = (String) bundle.getSerializable("uri");
        Uri uri= Uri.parse(musicname);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;
        mHandler=new Handler();
        mMarkerLeftInset = (int)(46 * mDensity);
        mMarkerRightInset = (int)(48 * mDensity);
        mMarkerTopOffset = (int)(10 * mDensity);
        mMarkerBottomOffset = (int)(10 * mDensity);
        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;
        mLoadingLastUpdateTime = getCurrentTime();
        mFilename="/mnt/soundspread/test1.mp3";
        mFile = new File(mFilename);
        try {
           // mSoundFile = SoundFile.create("/mnt/sdcard/soundspread/Sponge bob.mp3");
            mSoundFile = SoundFile.create(musicname);
            //mSoundFile = SoundFile.create("/mnt/sdcard/soundspread/clip/test.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SoundFile.InvalidInputException e) {
            e.printStackTrace();
        }
        mWaveformView = (WaveformView)findViewById(R.id.waveform);
        mWaveformView.setListener(this);
        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            Toast.makeText(EditActivity.this,"input",Toast.LENGTH_SHORT).show();
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }
        else
        {
            Toast.makeText(EditActivity.this,"aout",Toast.LENGTH_SHORT).show();
        }
        mStartMarker = (MarkerView)findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView)findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;

        //Toast.makeText(EditActivity.this, musicname, Toast.LENGTH_SHORT).show();
        clipaudio=(Button)findViewById(R.id.clipaudio);
        clipaudio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final EditText et=new EditText(EditActivity.this);
                new AlertDialog.Builder(EditActivity.this).setTitle("please input filename").setIcon(
                        android.R.drawable.ic_dialog_info).setView(et)
                        .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    MP3File f = new MP3File(musicname);
                                    //Toast.makeText(EditActivity.this,et.getText().toString(),Toast.LENGTH_SHORT).show();
                                    f.cut(firstbookmark, secondbookmark, et.getText().toString());
                                    Toast.makeText(EditActivity.this, "MP3 file is cut successfully", Toast.LENGTH_SHORT).show();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });
        checkclipfile=(Button)findViewById(R.id.checkclipfile);
        checkclipfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(EditActivity.this, ShowActivity.class);
                startActivity(intent);
            }
        });


        dataList=(DataList)bundle.getSerializable("datalist");
        //Toast.makeText(EditActivity.this,Long.toString(dataList.getitem(0).getBookmarktime()),Toast.LENGTH_SHORT).show();
        spinner = (Spinner) findViewById(R.id.showbookmark);
        spinner1 = (Spinner) findViewById(R.id.shownextbookmark);
        String[] mItems=new String[dataList.size()];
        int transtime,min,sec;
        for(int i=0;i<dataList.size();i++)
        {
            transtime=dataList.getitem(i).getBookmarktime()/1000;
            min = transtime/60;
            sec = transtime%60;
            if (sec<10) {
                mItems[i]=Integer.toString(min)+":0"+Integer.toString(sec);
            }else{
                mItems[i]=Integer.toString(min)+":"+Integer.toString(sec);
            }

        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                firstbookmark = dataList.getitem(pos).getBookmarktime();
                // Toast.makeText(EditActivity.this, Long.toString(firstbookmark), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                secondbookmark = dataList.getitem(pos).getBookmarktime();
                // Toast.makeText(EditActivity.this,Long.toString(secondbookmark),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        spinner.setAdapter(adapter);
        spinner1.setAdapter(adapter);
        updateDisplay();
     //   updatedotdisplay();
        instance = this;


    }

    @Override
    public void markerTouchStart(MarkerView marker, float pos) {
        mTouchDragging = true;
        mTouchStart = pos;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    @Override
    public void markerTouchMove(MarkerView marker, float pos) {
        float delta = pos - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int)(mTouchInitialStartPos + delta));
            mEndPos = trap((int)(mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int)(mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    @Override
    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            //setOffsetGoalEnd();
        }
    }

    @Override
    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            //setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }

    @Override
    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            //setOffsetGoalEnd();
        }

        updateDisplay();
    }

    @Override
    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

       // updateDisplay();
    }

    @Override
    public void markerEnter(MarkerView marker) {

    }

    @Override
    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    @Override
    public void markerDraw() {

    }

    @Override
    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = getCurrentTime();
    }

    @Override
    public void waveformTouchMove(float x) {
        mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
      //  updatedotdisplay();
    }

    @Override
    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int)(mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                } else {
                }
            } else {
            }
        }
    }

    @Override
    public void waveformFling(float x) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int)(-x);
        updateDisplay();
      //  updatedotdisplay();
    }

    @Override
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown) {
            updateDisplay();
         //   updatedotdisplay();
        }
        else if (mIsPlaying) {
            updateDisplay();
          //  updatedotdisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
          //  updatedotdisplay();
        }
    }

    @Override
    public void waveformZoomIn() {

    }

    @Override
    public void waveformZoomOut() {

    }
   /* public void onItemSelected(AdapterView<?> parent,
                               View v, int position, long id) {
        switch(v.getId())
        {
            case R.id.showbookmark:
                //firstbookmark=dataList.getitem(position).getBookmarktime();
                firstbookmark=71538;
                Toast.makeText(EditActivity.this,Long.toString(firstbookmark),Toast.LENGTH_SHORT).show();
                break;
            case R.id.shownextbookmark:
                //secondbookmark=dataList.getitem(position).getBookmarktime();
                secondbookmark=181230;
                Toast.makeText(EditActivity.this,Long.toString(secondbookmark),Toast.LENGTH_SHORT).show();
                break;
        }
    }*/
   private long getCurrentTime() {
       return System.nanoTime() / 1000000;
   }
    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
    }
    private synchronized void updateDisplay() {
       // int frames = mWaveformView.millisecsToPixels(0);
       // mWaveformView.setPlayback(frames);
        //setOffsetGoalNoUpdate(frames - mWidth / 2);
        if (mIsPlaying) {
            int frames = mWaveformView.millisecsToPixels(0);//set the default value, which can be replaced by the position of the player
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
        }
        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }
        //mStartMarker.setAlpha(1f);//change the transparency of startmark
        //mEndMarker.setAlpha(1f);//change the transparency of endmark
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                startX,
                mMarkerTopOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mStartMarker.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                endX,
                (int) (getWindowManager().getDefaultDisplay().getHeight() * 0.333),
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mEndMarker.setLayoutParams(params);
        updatedotdisplay();
    }

    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }
    private String formatDecimal(double x) {
        int xWhole = (int)x;
        int xFrac = (int)(100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }
    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }
    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void updatedotdisplay() {
        int mOffset1=mOffset;
        if (System.currentTimeMillis() - curretntime > 1000) {
            RelativeLayout dynamiclayout = (RelativeLayout) findViewById(R.id.dynamiclayout);
            if (bookmarklist.size() != 0) {
                for (int k = 0; k < bookmarklist.size(); k++) {
                    dynamiclayout.removeView(bookmarklist.get(k));
                }
            }
            //Toast.makeText(EditActivity.this,Integer.toString(mOffset),Toast.LENGTH_SHORT).show();
            for (int i = 0; i < dataList.size(); i++) {
                if ((int) ((dataList.getitem(i).getBookmarktime() / 60000.0) * dynamiclayout.getWidth()) - mOffset1 >= 0) {
                    //Toast.makeText(EditActivity.this,Integer.toString(dataList.getitem(i).getBookmarktime()/1000),Toast.LENGTH_SHORT).show();
                    TextView bookmark = new TextView(this);
                    //bookmark.setId(110);
                    bookmark.setText("●");
                    bookmark.setId(dataList.getitem(i).getBookmarktime() / 1000);
                    bookmark.setTextColor(android.graphics.Color.RED);
                    RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lp1.setMargins((int) ((dataList.getitem(i).getBookmarktime() / 60000.0) * getWindowManager().getDefaultDisplay().getWidth()) - mOffset1
                            , (int) (getWindowManager().getDefaultDisplay().getHeight() * 0.2), bookmark.getWidth(), bookmark.getHeight());
//        lp1.addRule(RelativeLayout.ALIGN_TOP);
//        lp1.setMargins(30, 50, 100, 100);//(int left, int top, int right, int bottom)
                    dynamiclayout.addView(bookmark, lp1);
                    bookmarklist.add(bookmark);
                } else {
                    //Toast.makeText(EditActivity.this,"delete",Toast.LENGTH_SHORT).show();
                    for (int j = 0; j < bookmarklist.size(); j++) {
                        // Toast.makeText(EditActivity.this,bookmarklist.get(j).getText().toString(),Toast.LENGTH_SHORT).show();
                        if (bookmarklist.get(j).getId() == dataList.getitem(i).getBookmarktime() / 1000)
                            dynamiclayout.removeView(bookmarklist.get(j));
                    }
                }
            }
        }
    }

}
