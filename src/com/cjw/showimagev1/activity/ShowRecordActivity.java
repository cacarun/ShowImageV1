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
 * �첽��������ͼƬ, ������ͼƬ������, ��notifyDataSetChangedһ���Ը��½���
 * 
 * ������λ�ó�ʼ��adapter:
 * 1. ��onCreate����, ��ΪҪ����ȥmBitmapArray,���Ժ������ж��첽����������ͼƬ,
 *    Ҫ��notifyDataSetChangedһ���Ը��½���;(ShowRecordActivity)
 *    List<RecordInfo> mRecordInfos  --> updateRecord������ʼ�� ������Ա�֤˳��һ��.
 * 2. ���ж��첽����������ͼƬ, ��ʼ��adapter, ����һ�μ���.(ShowTypeActivity)
 *    Bitmap[] mBitmapArray          --> û�г�ʼ����ʽ, ֻ��ͨ�����鱣֤˳��һ��.
 * 
 * @author Burt.Cai
 *
 */
public class ShowRecordActivity extends Activity implements IXListViewListener
{
    //private static int COUNT;

    // �����ȴ������ֵ�����ѹ�� ���򲻻�
    private int mDefaultWidth = CommonConstants.IMAGE_LOAD_ORIGINAL_SIZE;// FIXME

    // �Ƿ�Ҫȥ������������Դ
    private boolean mIsOnLoadNeeded = false;// FIXME

    private XListView mListView;

    private List<RecordInfo> mRecordInfos;
    private ListImageAdapter mRecordAdapter;

    private Handler mHandler;

    // ��ͼƬ���й���Ĺ�����
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
     * xmlStr ��������Դ
     * 1. �ӷ������õ����°汾
     * 2. ���ش��ڵİ汾
     * 
     * @param xmlStr
     */
    private void updateRecord(String xmlStr)
    {
        mRecordInfos = XMLHelper.parseXML(xmlStr); // ÿ�ζ���new������
        mRecordAdapter.swapItems(mRecordInfos); // ����Դ����ͬһ����ʱ��᲻ͬ��

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
        mImageLoader.cancelTask(); // �ͷ��ڴ�

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
                mListView.setRefreshTime(new SimpleDateFormat("yyyy��MM��dd��", Locale.CHINA).format(new Date()));

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
                        Log.e("onImageLoader", "�ص� ÿһ�� ��̬����");
                        mRecordInfos.get(pos).setBitmap(bitmap);
                        mRecordAdapter.notifyDataSetChanged(); // FIXME ���ε���getView, �����ô��ǿ������ϸ���UI
                    }

                    // FIXME �����������ߣ�������ͼƬ�����أ�ʣ�µ�ͼƬҪ��Ĭ�ϵ�ͼƬ���ǲ���ʾ����
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
     * �ȼ��ر�����Դ�����û����ȥ����������
     * ȡ�÷���Ŀ¼�����·���
     * 
     * 1. ��һ��xmlStrΪ�գ���Ҫ����
     * 2. ֮��xmlStr��Ϊ�գ�ֻ��ӱ��ؼ���
     * 3. onload��������Ҫ��ȥ���������أ�����ͱ���һ�£����ø��£������滻���ص�xml��
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