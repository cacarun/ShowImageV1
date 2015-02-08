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
 * ��Ϊ��coverflow��ͼƬ�л�������ת������Ч���ģ����Դ���gallery�в�û��ʵ�֡� ��ˣ�������չ�Դ���gallery��ʵ���Լ���galleryflow����ԭgallery���У�
 * �ṩ��һ������getChildStaticTransformation()��ʵ�ֶ�ͼƬ�ı任��
 * ����ͨ����д��������������е����Զ����transformImageBitmap(��ÿ��ͼƬ��gallery���ĵľ��롱)������
 * ����ʵ��ÿ��ͼƬ����Ӧ����ת�����š�����ʹ����camera��matrix������ͼ�任��
 * 
 * Gallery�ؼ�����?
 * 
 * �һ���Gallery���ڵ�ԭ������Ϊ�������������ܺ��ʵ�ת����ͼ����ÿ���л�ͼƬʱ��Ҫ�½���ͼ����˷�̫�����Դ�� ���������ѡ��ʹ��The third part created
 * ecogallery�����˷���gallery���ܻ�����ͼ��ȱ�㣻�����ҵ��ǣ�ͨ������ֻ��pastebin���ҵ������й����ϡ�(��仰���Ӱɣ��Ǹ����ӷ��ʲ���)
 * ���ˣ������Ѿ���ȷ�ˣ�AndroidԴ����gallery��ȷ��Ϊ����ȱ���Ѿ��������ˣ�Ͷ�뵽������ԴGalleryϵ�еĻ����ɡ�
 * �����SDK���ĵ����ҵ�Gallery��������ôһ�仰��This class is deprecated.This widget is no longer supported. Other
 * horizontally scrolling widgets include HorizontalScrollView and ViewPager from the support
 * library. ͬ���ģ��ٷ���ʾ��HSV��VP���档
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

    /** ��ȡGallery������x */
    private int getCenterOfCoverflow()
    {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    /** ��ȡView������x */
    private static int getCenterOfView(View view)
    {
        return view.getLeft() + view.getWidth() / 2;
    }

    protected boolean getChildStaticTransformation(View child, Transformation t)
    {

        //ͼ������ĵ�Ϳ��
        final int childCenter = getCenterOfView(child);
        final int childWidth = child.getWidth();
        int rotationAngle = 0;

        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX); // alpha �� matrix ���任

        if (childCenter == mCoveflowCenter)
        {
         // ���м��childView
            transformImageBitmap((ImageView)child, t, 0);
        }
        else
        {
         // �����childView
            rotationAngle = (int)(((float)(mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
            if (Math.abs(rotationAngle) > mMaxRotationAngle)
            {
                rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
            }
          //����ƫ�ƽǶȶ�ͼƬ���д�������ȥ��3D��Ч����
            transformImageBitmap((ImageView)child, t, rotationAngle);
        }

        return true;
    }

    // �ڸı��С��ʱ�����¼��㻬���л�ʱ��Ҫ��ת�仯������
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

        // ��Z���������ƶ�camera���ӽǣ�ʵ��Ч��Ϊ�Ŵ�ͼƬ��
        // �����Y�����ƶ�����ͼƬ�����ƶ���X���϶�ӦͼƬ�����ƶ���
        mCamera.translate(0.0f, 0.0f, 100.0f);

        // As the angle of the view gets less, zoom in
        if (rotation < mMaxRotationAngle)
        {
            float zoomAmount = (float)(mMaxZoom + (rotation * 1.5));
            mCamera.translate(0.0f, 0.0f, zoomAmount);
        }

        // ��Y������ת����ӦͼƬ�������﷭ת��
        // �����X������ת�����ӦͼƬ�������﷭ת��
        mCamera.rotateY(rotationAngle);
        mCamera.getMatrix(imageMatrix);
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
        mCamera.restore();
    }
}
