package com.cjw.showimagev1.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.http.HttpDownloader;

/**
 * ��ͼƬ���й���Ĺ����ࡣ
 */
public class ImageLoader
{

    // ͼƬ���漼���ĺ����࣬���ڻ����������غõ�ͼƬ���ڳ����ڴ�ﵽ�趨ֵʱ�Ὣ�������ʹ�õ�ͼƬ�Ƴ���
    private LruCache<String, Bitmap> mMemoryCache; // �Ƿ���static�ȽϺ� FIXME

    private static ImageLoader mImageLoader;

    // ���ص��̳߳�
    private ExecutorService mImageThreadPool = null; // �Ƿ���static�ȽϺ� FIXME

    private FileUtils mFileUtils;
    private HttpDownloader mHttpDownloader;

    private ImageLoader()
    {
        // ��ȡӦ�ó����������ڴ�(ÿ��Ӧ��ϵͳ����32M)
        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;

        // ����ͼƬ�����СΪ�����������ڴ��1/8
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                return bitmap.getByteCount();
            }
        };

        mFileUtils = FileUtils.getInstance();
        mHttpDownloader = HttpDownloader.getInstance();
    }

    public static ImageLoader getInstance()
    {
        if (mImageLoader == null)
        {
            mImageLoader = new ImageLoader();
        }
        return mImageLoader;
    }

    /**
     * ��ȡ�̳߳صķ������漰�����������⣬����ͬ����
     * 
     * @return
     */
    private ExecutorService getThreadPool()
    {
        if (mImageThreadPool == null)
        {
            synchronized (ExecutorService.class)
            {
                if (mImageThreadPool == null)
                {
                    // Ϊ������ͼƬ���ӵ���������������5���߳�������ͼƬ
                    mImageThreadPool = Executors.newFixedThreadPool(CommonConstants.THREAD_NUM);
                }
            }
        }

        return mImageThreadPool;
    }

    /**
     * ��һ��ͼƬ�洢��LruCache�С�
     * 
     * @param key ����ͼƬ����Դurl
     *            like��  /ShowImageV1/Common/Passionate/leg/leg_001.jpg
     * @param bitmap ����Bitmap����
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (getBitmapFromMemoryCache(key) == null && bitmap != null)
        {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * ��LruCache�л�ȡһ��ͼƬ
     * 
     * @param key ����ͼƬ��URL��ַ
     * @return
     */
    public Bitmap getBitmapFromMemoryCache(String key)
    {
        return mMemoryCache.get(key);
    }

    /**
     * load image from resource
     * 
     * @param imageId
     * @return
     */
    // private Bitmap loadImage(String imageId)
    // {
    // if (imageId != null)
    // {
    // Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(getContext().getResources(),
    // imageId, columnWidth);
    // if (bitmap != null)
    // {
    // imageLoader.addBitmapToMemoryCache(imageId, bitmap);
    // return bitmap;
    // }
    // }
    // return null;
    // }

    /**
     * ���ּ��ط�����һ��ֻ��Ҫ��local��; ��һ�ֿ��Դӷ��������� �ȴ��ڴ滺���л�ȡBitmap,���û�оʹ�SD�������ֻ������л�ȡ��û�о�ȥ����������
     * 
     * @param isLoadFromLocal
     * @param url  �������
     *             1. �Ǳ���ͼƬ�� url like��                                                   /ShowImageV1/Common/Passionate/leg/leg_001.jpg
     *             2. �Ƿ������ϵ� url like�� http://192.168.0.100:8080/ShowImageV1/Common/Download/NO.261/NO.261.jpg
     *                ��ΪmMemoryCache�ŵ�key�Ǳ��ص�ַ����Ҫ��url
     *                ת��Ϊ                                                                                   /ShowImageV1/Common/Download/NO.261/NO.261.jpg
     * @param listener
     * @return
     */
    public void loadImage(final boolean canLoadFromHttp, final int columnWidth, final String url, final OnImageLoaderListener listener)
    {
        String temp = url;
        if (canLoadFromHttp) // FIXME �������������
        {
            // ��Ҫ�� http......ͳһת��Ϊlocal ��ַ
            temp = StrUtils.getFilePathFromHttpUrl(url);
        }

        final String localURL = temp;

        if (getBitmapFromMemoryCache(localURL) != null) // �ȴӻ�����ȡ
        {
            //Log.e("ImageLoader --> loadImage", "�ȴӻ�����ȡ");
            listener.onImageLoader(getBitmapFromMemoryCache(localURL), localURL);
        }
        else
        {
            // ���̼߳�UI�߳��µ�Handler,Ĭ�ϻ��и�looper,��ȥ������Ϣ����
            final Handler handler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);
                    listener.onImageLoader((Bitmap)msg.obj, localURL);
                }
            };

            getThreadPool().execute(new Runnable()
            {
                private void closeInputStreamAndDisConnect(InputStream inputStream)
                {
                    try
                    {
                        if (inputStream != null)
                        {
                            inputStream.close();
                            inputStream = null;
                        }
                        
                        // FIXME ���߳�ͬ���ǻ��������
                        //mHttpDownloader.disConnect();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void run()
                {
                    Bitmap bitmap = null;

                    if (FileUtils.isSDMounted())
                    {
                        bitmap = getLocalBitmap(localURL, columnWidth); // �ӱ���·������

                        if (bitmap == null)
                        {
                            // FIXME �жϷ������Ƿ�����
                            if (canLoadFromHttp) // �����Դӷ���������
                            {
                                Log.e("ImageLoader --> http download", "�ӷ�����������");
                                InputStream inputStream = mHttpDownloader.getInputStreamFromUrl(url);
                                if (inputStream != null) // connection exception FIXME
                                {
                                    byte[] data = inputStream2ByteArr(inputStream);//��InputStreamתΪbyte���飬���Զ�ζ�ȡ
                                    
                                    closeInputStreamAndDisConnect(inputStream);
                                    
                                    // ��SD����ȡ�ֻ������ȡBitmap bitmap�Ǿ���������˵�
                                    bitmap = decodeSampledBitmapFromStream(data, columnWidth);
                                }

                                if (bitmap != null)
                                {
                                    Log.e("ImageLoader --> http download", "�����������غ���뻺��ͬʱд���ļ�~");
                                    // ��bitmap �����ڴ滺��
                                    addBitmapToMemoryCache(localURL, bitmap);

                                    InputStream stream = mHttpDownloader.getInputStreamFromUrl(url);
                                    // ������SD��
                                    mFileUtils.write2SDFromInput(StrUtils.getDirOrFileFromHttpUrl(url, true),
                                            StrUtils.getDirOrFileFromHttpUrl(url, false), stream);

                                    closeInputStreamAndDisConnect(stream);
                                }
                            }
                        }

                    }
                    else
                    {
                        // FIXME
                        // "SD Mounted failed!"
                        // bitmap = BitmapFactory.decodeResource(getResources(),
                        // R.drawable.empty_photo);
                    }

                    Message msg = handler.obtainMessage();
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                }
            });

        }

    }

    /**
     * ������ȡ������bitmap���Ѿ� decode �õ�
     * 
     * ��ȡBitmap, �ڴ���û�о�ȥ�ֻ�����sd���л�ȡ
     * 
     * @param url
     * @return
     */
    public Bitmap getLocalBitmap(String localURL, int columnWidth)
    {
        if (mFileUtils.isFileExists(localURL) && mFileUtils.getFileSize(localURL) != 0)
        {
            //Log.e("ImageLoader --> getLocalBitmap", "�ӱ���·������");
            // ��SD����ȡ�ֻ������ȡBitmap
            Bitmap bitmap = decodeSampledBitmapFromFile(FileUtils.getImagePath(localURL), columnWidth);

            if (bitmap != null)
            {
                //Log.e("ImageLoader --> getLocalBitmap", "����·�����Һ���뻺��");
                // ��Bitmap �����ڴ滺��
                Log.e("getLocalBitmap", "localURL: " + localURL + " bitmapWidth: " + bitmap.getWidth() + " bitmapHeight: " + bitmap.getHeight());
                addBitmapToMemoryCache(localURL, bitmap);
            }
            return bitmap;
        }

        return null;
    }

    /**
     * ȡ���������ص�����
     */
    public synchronized void cancelTask()
    {
        if (mImageThreadPool != null)
        {
            mImageThreadPool.shutdownNow();
            mImageThreadPool = null;

            mHttpDownloader.disConnect();
        }
    }

    /**
     * �첽����ͼƬ�Ļص��ӿ�
     * 
     * @author len
     * 
     */
    public interface OnImageLoaderListener
    {
        void onImageLoader(Bitmap bitmap, String url);
    }

    /**
     * BitmapFactory.decodeStream �����м���
     * 
     * ע�⣺��һ�ζ�ȡInputStream�����ٴ�ͨ��InputStream���õ�bitmap��ʱ���Ϊ�գ�ֻ�ܶ�һ��
     * 
     * @param pathName
     * @param reqWidth
     * @return
     */
    public static Bitmap decodeSampledBitmapFromStream(byte[] data, int reqWidth)
    {
        // ��һ�ν�����inJustDecodeBounds����Ϊtrue������ȡͼƬ��С
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //BitmapFactory.decodeStream(inputStream, null, options);
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // �������涨��ķ�������inSampleSizeֵ
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // ʹ�û�ȡ����inSampleSizeֵ�ٴν���ͼƬ
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    private static byte[] inputStream2ByteArr(InputStream inputStream)
    {
        byte[] byteArr = null;
        ByteArrayOutputStream outputStream = null;
        try
        {
            outputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) != -1)
            {
                outputStream.write(buff, 0, len);
            }
            byteArr = outputStream.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                inputStream.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return byteArr;
    }

    /**
     * BitmapFactory.decodeFile ���ļ��м���
     * 
     * @param pathName
     * @param reqWidth
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth)
    {
        // ��һ�ν�����inJustDecodeBounds����Ϊtrue������ȡͼƬ��С
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(pathName, options);

        // �������涨��ķ�������inSampleSizeֵ
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // ʹ�û�ȡ����inSampleSizeֵ�ٴν���ͼƬ
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * BitmapFactory.decodeResource ����Ŀ��Դ�м���
     * 
     * @param resource
     * @param resourceId
     * @param reqWidth
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources resource, int resourceId, int reqWidth)
    {
        // ��һ�ν�����inJustDecodeBounds����Ϊtrue������ȡͼƬ��С
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(resource, resourceId, options);

        // �������涨��ķ�������inSampleSizeֵ
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // ʹ�û�ȡ����inSampleSizeֵ�ٴν���ͼƬ
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resource, resourceId, options);
    }

    /**
     * ��Ҫ���ص�ͼƬ����ͨ���������
     * ���ͼƬԭ���Ŀ�ȱ� reqWidth�󣬼�����Ҫ��С������������ԭ�ߴ����
     * 
     * @param options
     * @param reqWidth
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth)
    {
        // ԴͼƬ�Ŀ��
        final int width = options.outWidth; // ͼƬʵ�ʿ��
        int inSampleSize = 1;
        if (width > reqWidth)
        {
            // �����ʵ�ʿ�Ⱥ�Ŀ���ȵı���
            final int widthRatio = Math.round((float)width / (float)reqWidth);
            inSampleSize = widthRatio;
        }
        return inSampleSize;
    }

}
