package com.cjw.showimagev1.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.GridView;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.util.ImageLoader;
import com.cjw.showimagev1.util.ImageLoader.OnImageLoaderListener;
import com.cjw.showimagev1.util.Images;
import com.cjw.showimagev1.view.adapter.GridImageAdapter;

/**
 * �첽��������ͼƬ, ������ͼƬ������, ��notifyDataSetChangedһ���Ը��½���
 * 
 * Bitmap[] mBitmapArray ������Ա�֤˳����ʾͼƬ
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
public class ShowTypeActivity extends Activity
{
    //private static final int REQUEST_CODE_SHOW_WAIT = 1;

    private List<String> mLocalSharedImagePaths; // �Լ������Ŀ¼

    private GridView mGridView;
    private GridImageAdapter mGridImageAdapter;

    private ImageLoader mImageLoader;

    private int mDefaultWidth = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_type);

//        RelativeLayout showTypeLayout = (RelativeLayout)findViewById(R.id.show_type_layout);
//        if (ShowImageApp.IS_VIP)
//        {
//            showTypeLayout.setBackgroundResource(R.drawable.vip_bg);
//        }
//        else
//        {
//            showTypeLayout.setBackgroundResource(R.drawable.common_bg);
//        }

        loadLocalImagePaths(); // will init Images.COVER_INFOS

        mImageLoader = ImageLoader.getInstance();

        mGridView = (GridView)findViewById(R.id.show_type_gridview);

        mGridImageAdapter = new GridImageAdapter(ShowTypeActivity.this, Images.COVER_INFOS); 
        mGridView.setAdapter(mGridImageAdapter);

        //showWaitScreen(getResources().getString(R.string.wait_for_load_pic), REQUEST_CODE_SHOW_WAIT);
        loadImages();

    }
    
    private void loadImages()
    {
        for (int i = 0; i < Images.COVER_INFOS.size(); i++)
        {
            final int pos = i;
            mImageLoader.loadImage(false, mDefaultWidth, Images.COVER_INFOS.get(i).getCoverUrl(), new OnImageLoaderListener()
            {
                @Override
                public void onImageLoader(Bitmap bitmap, String url)
                {
                    if (bitmap != null)
                    {
                        int tmp = bitmap.getWidth() >= bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth();
                        int cubeLength = tmp >= mDefaultWidth ? mDefaultWidth : tmp;

                        //else mBitmapArray[pos] = BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
                        Images.COVER_INFOS.get(pos).setBitmap(Bitmap.createBitmap(bitmap, 0, 0, cubeLength, cubeLength, null, false));
                        mGridImageAdapter.notifyDataSetChanged();
                    }

                }
            });
        }
    }

    /**
     * Shows wait screen.
     */
//    private void showWaitScreen(String message, int requestCode)
//    {
//        Intent intent = new Intent(ShowTypeActivity.this, WaitScreenActivity.class);
//        intent.putExtra(WaitScreenActivity.KEY_MESSAGE, message);
//
//        startActivityForResult(intent, requestCode);
//    }
    
    private void saveLocalImagePaths()
    {
        if (mLocalSharedImagePaths != null && mLocalSharedImagePaths.size() != 0)
        {
            SharedPreferences preferences = getSharedPreferences(CommonConstants.SHOW_IMAGE_PREFERENCE, MODE_PRIVATE);
            Editor editor= preferences.edit();
            editor.putInt(CommonConstants.LOCAL_IMAGE_PATH, mLocalSharedImagePaths.size());
            
            for(int i=0; i<mLocalSharedImagePaths.size(); i++)
            {
                editor.remove(CommonConstants.LOCAL_IMAGE_PATH_CHILD + i);
                editor.putString(CommonConstants.LOCAL_IMAGE_PATH_CHILD + i, mLocalSharedImagePaths.get(i));
            }
            editor.commit();
        }
   }
    
    private void loadLocalImagePaths()
    {  
        mLocalSharedImagePaths = new ArrayList<String>();
        SharedPreferences preferences = getSharedPreferences(CommonConstants.SHOW_IMAGE_PREFERENCE, MODE_PRIVATE);
        int size = preferences.getInt(CommonConstants.LOCAL_IMAGE_PATH, 0);
        for(int i=0;i<size;i++)
        {
            mLocalSharedImagePaths.add(preferences.getString(CommonConstants.LOCAL_IMAGE_PATH_CHILD + i, null));
        }
        
        Images.initLocalImageUrls(mLocalSharedImagePaths);
    }
    
}
