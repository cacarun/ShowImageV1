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
    private int vWidth; // ͼƬ���
    private int vHeight; // ͼƬ�߶�

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
        // ����һ����Paint���Ͽ���ݱ�־,Ȼ��Paint������Ϊ��������canvas�Ļ��Ʒ���
        // paint.setAntiAlias(true);
        // ������: ֱ�Ӹ�canvas�ӿ���ݣ������㡣
        canvas.setDrawFilter(drawFilter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

        // ��㴥��ͳһ
        // and֮����������ٸ���ָ�ӽ��������ǻ�ACTION_POINTER_DOWN����ACTION_POINTER_UP
        // ���ϲ��Ƕ�㴥�ص�3�� ACTION_DOWN, ACTION_UP, ACTION_MOVE
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                float X = event.getX(); // ��ȡ��һ�����ص��Xλ��(������������Ͻǵ�x����)
                float Y = event.getY(); // ��ȡ��һ�����ص��Yλ��
                // ���Ʋ�ͬ������ת��
                // X >  Y ��X����ת?
                // X <= Y ��Y����ת?
                RolateX = vWidth / 2 - X;
                RolateY = vHeight / 2 - Y;
                XbigY = Math.abs(RolateX) > Math.abs(RolateY) ? true : false;

                // ������ص�������������� ��scale
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
        camera.save();// save()��restore()���Խ�ͼ����ɵ����һЩ, Each save should be balanced with a call to restore().
        camera.rotateX(RolateY > 0 ? rotateY : -rotateY);
        camera.rotateY(RolateX < 0 ? rotateX : -rotateX);
        camera.getMatrix(matrix);// �����ǸղŶ����һϵ�б任Ӧ�õ��任��������  
        camera.restore();// �ָ���֮ǰ�ĳ�ʼ״̬��

        // ͼ�����У�Խ�����ұߵľ���Խ��ִ�У�����pre��Ҳ�����ȵ���˼�������õľ���T��Scale��RotationҲ��һ���ģ�
        // �ͻ�������һ��ʼ���õ�Scaleִ�У���post�������˼������Ϊ����ˣ����������������ߣ���ô�ͻ����ִ�С�

        if (RolateX > 0 && rotateX != 0)
        {
            // ��ô�����������������ͼƬ�������ţ�Ӧ�ø���ô�죿Ҫ�õ���ϱ任��
            // �Ƚ�ͼƬ������ƽ�Ƶ�ԭ�㣬����Ӧ�ñ任 T
            // ��ͼӦ�����ű任 S 
            // �ٽ�ͼƬƽ�ƻص����ģ�Ӧ�ñ任 -T

            matrix.preTranslate(-vWidth, -scaleY);// ��ִ��һ��ƽ�Ƶ�ԭ��Ĳ�����preTranslate������ʵҲ���ǽ����ҳˣ�S * T��
            // ��������ִ�еı任 camera.getMatrix(matrix);
            matrix.postTranslate(vWidth, scaleY);// ��ִ��һ����ԭ��ƽ�Ƶ����ĵ�Ĳ�����postTranslate����Ҳ���ǽ���һ����ˣ�-T * S * T��
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
        setImageMatrix(matrix); // ��������
    }

    private Handler scale_handler = new Handler()
    {
        private Matrix matrix = new Matrix();
        private float s;
        int count = 0;

        // Scale_Handler_Message_Turned ��ÿ���׶εĽ����㣡
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
                        
                        s = (float)Math.sqrt(Math.sqrt(CommonConstants.MIN_SCALE)); // ÿ�����ű���
                        beginScale(matrix, s);
                        scale_handler.sendEmptyMessage(Scale_Handler_Message_Turning);
                    }
                    break;
                case Scale_Handler_Message_Turning:

                    beginScale(matrix, s); // �ٽ���5������

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
                    isAnimationFinish = true; // �����㣡
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
        int scaleX = (int)(vWidth * 0.5f); // �������ĵ� X
        int scaleY = (int)(vHeight * 0.5f); // �������ĵ� Y
        // ������scaleX��scaleYΪ���Ľ������ţ�sx/sy����XY��������ű���
        matrix.postScale(scale, scale, scaleX, scaleY);
        setImageMatrix(matrix);
    }

}
