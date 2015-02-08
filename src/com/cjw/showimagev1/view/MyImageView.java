package com.cjw.showimagev1.view;

import com.cjw.showimagev1.constant.CommonConstants;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MyImageView extends ImageView
{
    public static final int Rotate_Handler_Message_Start = 1;
    public static final int Rotate_Handler_Message_Turning = 2;
    public static final int Rotate_Handler_Message_Turned = 3;
    public static final int Rotate_Handler_Message_Reverse = 6;

    public static final int Scale_Handler_Message_Start = 1;
    public static final int Scale_Handler_Message_Turning = 2;
    public static final int Scale_Handler_Message_Turned = 3;
    public static final int Scale_Handler_Message_Reverse = 6;

    private boolean isFirst = true;
    private int vWidth; // 图片宽度
    private int vHeight; // 图片高度

    private boolean isSizeChanged = false;
    private boolean isAnimationFinish = true;
    private boolean isActionMove = false; // for future control
    private boolean isScale = false;

    private Camera camera;
    private boolean XbigY = false;
    private float RolateX = 0;
    private float RolateY = 0;

    private PaintFlagsDrawFilter drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public MyImageView(Context context)
    {
        super(context);
        camera = new Camera();
    }

    public MyImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        camera = new Camera();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (isFirst)
        {
            isFirst = false;
            vWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            vHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        }
        // 方法一：给Paint加上抗锯齿标志,然后将Paint对象作为参数传给canvas的绘制方法
        // paint.setAntiAlias(true);
        // 方法二: 直接给canvas加抗锯齿，更方便。
        canvas.setDrawFilter(drawFilter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

        // 多点触控统一
        // and之后，无论你多少根手指加进来，都是会ACTION_POINTER_DOWN或者ACTION_POINTER_UP
        // 加上不是多点触控的3个 ACTION_DOWN, ACTION_UP, ACTION_MOVE
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                float X = event.getX(); // 获取第一个触控点的X位置(相对于自身左上角的x坐标)
                float Y = event.getY(); // 获取第一个触控点的Y位置
                // 控制不同方向旋转：
                // X >  Y 绕X轴旋转?
                // X <= Y 绕Y轴旋转?
                RolateX = vWidth / 2 - X;
                RolateY = vHeight / 2 - Y;
                XbigY = Math.abs(RolateX) > Math.abs(RolateY) ? true : false;

                // 如果触控点落在这个区间内 就scale
                isScale = X > vWidth / 3 && X < vWidth * 2 / 3 && Y > vHeight / 3 && Y < vHeight * 2 / 3;
                isActionMove = false;

                if (isScale)
                {
                    if (isAnimationFinish && !isSizeChanged)
                    {
                        isSizeChanged = true;
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Start);
                    }
                }
                else
                {
                    rotate_Handler.sendEmptyMessage(Rotate_Handler_Message_Start);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (x > vWidth || y > vHeight || x < 0 || y < 0)
                {
                    isActionMove = true;
                }
                else
                {
                    isActionMove = false;
                }

                break;
            case MotionEvent.ACTION_UP:
                if (isScale)
                {
                    if (isSizeChanged) scale_handler.sendEmptyMessage(Scale_Handler_Message_Reverse);
                }
                else
                {
                    rotate_Handler.sendEmptyMessage(Rotate_Handler_Message_Reverse);
                }
                break;
        }
        return true;
    }

    private Handler rotate_Handler = new Handler()
    {
        private Matrix matrix = new Matrix();
        private float count = 0;

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            matrix.set(getImageMatrix());
            switch (msg.what)
            {
                case Rotate_Handler_Message_Start:
                    count = 0;
                    beginRotate(matrix, (XbigY ? count : 0), (XbigY ? 0 : count));
                    rotate_Handler.sendEmptyMessage(Rotate_Handler_Message_Turning);
                    break;
                case Rotate_Handler_Message_Turning:
                    beginRotate(matrix, (XbigY ? count : 0), (XbigY ? 0 : count));
                    count++;
                    if (count < CommonConstants.ROTATE_DEGREE)
                    {
                        rotate_Handler.sendEmptyMessage(Rotate_Handler_Message_Turning);
                    }
                    else
                    {
                        isAnimationFinish = true;
                    }
                    break;
                case Rotate_Handler_Message_Turned:
                    beginRotate(matrix, (XbigY ? count : 0), (XbigY ? 0 : count));
                    if (count > 0)
                    {
                        rotate_Handler.sendEmptyMessage(Rotate_Handler_Message_Turned);
                    }
                    else
                    {
                        isAnimationFinish = true;
                    }
                    count--;
                    break;
                case Rotate_Handler_Message_Reverse:
                    count = CommonConstants.ROTATE_DEGREE;
                    beginRotate(matrix, (XbigY ? count : 0), (XbigY ? 0 : count));
                    rotate_Handler.sendEmptyMessage(Rotate_Handler_Message_Turned);
                    break;
            }
        }
    };

    private synchronized void beginRotate(Matrix matrix, float rotateX, float rotateY)
    {
        int scaleX = (int)(vWidth * 0.5f);
        int scaleY = (int)(vHeight * 0.5f);
        camera.save();// save()和restore()可以将图像过渡得柔和一些, Each save should be balanced with a call to restore().
        camera.rotateX(RolateY > 0 ? rotateY : -rotateY);
        camera.rotateY(RolateX < 0 ? rotateX : -rotateX);
        camera.getMatrix(matrix);// 将我们刚才定义的一系列变换应用到变换矩阵上面  
        camera.restore();// 恢复到之前的初始状态。

        // 图像处理中，越靠近右边的矩阵越先执行，所以pre（也就是先的意思）所设置的矩阵T（Scale，Rotation也是一样的）
        // 就会先于其一开始设置的Scale执行，而post（后的意思）的因为是左乘，所以它会放在最左边，那么就会最后执行。

        if (RolateX > 0 && rotateX != 0)
        {
            // 那么我们如果想让它基于图片中心缩放，应该该怎么办？要用到组合变换，
            // 先将图片由中心平移到原点，这是应用变换 T
            // 对图应用缩放变换 S 
            // 再将图片平移回到中心，应用变换 -T

            matrix.preTranslate(-vWidth, -scaleY);// 先执行一个平移到原点的操作（preTranslate），其实也就是进行右乘（S * T）
            // 就是上面执行的变换 camera.getMatrix(matrix);
            matrix.postTranslate(vWidth, scaleY);// 再执行一个从原点平移到中心点的操作（postTranslate），也就是进行一个左乘（-T * S * T）
        }
        else if (RolateY > 0 && rotateY != 0)
        {
            matrix.preTranslate(-scaleX, -vHeight);
            matrix.postTranslate(scaleX, vHeight);
        }
        else if (RolateX < 0 && rotateX != 0)
        {
            matrix.preTranslate(-0, -scaleY);
            matrix.postTranslate(0, scaleY);
        }
        else if (RolateY < 0 && rotateY != 0)
        {
            matrix.preTranslate(-scaleX, -0);
            matrix.postTranslate(scaleX, 0);
        }
        setImageMatrix(matrix); // 矩阵缩放
    }

    private Handler scale_handler = new Handler()
    {
        private Matrix matrix = new Matrix();
        private float s;
        int count = 0;

        // Scale_Handler_Message_Turned 是每个阶段的结束点！
        // MotionEvent.ACTION_DOWN:
        // Scale_Handler_Message_Start   --> Scale_Handler_Message_Turning --> Scale_Handler_Message_Turned;
        // MotionEvent.ACTION_UP:
        // Scale_Handler_Message_Reverse --> Scale_Handler_Message_Turning --> Scale_Handler_Message_Turned.
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            matrix.set(getImageMatrix());
            switch (msg.what)
            {
                case Scale_Handler_Message_Start:
                    if (!isAnimationFinish)
                    {
                        return;
                    }
                    else
                    {
                        isAnimationFinish = false;
                        isSizeChanged = true;
                        
                        count = 0;
                        
                        s = (float)Math.sqrt(Math.sqrt(CommonConstants.MIN_SCALE)); // 每次缩放比例
                        beginScale(matrix, s);
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Turning);
                    }
                    break;
                case Scale_Handler_Message_Turning:

                    beginScale(matrix, s); // 再进行5次缩放

                    if (count < CommonConstants.SCALE_COUNT)
                    {
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Turning);
                    }
                    else
                    {
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Turned);
                    }
                    count++;
                    break;
                case Scale_Handler_Message_Turned:
                    isAnimationFinish = true; // 结束点！
                    break;
                case Scale_Handler_Message_Reverse:
                    if (!isAnimationFinish)
                    {
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Reverse);
                    }
                    else
                    {
                        isAnimationFinish = false;
                        count = 0;
                        
                        s = (float)Math.sqrt(Math.sqrt(1.0f / CommonConstants.MIN_SCALE));
                        beginScale(matrix, s);
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Turning);
                        isSizeChanged = false;
                    }
                    break;
            }
        }
    };

    private synchronized void beginScale(Matrix matrix, float scale)
    {
        int scaleX = (int)(vWidth * 0.5f); // 缩放中心点 X
        int scaleY = (int)(vHeight * 0.5f); // 缩放中心点 Y
        // 设置以scaleX、scaleY为轴心进行缩放，sx/sy控制XY方向的缩放比例
        matrix.postScale(scale, scale, scaleX, scaleY);
        setImageMatrix(matrix);
    }

}
