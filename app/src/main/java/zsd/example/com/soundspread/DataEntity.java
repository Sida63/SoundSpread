package zsd.example.com.soundspread;

import java.io.Serializable;

/**
 * Created by zsd on 2016/1/20.
 */
public class DataEntity implements Serializable {
    private int bookmarktime;
    public DataEntity()
    {
        bookmarktime=0;
    }

    public int getBookmarktime() {
        return bookmarktime;
    }

    public void setBookmarktime(int bookmarktime) {
        this.bookmarktime = bookmarktime;
    }





}
