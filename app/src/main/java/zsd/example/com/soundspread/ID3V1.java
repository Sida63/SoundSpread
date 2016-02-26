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
    public static void main(String[] args) {
        File f = new File("f:/media/mp3/other/huozhe.mp3");
        ID3V1 id3v1 = new ID3V1(f);
        try {
            id3v1.initialize();
            System.out.println(id3v1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("标题="+title+"\n");
        buffer.append("歌手="+artist+"\n");
        buffer.append("专辑="+album+"\n");
        buffer.append("年代="+year+"\n");
        buffer.append("注释="+comment+"\n");
        return buffer.toString();
    }
}