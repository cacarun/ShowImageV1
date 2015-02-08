package com.cjw.showimagev1.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.util.ImageLoader;
import com.cjw.showimagev1.util.ImageLoader.OnImageLoaderListener;
import com.cjw.showimagev1.util.Images;
import com.cjw.showimagev1.view.GalleryFlow;
import com.cjw.showimagev1.view.adapter.GalleryImageAdapter;

/**
 * �첽����ͼƬ, ÿ�α���5��ͼƬ���ڴ�, ����ڴ���, mImageLoader�ᴦ��
 * �����鱣�� Bitmap[] mBitmapArray ��Ϊsize����ȷ���� onItemSelected ����ƫ��lengthλ�� ����
 * ��List����, ����һ�ŷ�һ��, ����ͼƬ˳��Ͳ�һ���� MyScrollView�����������
 * 
 * �첽����˳��һ�� FIXME
 * 
 * @author Burt.Cai
 *
 */
public class GalleryCoverFlow extends Activity
{
    private List<String> mImageUrls;

    private int mDefaultWidth = 500;
    private ImageLoader mImageLoader;

    private List<Bitmap> mBitmapList;

    private GalleryImageAdapter mGalleryAdapter;
    private GalleryFlow mGalleryFlow;

    // ��¼��ǰ�Ѽ��ص��ڼ�ҳ
    private int mPage;
    private int mEndIndex;
    private int mStartIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_cover_flow);

        mImageLoader = ImageLoader.getInstance();
        mImageUrls = Images.contentUrls.get(getIntent().getIntExtra(CommonConstants.RECORD_LIST_POS, 0));

        mBitmapList = new ArrayList<Bitmap>();

        mGalleryAdapter = new GalleryImageAdapter(GalleryCoverFlow.this, mBitmapList);
        mGalleryFlow = (GalleryFlow)findViewById(R.id.gallery_flow);
        mGalleryFlow.setAdapter(mGalleryAdapter);

        // ֻ����gallery����ֹͣ��ʱ��Żᴥ��onitemselected�¼�
        mGalleryFlow.setCallbackDuringFling(false);
        mGalleryFlow.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.e("onItemSelected", "positionPos: " + position);
                if (position <= mEndIndex && position >= mStartIndex + 2)
                {
                    loadMore();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // TODO Auto-generated method stub
            }
        });

        loadMore();
    }

    private void loadMore()
    {
        mStartIndex = mPage * CommonConstants.COMMON_PAGE_SIZE;
        mEndIndex = mPage * CommonConstants.COMMON_PAGE_SIZE + CommonConstants.COMMON_PAGE_SIZE;
        if (mStartIndex < mImageUrls.size())
        {
            Toast.makeText(this, "���ڼ���...", Toast.LENGTH_SHORT).show();
            if (mEndIndex > mImageUrls.size())
            {
                mEndIndex = mImageUrls.size();
            }
            for (int i = mStartIndex; i < mEndIndex; i++)
            {
                loadImages(i);
            }

            mPage++;
        }
        else
        {
            Toast.makeText(this, "��û�и���ͼƬ", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadImages(int position)
    {
        mImageLoader.loadImage(true, mDefaultWidth, mImageUrls.get(position), new OnImageLoaderListener()
        {
            @Override
            public void onImageLoader(Bitmap bitmap, String url)
            {
                if (bitmap == null)
                {
                    // ����Ĭ��ͼƬ
                    mBitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo));
                }
                else
                {
                    // ������ӰͼƬ
                    mBitmapList.add(createReflectedBitmap(bitmap));
                }

                mGalleryAdapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * Create Reflected Bitmap
     * 
     * @param loadBitmap
     * @param pos
     * @return
     */
    private synchronized Bitmap createReflectedBitmap(Bitmap loadBitmap)
    {
        // The gap we want between the reflection and the original image
        int reflectionGap = 10;

        if (loadBitmap == null)
        {
            return BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
        }

        int width = loadBitmap.getWidth();
        int height = loadBitmap.getHeight();

        // This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        // Create a Bitmap with the flip matrix applied to it.
        // We only want the bottom half of the image ��ȡԭͼ�°벿��
        Bitmap reflectionImage = Bitmap.createBitmap(loadBitmap, 0, height / 2, width, height / 2, matrix, false);

        // Create a new bitmap with same width but taller to fit
        // reflection ������ӰͼƬ���߶�Ϊԭͼ3/2��
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

        // Create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        // ��bitmap���󴴽�һ�������������ݶ�������bitmap�ϣ����bitmap����Ϊnull
        Canvas canvas = new Canvas(bitmapWithReflection);

        // Draw in the original image ����ԭͼ
        canvas.drawBitmap(loadBitmap, 0, 0, null);

        // Draw in the gap  ����ԭͼ�뵹Ӱ�ļ��
        Paint deafaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);

        // Draw in the reflection ���Ƶ�Ӱͼ
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        // Create a shader that is a linear gradient that covers the
        // reflection  ���Խ���Ч��
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, loadBitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap,
                0x70ffffff, 0x00ffffff, TileMode.CLAMP);

        // Set the paint to use this shader (linear gradient)
        paint.setShader(shader);

        // Set the Transfer mode to be porter duff and destination in ��Ӱ����Ч��
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        // Draw a rectangle using the paint with our linear gradient ���Ƶ�Ӱ����ӰЧ��
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        return bitmapWithReflection;
    }
}
