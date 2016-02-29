package zsd.example.com.soundspread;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;


public class EditActivity extends AppCompatActivity {
    private Button clipaudio;
    private Button checkclipfile;
    private Spinner spinner;
    private Spinner spinner1;
    private String musicname;
    private long firstbookmark;
    private long secondbookmark;
    private DataList dataList;
    public static EditActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
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
}
