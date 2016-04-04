package zsd.example.com.soundspread;

/**
 * Created by zsd on 2016/2/23.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ID3V1 {

    private String title;
    private String artist;
    private String album;
    private String comment;
    private String year;
    private byte reserve;
    private byte track;
    private byte genre;
    private File file;
    //可以用MP3文件测试ID3V1解析的结果
    public static String main(File f) {

        ID3V1 id3v1 = new ID3V1(f);
        try {
            id3v1.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return(id3v1.toString());
    }
    public ID3V1(File file) {
        this.file = file;
    }
    public void initialize() throws Exception {
        try {
            //可以随机访问文件的任意部分
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            //跳到ID3V1开始的位置
            raf.seek(raf.length() - 128);
            byte[] tag = new byte[3];
            //读取Header
            raf.read(tag);
            if (!new String(tag).equals("TAG")) {
                throw new Exception("No ID3V1 found");
            }
            byte[] tags = new byte[125];
            raf.read(tags);
            //逐一读取ID3V1中的各个字段
            readTag(tags);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readTag(byte[] array) {
        title = new String(array, 0, 30).trim();
        artist = new String(array, 30, 30).trim();
        album = new String(array, 60, 30).trim();
        year = new String(array, 90, 4);
        comment = new String(array, 94, 28).trim();
        reserve = array[122];
        track = array[123];
        genre = array[124];
    }
/*
    public String getTag(String wantwhat){
        String result="";
        switch (wantwhat){
            case "title": result=title;
                break;
            case "artist": result=artist;
                break;
            case "album": result=album;
                break;
            case "year": result=year;
                break;
        }
        return result;
    }
*/
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("Title: "+title+"\n");
      //  buffer.append("Artist: "+artist+"\n");
        buffer.append("Full Podcast: "+album+"\n");
        buffer.append("Year: "+year+"\n");
        //buffer.append("Comment: "+comment+"\n");
        return buffer.toString();
    }
}