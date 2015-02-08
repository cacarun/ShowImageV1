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
 * 异步加载图片, 每次保存5张图片到内存, 如果内存满, mImageLoader会处理
 * 用数组保存 Bitmap[] mBitmapArray 因为size事先确定了 onItemSelected 最后会偏到length位置 不好
 * 用List来存, 加载一张放一张, 里面图片顺序就不一致了 MyScrollView就是这种情况
 * 
 * 异步导致顺序不一致 FIXME
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

    // 记录当前已加载到第几页
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

        // 只有在gallery滑动停止的时候才会触发onitemselected事件
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
            Toast.makeText(this, "正在加载...", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "已没有更多图片", Toast.LENGTH_SHORT).show();
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
                    // 加载默认图片
                    mBitmapList.add(BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo));
                }
                else
                {
                    // 产生倒影图片
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
        // We only want the bottom half of the image 截取原图下半部分
        Bitmap reflectionImage = Bitmap.createBitmap(loadBitmap, 0, height / 2, width, height / 2, matrix, false);

        // Create a new bitmap with same width but taller to fit
        // reflection 创建倒影图片（高度为原图3/2）
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

        // Create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        // 以bitmap对象创建一个画布，则将内容都绘制在bitmap上，因此bitmap不得为null
        Canvas canvas = new Canvas(bitmapWithReflection);

        // Draw in the original image 绘制原图
        canvas.drawBitmap(loadBitmap, 0, 0, null);

        // Draw in the gap  绘制原图与倒影的间距
        Paint deafaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);

        // Draw in the reflection 绘制倒影图
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        // Create a shader that is a linear gradient that covers the
        // reflection  线性渐变效果
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, loadBitmap.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap,
                0x70ffffff, 0x00ffffff, TileMode.CLAMP);

        // Set the paint to use this shader (linear gradient)
        paint.setShader(shader);

        // Set the Transfer mode to be porter duff and destination in 倒影遮罩效果
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        // Draw a rectangle using the paint with our linear gradient 绘制倒影的阴影效果
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        return bitmapWithReflection;
    }
}
