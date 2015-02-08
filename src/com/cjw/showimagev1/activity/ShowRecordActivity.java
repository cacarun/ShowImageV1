package com.cjw.showimagev1.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.feedback.Feedback;
import com.cjw.showimagev1.model.RecordInfo;
import com.cjw.showimagev1.util.FileUtils;
import com.cjw.showimagev1.util.ImageLoader;
import com.cjw.showimagev1.util.ImageLoader.OnImageLoaderListener;
import com.cjw.showimagev1.util.Images;
import com.cjw.showimagev1.util.StrUtils;
import com.cjw.showimagev1.util.xml.XMLHelper;
import com.cjw.showimagev1.view.adapter.ListImageAdapter;
import com.cjw.showimagev1.view.xlistview.XListView;
import com.cjw.showimagev1.view.xlistview.XListView.IXListViewListener;
import com.cjw.showimagev1.work.DownloadXMLWork;

/**
 * 异步加载所有图片, 对所有图片加载完, 用notifyDataSetChanged一次性更新界面
 * 
 * 有两种位置初始化adapter:
 * 1. 在onCreate里面, 因为要传进去mBitmapArray,所以后面在判断异步加载完所有图片,
 *    要用notifyDataSetChanged一次性更新界面;(ShowRecordActivity)
 *    List<RecordInfo> mRecordInfos  --> updateRecord方法初始化 后面可以保证顺序一致.
 * 2. 在判断异步加载完所有图片, 初始化adapter, 进行一次加载.(ShowTypeActivity)
 *    Bitmap[] mBitmapArray          --> 没有初始化方式, 只能通过数组保证顺序一致.
 * 
 * @author Burt.Cai
 *
 */
public class ShowRecordActivity extends Activity implements IXListViewListener
{
    //private static int COUNT;

    // 如果宽度大于这个值会进行压缩 否则不会
    private int mDefaultWidth = CommonConstants.IMAGE_LOAD_ORIGINAL_SIZE;// FIXME

    // 是否要去服务器更新资源
    private boolean mIsOnLoadNeeded = false;// FIXME

    private XListView mListView;

    private List<RecordInfo> mRecordInfos;
    private ListImageAdapter mRecordAdapter;

    private Handler mHandler;

    // 对图片进行管理的工具类
    private ImageLoader mImageLoader;

    private FrameLayout mShowRecordLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_record);

        mShowRecordLayout = (FrameLayout)findViewById(R.id.show_record_layout);
        if (ShowImageApp.IS_VIP)
        {
            mShowRecordLayout.setBackgroundResource(R.drawable.vip_bg);
        }
        else
        {
            mShowRecordLayout.setBackgroundResource(R.drawable.common_bg);
        }

        mRecordInfos = new ArrayList<RecordInfo>();
        mImageLoader = ImageLoader.getInstance();

        mListView = (XListView)findViewById(R.id.xListView);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(this);

        mRecordAdapter = new ListImageAdapter(this, mRecordInfos);
        mListView.setAdapter(mRecordAdapter);

        mHandler = new Handler();

        Log.e("onCreate", "1. updateItems");
        updateXMLAndRecordCover(); // will download cover images
    }

    private Feedback feedback = new Feedback()
    {
        @Override
        public void callBackOperator(int status, String xmlStr)
        {
            if (status == CommonConstants.STATUS_DIFF && !StrUtils.isEmpty(xmlStr))
            {
                updateRecord(xmlStr);
            }
            else if (status == CommonConstants.STATUS_SAME && StrUtils.isEmpty(xmlStr))
            {
                Toast.makeText(ShowRecordActivity.this, "Now is the newest!", Toast.LENGTH_SHORT).show();
            }
            else if (status == CommonConstants.STATUS_OFFLINE && StrUtils.isEmpty(xmlStr))
            {
                Toast.makeText(ShowRecordActivity.this, "Server is Offline!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * xmlStr 有两种来源
     * 1. 从服务器拿到的新版本
     * 2. 本地存在的版本
     * 
     * @param xmlStr
     */
    private void updateRecord(String xmlStr)
    {
        mRecordInfos = XMLHelper.parseXML(xmlStr); // 每次都是new出来的
        mRecordAdapter.swapItems(mRecordInfos); // 数据源不是同一个的时候会不同步

        // will init child image urls
        Images.initHttpImageUrls(mRecordInfos);

        // download http image covers
        loadImageCover();
    }

    @Override
    public void onRefresh()
    {
        onLoad();
    }

    @Override
    public void onLoadMore()
    {
        onLoad();
    }

    @Override
    protected void onDestroy()
    {
        mImageLoader.cancelTask(); // 释放内存

        super.onDestroy();
    }

    private void onLoad()
    {
        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mListView.stopRefresh();
                mListView.stopLoadMore();
                mListView.setRefreshTime(new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(new Date()));

                Log.e("onLoad", "go to updateItems...isOnLoadNeeded is true");
                mIsOnLoadNeeded = true;
                updateXMLAndRecordCover();
            }
        }, 2000);
    }

    private void loadImageCover()
    {
        //COUNT = mRecordInfos.size();

        for (int i = 0; i < mRecordInfos.size(); i++)
        {
            final int pos = i;
            RecordInfo coverInfo = mRecordInfos.get(pos);
            mImageLoader.loadImage(true, mDefaultWidth, coverInfo.getHttpUrl(), new OnImageLoaderListener()
            {
                @Override
                public void onImageLoader(Bitmap bitmap, String url)
                {
                    if (bitmap != null)
                    {
                        Log.e("onImageLoader", "回调 每一张 动态更新");
                        mRecordInfos.get(pos).setBitmap(bitmap);
                        mRecordAdapter.notifyDataSetChanged(); // FIXME 会多次调用getView, 不过好处是可以马上更新UI
                    }

                    // FIXME 服务器不在线，而部分图片已下载，剩下的图片要用默认的图片吗还是不显示好了
//                    if (--COUNT <= 0)
//                    {
//                        mRecordAdapter.notifyDataSetChanged();
//                    }
//                    Log.e("COUNT", "COUNT: " + COUNT);
                }

            });
        }
    }

    /**
     * 先加载本地资源，如果没有则去服务器下载
     * 取得封面目录，更新封面
     * 
     * 1. 第一次xmlStr为空，需要下载
     * 2. 之后xmlStr不为空，只需从本地加载
     * 3. onload方法主动要求去服务器下载（如果和本地一致，不用更新，否则替换本地的xml）
     * 
     */
    private void updateXMLAndRecordCover()
    {
        String serverXMLPath = null;
        String localXMLPath = null;
        if (ShowImageApp.IS_VIP)
        {
            serverXMLPath = CommonConstants.SERVER_XML_PATH_FOR_VIP;
            localXMLPath = CommonConstants.LOCAL_XML_PATH_FOR_VIP;
        }
        else
        {
            serverXMLPath = CommonConstants.SERVER_XML_PATH_FOR_COMMON;
            localXMLPath = CommonConstants.LOCAL_XML_PATH_FOR_COMMON;
        }

        String xmlStr = FileUtils.getXMLFromSD(localXMLPath);
        if (StrUtils.isEmpty(xmlStr) || mIsOnLoadNeeded)
        {
            mIsOnLoadNeeded = false;

            DownloadXMLWork dXmlWork = new DownloadXMLWork(feedback, serverXMLPath, localXMLPath);
            dXmlWork.execute();
        }
        else
        {
            Log.e("updateXMLAndRecordCover", "local exist.................");
            updateRecord(xmlStr);
        }
    }

}