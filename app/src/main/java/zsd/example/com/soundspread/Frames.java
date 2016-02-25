package zsd.example.com.soundspread;

/**
 * Created by zsd on 2016/2/24.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class Frames {

    private static int version;
    private static int layer;
    private MP3File file;
    //存储帧在文件中的位移和大小
    private Vector<F> v = new Vector<F>();

    public Frames(MP3File file) throws Exception {
        //引用MP3File，方便获得MP3帧开始的位置
        this.file = file;
        try {
            FileInputStream fis = new FileInputStream(file.getPath());
            //定位到帧起始位置，开始解析
            fis.skip(file.getFrameOffset());
            parse(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //将传入的媒体时间转换为在文件中的位置
    public long time2offset(long time) {
        long offset = -1;
        long index = time / 260;
        offset = ((F) v.get((int) index)).offset;
        return offset;
    }

    private void parse(InputStream is) throws Exception {
        try {
            int position = file.getFrameOffset();
            //帧的结束位置，也就是ID3V1的起始位置
            long count = file.getLength() - 128;
            //计算帧的个数，每10个帧放入到Vector中
            int fc = 0;
            //存储10个帧的大小
            int fs = 0;
            while (is.available() > 0 && position < count) {
                //同步帧头位置
                int first = is.read();
                while (first != 255 && first != -1) {
                    first = is.read();
                }
                int second = is.read();
                if (second > 224) {
                    int third = is.read();
                    int forth = is.read();

                    int i20 = getBit(second, 4);
                    int i19 = getBit(second, 3);
                    if (i20 == 0 & i19 == 0)
                        throw new Exception
                                ("MPEG 2.5 is not supported");
                    //获得MPEG版本号
                    version = i19 == 0 ? 2 : 1;

                    int i18 = getBit(second, 2);
                    int i17 = getBit(second, 1);
                    layer = (4 - ((i18 << 1) + i17));

                    int i16 = getBit(second, 0);

                    int i15 = getBit(third, 7);
                    int i14 = getBit(third, 6);
                    int i13 = getBit(third, 5);
                    int i12 = getBit(third, 4);
                    //查表获得比特率
                    int bitRate = convertBitrate(i15,
                            i14, i13, i12) * 1000;

                    int i11 = getBit(third, 3);
                    int i10 = getBit(third, 2);
                    //查表获得抽样率
                    int sampleRate = convertSamplerate(i11, i10);

                    int padding = getBit(third, 1);
                    //计算帧的大小
                    int size = ((version == 1 ? 144 : 72) * bitRate)
                            / sampleRate + padding;
                    is.skip(size - 4);
                    fs += size;
                    fc++;
                    if (fc == 10) {
                        //每10帧存储一次
                        F f = new F(position, fs);
                        v.add(f);
                        fc = 0;
                        fs = 0;
                    }
                    position = position + size;
                }
            }
            //将剩余的帧放入Vector中
            if (fs != 0) {
                v.add(new F(position, fs));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //根据表7-5计算抽样率
    protected int convertSamplerate(int in1, int in2) {
        int sample = 0;
        switch ((in1 << 1) | in2) {
            case 0:
                sample = 44100;
                break;
            case 1:
                sample = 48000;
                break;
            case 2:
                sample = 32000;
                break;
            case 3:
                sample = 0;
                break;
        }
        if (version == 1) {
            return sample;
        } else {
            return sample / 2;
        }
    }
    //根据表7-4计算比特率
    protected int convertBitrate(int in1, int in2, int in3, int in4) {
        int[][] convert = { { 0, 0, 0, 0, 0, 0 }, { 32, 32, 32, 32, 32, 8 },
                { 64, 48, 40, 64, 48, 16 }, { 96, 56, 48, 96, 56, 24 },
                { 128, 64, 56, 128, 64, 32 }, { 160, 80, 64, 160, 80, 64 },
                { 192, 96, 80, 192, 96, 80 }, { 224, 112, 96, 224, 112, 56 },
                { 256, 128, 112, 256, 128, 64 },
                { 288, 160, 128, 288, 160, 128 },
                { 320, 192, 160, 320, 192, 160 },
                { 352, 224, 192, 352, 224, 112 },
                { 384, 256, 224, 384, 256, 128 },
                { 416, 320, 256, 416, 320, 256 },
                { 448, 384, 320, 448, 384, 320 }, { 0, 0, 0, 0, 0, 0 } };
        int index1 = (in1 << 3) | (in2 << 2) | (in3 << 1) | in4;
        int index2 = (version - 1) * 3 + layer - 1;
        return convert[index1][index2];
    }

    private int getBit(int input, int bit) {
        return (input & (1 << bit)) > 0 ? 1 : 0;
    }

    class F {
        int offset;
        int size;
        public F(int _offset, int _size) {
            offset = _offset;
            size = _size;
        }
    }
}
