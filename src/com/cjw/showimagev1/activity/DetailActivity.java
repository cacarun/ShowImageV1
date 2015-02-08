package com.cjw.showimagev1.activity;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.util.FileUtils;
import com.cjw.showimagev1.util.ImageLoader;
import com.cjw.showimagev1.util.ImageLoader.OnImageLoaderListener;
import com.cjw.showimagev1.util.Images;
import com.cjw.showimagev1.view.ZoomImageView;

public class DetailActivity extends Activity implements OnPageChangeListener
{
    /**
     * ���ڹ���ͼƬ�Ļ���
     */
    private ViewPager mViewPager;

    private ViewPagerAdapter mAdapter;

    /**
     * ��ʾ��ǰͼƬ��ҳ��
     */
    private TextView mPageText;

    private int mImagePosition;

    private static List<String> mImageUrls; // ��ǰͼƬ��Դ // FIXME static
    private Bitmap[] mBitmapArray;

    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = getIntent().getExtras();
        mImagePosition = bundle.getInt(CommonConstants.IMAGE_POSITION_ID);

        // ��ǰ����ͼƬ
        mImageUrls = Images.getImageUrlsByPath(ShowImageApp.TYPE_PATH);
        mBitmapArray = new Bitmap[mImageUrls.size()];
        mImageLoader = ImageLoader.getInstance();

        mPageText = (TextView)findViewById(R.id.page_text);
        mViewPager = (ViewPager)findViewById(R.id.view_pager);

        mAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        // ֻ���浱ǰ����������Ҹ�����
        // mViewPager.setOffscreenPageLimit(CommonConstants.VIEW_PAGE_OFF_SCREEN_LIMIT);
        mViewPager.setCurrentItem(mImagePosition);
        mViewPager.setOnPageChangeListener(this);
        // �趨��ǰ��ҳ������ҳ��
        mPageText.setText((mImagePosition + 1) + "/" + mImageUrls.size());
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }

    @Override
    public void onPageSelected(int currentPage)
    {
        // ÿ��ҳ�������ı�ʱ�����趨һ�鵱ǰ��ҳ������ҳ��
        mPageText.setText((currentPage + 1) + "/" + mImageUrls.size());
    }

    @Override
    protected void onDestroy()
    {
        mImageLoader.cancelTask(); // �ͷ��ڴ�

        super.onDestroy();
    }

    /**
     * ViewPager��������
     * 
     * @author guolin
     */
    class ViewPagerAdapter extends PagerAdapter
    {

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {

            final int pos = position;
            mImageLoader.loadImage(false, 400, mImageUrls.get(position), new OnImageLoaderListener()
            {
                @Override
                public void onImageLoader(Bitmap bitmap, String url)
                {
                    // TODO Auto-generated method stub
                    if (bitmap == null)
                    {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
                    }

                    mBitmapArray[pos] = bitmap;
                }
            });
            // ViewPage ֻ���浱ǰ����������Ҹ����
            // FIXME OOM
            // Bitmap bitmap =
            // BitmapFactory.decodeFile(FileUtils.getImagePath(mImageUrls.get(position)));

            Log.e("instantiateItem", "instantiateItem: " + position);

            if (mBitmapArray[position] == null)
            {
                mBitmapArray[position] = BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
            }
            // ��� �Ŵ���С��Ч��
            View myView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.activity_zoom, null);
            ZoomImageView zoomImageView = (ZoomImageView)myView.findViewById(R.id.detail_image);
            zoomImageView.setImageBitmap(mBitmapArray[position]);
            container.addView(myView);

            return myView;
        }

        @Override
        public int getCount()
        {
            return mImageUrls.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            Log.e("destroyItem", "destroyItem: " + position);
            View view = (View)object;
            container.removeView(view);
        }
    }
}