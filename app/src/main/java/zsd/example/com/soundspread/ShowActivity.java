package zsd.example.com.soundspread;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowActivity extends AppCompatActivity {
    private String[] items;
    private ArrayList<HashMap<String, Object>> listItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        scanfolder();
        ListView listView = (ListView)findViewById(R.id.listView);
        /*SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.item, new String[]{"filename"},
                new int[]{R.id.filename});*/
        //listView.setAdapter(adapter);
        listView.setAdapter(new ArrayAdapter<String>(ShowActivity.this,
                android.R.layout.simple_list_item_1,
                items));


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(ShowActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("uri", "/mnt/sdcard/soundspread/clip/"+items[arg2]);
                intent.putExtras(bundle);
                startActivity(intent);
                EditActivity.instance.finish();
                MainActivity.instance.finish();
                finish();
            }

        });
    }
    public void scanfolder(){
        String path="/mnt/sdcard/soundspread/clip/";
        Toast.makeText(ShowActivity.this, path, Toast.LENGTH_SHORT);
        File file=new File(path);
        File[] filelist =file.listFiles();
        String []filename=new String[filelist.length];
        /*listItem = new ArrayList<HashMap<String, Object>>();
        for(int i=0;i<filelist.length;i++){
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("filename",filelist[i].getName().toString());
            listItem.add(map);*/
        for(int i=0;i<filelist.length;i++){
            filename[i]=filelist[i].getName().toString();
        }
        items=filename;

    }
}
