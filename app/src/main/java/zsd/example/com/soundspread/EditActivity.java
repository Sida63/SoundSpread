package zsd.example.com.soundspread;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class EditActivity extends AppCompatActivity implements MarkerView.MarkerListener,
        WaveformView.WaveformListener{
    private Button clipaudio;
    private Button checkclipfile;
    private Spinner spinner;
    private Spinner spinner1;
    private String musicname;
    private long firstbookmark;
    private long secondbookmark;

    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private DataList dataList;
    private File mFile;
    private String mFilename;
    private SoundFile mSoundFile;
    private long mLoadingLastUpdateTime;
    public static EditActivity instance = null;
    private float mDensity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;
        mLoadingLastUpdateTime = getCurrentTime();
        mFilename="/mnt/soundspread/test1.mp3";
        mFile = new File(mFilename);
        try {
            mSoundFile = SoundFile.create("/mnt/sdcard/soundspread/Sponge bob.mp3");
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

        mEndMarker = (MarkerView)findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        musicname = (String) bundle.getSerializable("uri");
        Uri uri= Uri.parse(musicname);
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
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                firstbookmark=dataList.getitem(pos).getBookmarktime();
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
                secondbookmark=dataList.getitem(pos).getBookmarktime();
               // Toast.makeText(EditActivity.this,Long.toString(secondbookmark),Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        spinner.setAdapter(adapter);
        spinner1.setAdapter(adapter);
        instance = this;


    }

    @Override
    public void markerTouchStart(MarkerView marker, float pos) {

    }

    @Override
    public void markerTouchMove(MarkerView marker, float pos) {

    }

    @Override
    public void markerTouchEnd(MarkerView marker) {

    }

    @Override
    public void markerFocus(MarkerView marker) {

    }

    @Override
    public void markerLeft(MarkerView marker, int velocity) {

    }

    @Override
    public void markerRight(MarkerView marker, int velocity) {

    }

    @Override
    public void markerEnter(MarkerView marker) {

    }

    @Override
    public void markerKeyUp() {

    }

    @Override
    public void markerDraw() {

    }

    @Override
    public void waveformTouchStart(float x) {

    }

    @Override
    public void waveformTouchMove(float x) {

    }

    @Override
    public void waveformTouchEnd() {

    }

    @Override
    public void waveformFling(float x) {

    }

    @Override
    public void waveformDraw() {

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
}
