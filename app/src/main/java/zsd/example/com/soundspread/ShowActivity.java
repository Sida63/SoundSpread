package zsd.example.com.soundspread;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowActivity extends AppCompatActivity {
    private String[] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        scanfolder();
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(ShowActivity.this,
                android.R.layout.simple_list_item_1,
                items));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
            Toast.makeText(ShowActivity.this,"/mnt/sdcard/soundspread/clip/"+items[arg2],Toast.LENGTH_SHORT).show();
            }

        });
    }
    public void scanfolder(){
        String path="/mnt/sdcard/soundspread/clip/";
        Toast.makeText(ShowActivity.this, path, Toast.LENGTH_SHORT);
        File file=new File(path);
        File[] filelist =file.listFiles();
        String []filename=new String[filelist.length];
        for(int i=0;i<filelist.length;i++){
            filename[i]=filelist[i].getName().toString();
        }
        items=filename;
    }
}
