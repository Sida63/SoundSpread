package zsd.example.com.soundspread;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent=getIntent();
        Bundle bundle = intent.getExtras();
        DataList dataList=(DataList)bundle.getSerializable("datalist");
        Toast.makeText(EditActivity.this,Integer.toString(dataList.getitem(0).getBookmarktime()),Toast.LENGTH_SHORT).show();
        Spinner spinner = (Spinner) findViewById(R.id.showbookmark);
        String[] mItems=new String[dataList.size()];
        for(int i=0;i<dataList.size();i++)
        {
            mItems[i]=Integer.toString(dataList.getitem(i).getBookmarktime());
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }
}
