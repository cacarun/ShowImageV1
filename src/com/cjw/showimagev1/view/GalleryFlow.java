package com.cjw.showimagev1.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * 因为在coverflow中图片切换是有旋转和缩放效果的，而自带的gallery中并没有实现。 因此，我们扩展自带的gallery，实现自己的galleryflow。在原gallery类中，
 * 提供了一个方法getChildStaticTransformation()以实现对图片的变换。
 * 我们通过覆写这个方法并在其中调用自定义的transformImageBitmap(“每个图片与gallery中心的距离”)方法，
 * 即可实现每个图片做相应的旋转和缩放。其中使用了camera和matrix用于视图变换。
 * 
 * Gallery控件过期?
 * 
 * 我怀疑Gallery过期的原因是因为它的适配器不能合适的转换视图，它每次切换图片时都要新建视图造成浪费太多的资源。 另外你可以选择使用The third part created
 * ecogallery，它克服了gallery不能回收视图的缺点；但不幸的是，通过网络只在pastebin上找到它的有关资料。(这句话无视吧，那个链接访问不了)
 * 好了，问题已经明确了，Android源生的gallery的确因为生理缺陷已经被抛弃了，投入到其他开源Gallery系列的怀抱吧。
 * 另外打开SDK的文档，找到Gallery看到有这么一句话：This class is deprecated.This widget is no longer supported. Other
 * horizontally scrolling widgets include HorizontalScrollView and ViewPager from the support
 * library. 同样的，官方提示用HSV和VP代替。
 * 
 * @author Burt.Cai
 * 
 */
public class GalleryFlow extends Gallery
{

    private Camera mCamera = new Camera();
    private int mMaxRotationAngle = 20; //60
    private int mMaxZoom = -180;
    private int mCoveflowCenter;

    public GalleryFlow(Context context)
    {
        super(context);
        this.setStaticTransformationsEnabled(true);
    }

    public GalleryFlow(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.setStaticTransformationsEnabled(true);
    }

    public GalleryFlow(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.setStaticTransformationsEnabled(true);
    }

    public int getMaxRotationAngle()
    {
        return mMaxRotationAngle;
    }

    public void setMaxRotationAngle(int maxRotationAngle)
    {
        mMaxRotationAngle = maxRotationAngle;
    }

    public int getMaxZoom()
    {
        return mMaxZoom;
    }

    public void setMaxZoom(int maxZoom)
    {
        mMaxZoom = maxZoom;
    }

    /** 获取Gallery的中心x */
    private int getCenterOfCoverflow()
    {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    /** 获取View的中心x */
    private static int getCenterOfView(View view)
    {
        return view.getLeft() + view.getWidth() / 2;
    }

    protected boolean getChildStaticTransformation(View child, Transformation t)
    {

        //图像的中心点和宽度
        final int childCenter = getCenterOfView(child);
        final int childWidth = child.getWidth();
        int rotationAngle = 0;

        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX); // alpha 和 matrix 都变换

        if (childCenter == mCoveflowCenter)
        {
         // 正中间的childView
            transformImageBitmap((ImageView)child, t, 0);
        }
        else
        {
         // 两侧的childView
            rotationAngle = (int)(((float)(mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
            if (Math.abs(rotationAngle) > mMaxRotationAngle)
            {
                rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
            }
          //根据偏移角度对图片进行处理，看上去有3D的效果。
            transformImageBitmap((ImageView)child, t, rotationAngle);
        }

        return true;
    }

    // 在改变大小的时候，重新计算滑动切换时需要旋转变化的中心
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mCoveflowCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void transformImageBitmap(ImageView child, Transformation t, int rotationAngle)
    {
        mCamera.save();
        final Matrix imageMatrix = t.getMatrix();
        final int imageHeight = child.getLayoutParams().height;
        final int imageWidth = child.getLayoutParams().width;
        final int rotation = Math.abs(rotationAngle);

        // 在Z轴上正向移动camera的视角，实际效果为放大图片。
        // 如果在Y轴上移动，则图片上下移动；X轴上对应图片左右移动。
        mCamera.translate(0.0f, 0.0f, 100.0f);

        // As the angle of the view gets less, zoom in
        if (rotation < mMaxRotationAngle)
        {
            float zoomAmount = (float)(mMaxZoom + (rotation * 1.5));
            mCamera.translate(0.0f, 0.0f, zoomAmount);
        }

        // 在Y轴上旋转，对应图片竖向向里翻转。
        // 如果在X轴上旋转，则对应图片横向向里翻转。
        mCamera.rotateY(rotationAngle);
        mCamera.getMatrix(imageMatrix);
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
        mCamera.restore();
    }
}
