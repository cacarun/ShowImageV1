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
 * 对图片进行管理的工具类。
 */
public class ImageLoader
{

    // 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉
    private LruCache<String, Bitmap> mMemoryCache; // 是否用static比较好 FIXME

    private static ImageLoader mImageLoader;

    // 下载的线程池
    private ExecutorService mImageThreadPool = null; // 是否用static比较好 FIXME

    private FileUtils mFileUtils;
    private HttpDownloader mHttpDownloader;

    private ImageLoader()
    {
        // 获取应用程序最大可用内存(每个应用系统分配32M)
        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;

        // 设置图片缓存大小为程序最大可用内存的1/8
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
     * 获取线程池的方法，涉及到并发的问题，加上同步锁
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
                    // 为了下载图片更加的流畅，我们用了5个线程来下载图片
                    mImageThreadPool = Executors.newFixedThreadPool(CommonConstants.THREAD_NUM);
                }
            }
        }

        return mImageThreadPool;
    }

    /**
     * 将一张图片存储到LruCache中。
     * 
     * @param key 传入图片的资源url
     *            like：  /ShowImageV1/Common/Passionate/leg/leg_001.jpg
     * @param bitmap 传入Bitmap对象
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap)
    {
        if (getBitmapFromMemoryCache(key) == null && bitmap != null)
        {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从LruCache中获取一张图片
     * 
     * @param key 传入图片的URL地址
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
     * 两种加载方法，一种只需要从local读; 另一种可以从服务器下载 先从内存缓存中获取Bitmap,如果没有就从SD卡或者手机缓存中获取，没有就去服务器下载
     * 
     * @param isLoadFromLocal
     * @param url  两种情况
     *             1. 是本地图片， url like：                                                   /ShowImageV1/Common/Passionate/leg/leg_001.jpg
     *             2. 是服务器上的 url like： http://192.168.0.100:8080/ShowImageV1/Common/Download/NO.261/NO.261.jpg
     *                因为mMemoryCache放的key是本地地址，需要把url
     *                转换为                                                                                   /ShowImageV1/Common/Download/NO.261/NO.261.jpg
     * @param listener
     * @return
     */
    public void loadImage(final boolean canLoadFromHttp, final int columnWidth, final String url, final OnImageLoaderListener listener)
    {
        String temp = url;
        if (canLoadFromHttp) // FIXME 服务器在线与否
        {
            // 需要把 http......统一转换为local 地址
            temp = StrUtils.getFilePathFromHttpUrl(url);
        }

        final String localURL = temp;

        if (getBitmapFromMemoryCache(localURL) != null) // 先从缓存中取
        {
            //Log.e("ImageLoader --> loadImage", "先从缓存中取");
            listener.onImageLoader(getBitmapFromMemoryCache(localURL), localURL);
        }
        else
        {
            // 主线程即UI线程下的Handler,默认会有个looper,回去处理消息队列
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
                        
                        // FIXME 多线程同步是会有问题的
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
                        bitmap = getLocalBitmap(localURL, columnWidth); // 从本地路径查找

                        if (bitmap == null)
                        {
                            // FIXME 判断服务器是否在线
                            if (canLoadFromHttp) // 还可以从服务器下载
                            {
                                Log.e("ImageLoader --> http download", "从服务器上下载");
                                InputStream inputStream = mHttpDownloader.getInputStreamFromUrl(url);
                                if (inputStream != null) // connection exception FIXME
                                {
                                    byte[] data = inputStream2ByteArr(inputStream);//将InputStream转为byte数组，可以多次读取
                                    
                                    closeInputStreamAndDisConnect(inputStream);
                                    
                                    // 从SD卡获取手机里面获取Bitmap bitmap是经过处理过了的
                                    bitmap = decodeSampledBitmapFromStream(data, columnWidth);
                                }

                                if (bitmap != null)
                                {
                                    Log.e("ImageLoader --> http download", "服务器上下载后放入缓存同时写入文件~");
                                    // 将bitmap 加入内存缓存
                                    addBitmapToMemoryCache(localURL, bitmap);

                                    InputStream stream = mHttpDownloader.getInputStreamFromUrl(url);
                                    // 保存在SD卡
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
     * 从这里取出来的bitmap是已经 decode 好的
     * 
     * 获取Bitmap, 内存中没有就去手机或者sd卡中获取
     * 
     * @param url
     * @return
     */
    public Bitmap getLocalBitmap(String localURL, int columnWidth)
    {
        if (mFileUtils.isFileExists(localURL) && mFileUtils.getFileSize(localURL) != 0)
        {
            //Log.e("ImageLoader --> getLocalBitmap", "从本地路径查找");
            // 从SD卡获取手机里面获取Bitmap
            Bitmap bitmap = decodeSampledBitmapFromFile(FileUtils.getImagePath(localURL), columnWidth);

            if (bitmap != null)
            {
                //Log.e("ImageLoader --> getLocalBitmap", "本地路径查找后放入缓存");
                // 将Bitmap 加入内存缓存
                Log.e("getLocalBitmap", "localURL: " + localURL + " bitmapWidth: " + bitmap.getWidth() + " bitmapHeight: " + bitmap.getHeight());
                addBitmapToMemoryCache(localURL, bitmap);
            }
            return bitmap;
        }

        return null;
    }

    /**
     * 取消正在下载的任务
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
     * 异步下载图片的回调接口
     * 
     * @author len
     * 
     */
    public interface OnImageLoaderListener
    {
        void onImageLoader(Bitmap bitmap, String url);
    }

    /**
     * BitmapFactory.decodeStream 从流中加载
     * 
     * 注意：第一次读取InputStream流后，再次通过InputStream流得到bitmap的时候就为空，只能读一次
     * 
     * @param pathName
     * @param reqWidth
     * @return
     */
    public static Bitmap decodeSampledBitmapFromStream(byte[] data, int reqWidth)
    {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //BitmapFactory.decodeStream(inputStream, null, options);
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // 使用获取到的inSampleSize值再次解析图片
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
     * BitmapFactory.decodeFile 从文件中加载
     * 
     * @param pathName
     * @param reqWidth
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth)
    {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(pathName, options);

        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * BitmapFactory.decodeResource 从项目资源中加载
     * 
     * @param resource
     * @param resourceId
     * @param reqWidth
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources resource, int resourceId, int reqWidth)
    {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(resource, resourceId, options);

        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resource, resourceId, options);
    }

    /**
     * 需要加载的图片都会通过这个方法
     * 如果图片原来的宽度比 reqWidth大，计算需要缩小到倍数，否则原尺寸加载
     * 
     * @param options
     * @param reqWidth
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth)
    {
        // 源图片的宽度
        final int width = options.outWidth; // 图片实际宽度
        int inSampleSize = 1;
        if (width > reqWidth)
        {
            // 计算出实际宽度和目标宽度的比率
            final int widthRatio = Math.round((float)width / (float)reqWidth);
            inSampleSize = widthRatio;
        }
        return inSampleSize;
    }

}
