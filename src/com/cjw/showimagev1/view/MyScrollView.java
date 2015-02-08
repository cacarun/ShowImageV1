package com.cjw.showimagev1.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.activity.DetailActivity;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.util.FileUtils;
import com.cjw.showimagev1.util.ImageLoader;
import com.cjw.showimagev1.util.ImageLoader.OnImageLoaderListener;
import com.cjw.showimagev1.util.Images;

/**
 * �Զ����ScrollView�������ж�̬�ض�ͼƬ������� ÿ�� �ȴӻ�����ȡ��û����ȥ�洢��ȡ��������Դ��������أ���ȥ����
 * 
 */
public class MyScrollView extends ScrollView implements OnTouchListener
{

    // ��¼��ǰ�Ѽ��ص��ڼ�ҳ
    private int page;

    // ÿһ�еĿ��
    private int columnWidth;

    // ��ǰ��һ�еĸ߶�
    private int firstColumnHeight;

    // ��ǰ�ڶ��еĸ߶�
    private int secondColumnHeight;

    // ��ǰ�����еĸ߶�
    private int thirdColumnHeight;

    // �Ƿ��Ѽ��ع�һ��layout������onLayout�еĳ�ʼ��ֻ�����һ��
    private boolean loadOnce;

    // ��һ�еĲ���
    private LinearLayout firstColumn;

    // �ڶ��еĲ���
    private LinearLayout secondColumn;

    // �����еĲ���
    private LinearLayout thirdColumn;

    // MyScrollView�µ�ֱ���Ӳ���
    private View scrollLayout;

    // MyScrollView���ֵĸ߶�
    private int scrollViewHeight;

    // ��¼�ϴ�ֱ����Ĺ�������
    private int lastScrollY = -1;

    // ��¼���н����ϵ�ͼƬ�����Կ�����ʱ���ƶ�ͼƬ���ͷ�
    //private List<ImageView> imageViewList = new ArrayList<ImageView>();
    private Map<Integer, ImageView> imageViewMap = new HashMap<Integer, ImageView>();

    // ��¼���ص�ǰ���ͼƬ��·��
    private static List<String> imageUrls; // FIXME static
    
    // ��ͼƬ���й���Ĺ�����
    private ImageLoader imageLoader;

    /**
     * Ҫ�����䣺
     * if (scrollViewHeight + scrollY >= scrollLayout.getHeight())
     * ��Ļ�ĸ� + ���ϱ仯�Ļ������ֵĸ�  >= ���ϱ仯������layout�ĸ�(layout�Ǽ��س���������ͼƬ�ĸߣ������ⲿ��һ��ʼ��ǰ�沿��Ҫ��)
     * 
     * ��һ�����⣺
     * ����ͼƬ�ܶ࣬��һ��������Ѹ���»�����;ûͣ��if (scrollY == lastScrollY)����ͨ��������һֱ��ִ��myScrollView.loadMoreImages();
     * Ȼ�󣬴������ϻ���������ʱ�򣬶��ǿյ�ͼƬ��
     * 
     * 
     * ��Handler�н���ͼƬ�ɼ��Լ����жϣ��Լ����ظ���ͼƬ�Ĳ�����
     */
    private Handler handler = new Handler()
    {

        public void handleMessage(android.os.Message msg)
        {
            MyScrollView myScrollView = (MyScrollView)msg.obj;
            int scrollY = myScrollView.getScrollY();
            // �����ǰ�Ĺ���λ�ú��ϴ���ͬ����ʾ��ֹͣ����
            if (scrollY == lastScrollY)
            {
                // ��������һ����ײ�����ʼ������һҳ��ͼƬ
                // ��Ļ�ĸ� + ���ϱ仯�Ļ������ֵĸ�  >= ���ϱ仯������layout�ĸ�(layout�Ǽ��س���������ͼƬ�ĸߣ������ⲿ��һ��ʼ��ǰ�沿��Ҫ��)
                if (scrollViewHeight + scrollY >= scrollLayout.getHeight())
                {
                    //Log.e("MyScrollView", "loadMoreImages");
                    myScrollView.loadMoreImages();
                }
                // �ɼ��Լ��
                Log.e("MyScrollView", "checkVisibility");
                myScrollView.checkVisibility();
            }
            else
            {
                // �ڹ���������ȡ������
                //Log.e("MyScrollView", "cancelTask");
                imageLoader.cancelTask();

                lastScrollY = scrollY;
                Message message = new Message();
                message.obj = myScrollView;
                // 5������ٴζԹ���λ�ý����ж�
                handler.sendMessageDelayed(message, CommonConstants.SCROLL_TIME_DELAY);
            }
        };

    };

    /**
     * MyScrollView�Ĺ��캯����
     * 
     * @param context
     * @param attrs
     */
    public MyScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        imageLoader = ImageLoader.getInstance();
        setOnTouchListener(this);

        imageUrls = Images.getImageUrlsByPath(ShowImageApp.TYPE_PATH);
    }

    /**
     * ����һЩ�ؼ��Եĳ�ʼ����������ȡMyScrollView�ĸ߶ȣ��Լ��õ���һ�еĿ��ֵ���������￪ʼ���ص�һҳ��ͼƬ��
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce) // ֻ�����һ��
        {
            scrollViewHeight = getHeight();
            scrollLayout = getChildAt(0);// ֱ���Ӳ��֣�getHeight����
            firstColumn = (LinearLayout)findViewById(R.id.first_column);
            secondColumn = (LinearLayout)findViewById(R.id.second_column);
            thirdColumn = (LinearLayout)findViewById(R.id.third_column);
            columnWidth = firstColumn.getWidth();
            loadOnce = true;
            //Log.e("MyScrollView", "onLayout-->loadMoreImages");
            loadMoreImages();
        }
    }

    /**
     * �����û��Ĵ����¼�������û���ָ�뿪��Ļ��ʼ���й�����⡣
     */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            Message message = new Message();
            message.obj = this;
            handler.sendMessageDelayed(message, 5);
        }
        return false;
    }

    /**
     * ��ʼ������һҳ��ͼƬ��ÿ��ͼƬ���Ὺ��һ���첽�߳�ȥ���ء�
     */
    public void loadMoreImages()
    {
        if (FileUtils.isSDMounted())
        {
            int startIndex = page * CommonConstants.SCROLL_PAGE_SIZE;
            int endIndex = page * CommonConstants.SCROLL_PAGE_SIZE + CommonConstants.SCROLL_PAGE_SIZE;
            if (startIndex < imageUrls.size())
            {
                Toast.makeText(getContext(), "���ڼ���...", Toast.LENGTH_SHORT).show();
                if (endIndex > imageUrls.size())
                {
                    endIndex = imageUrls.size();
                }
                for (int i = startIndex; i < endIndex; i++)
                {
                    loadImageInScroll(i, null);
                }

                page++;
            }
            else
            {
                Toast.makeText(getContext(), "��û�и���ͼƬ", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getContext(), "δ����SD��", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ����imageViewList�е�ÿ��ͼƬ����ͼƬ�Ŀɼ��Խ��м�飬���ͼƬ�Ѿ��뿪��Ļ�ɼ���Χ����ͼƬ�滻��һ�ſ�ͼ��
     * ʵ���ϣ��Ѽ��ع���ͼƬ���������ڴ�����mMemoryCacheά����
     * ���ܻ���ֵ�����ǣ������ڴ����ƣ�����ͼƬ�ӻ�������ʧ��loadImageInScroll(i, imageView);������������������ģ�
     * ����������ImageView,��Լ�ڴ档
     * 
     * 
     */
    public void checkVisibility()
    {
        for (Entry<Integer, ImageView> entry : imageViewMap.entrySet())
        //for (int i = 0; i < imageViewList.size(); i++)
        {
            //ImageView imageView = imageViewList.get(i);
            int pos = entry.getKey();
            ImageView imageView = entry.getValue();

            int borderTop = (Integer)imageView.getTag(R.string.border_top);
            int borderBottom = (Integer)imageView.getTag(R.string.border_bottom);

            // һ��ͼƬ�Ƿ�ɼ�ȡ���ڣ�
            // ͼƬ�ĵײ��߶� > ���ϱ仯�Ļ������ֵĸ� (����һ����������ͼƬ��Ҫ��ʧ)  && ͼƬ��ͷ���ĸ߶� < ���ϱ仯�Ļ������ֵĸ� + ��Ļ�ĸ� (����һ����������ͼƬ��Ҫ��ʧ) 
            if (borderBottom > getScrollY() && borderTop < getScrollY() + scrollViewHeight)
            {
                String imageUrl = (String)imageView.getTag(R.string.image_url);
                
                Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
                if (bitmap != null)
                {
                    Log.e("!!!!!", "!!!!!!" + imageUrl);
                    imageView.setImageBitmap(bitmap);
                }
                else
                {
                    Log.e("====", "=====" + imageUrl);
                    // ͼƬ��ʧ(mMemoryCacheά���ڴ�) ���¼���
                    loadImageInScroll(pos, imageView);
                }
            }
            else
            {
                imageView.setImageResource(R.drawable.empty_photo);
            }
        }
    }

    /**
     * ��ÿһ��ͼƬ�ļ���
     * 
     * ��֮ǰ�汾��ͨ�� ÿһ��ͼƬnew һ�� AsyncTask�����أ�
     * 
     * @param localImageUrl
     * @param imageView
     *        ͼƬ�п����ϴα����ع���(�ᱻ�ŵ�imageViewList�� )����� checkVisibilityʱ���ֲ��ڻ������ˣ�
     *        ��ʱ�Ϳ�����imageViewList�����Լ���ImageView������������ͼƬ��������Ҫ��new ImageView��
     *        �Դﵽ���õ�Ŀ�ġ�
     */
    private void loadImageInScroll(final int pos, final ImageView imageView)
    {
        // �趨��ǰ����ֻ֧�ִӱ���Ĭ��·������
        imageLoader.loadImage(false, columnWidth, imageUrls.get(pos), new OnImageLoaderListener()
        {
            @Override
            public void onImageLoader(Bitmap bitmap, String url)
            {
                if (bitmap != null)
                {
                    // FIXME ÿ�ε����ҳ�涼Ҫscale �˷�
                    // FIXME �˴�����Ҫ���� columnWidth �����¸ߣ�Ϊʲô֮ǰdecorate�Ĳ�׼ȷ��
                    double ratio = bitmap.getWidth() / (columnWidth * 1.0);
                    int scaledHeight = (int)(bitmap.getHeight() / ratio);
                    addImage(imageView, bitmap, url, columnWidth, scaledHeight, pos); // ����õ�Ӧ�÷ŵ�һ��Layout�Ŀ�ȸ߶�
                                                                                        // ����
                }
                else
                {
                    Log.e("MyScrollView", "loadImageInScroll --> FIXME");
                    // FIXME
                    // imageView.setImageResource(R.drawable.empty_photo);
                }
            }

        });
    }
    
    /**
     * ��ImageView�����һ��ͼƬ
     * 
     * @param bitmap ����ӵ�ͼƬ
     * @param imageWidth ͼƬ�Ŀ��
     * @param imageHeight ͼƬ�ĸ߶�
     * @param pos -------->>>>  imageUrls �� index
     */
    private void addImage(ImageView repeatUsedIV, Bitmap bitmap, String url, int imageWidth, int imageHeight, final int pos)
    {
        if (repeatUsedIV != null)
        {
            Log.e("repeatUsedIV", "repeatUsedIV != nullrepeatUsedIV != null");
            // �� checkVisibility�������������ʱ�������������Ѵ��ڵ�ImageView����Լ�ڴ�
            repeatUsedIV.setImageBitmap(bitmap);
        }
        else
        {
            // ÿһ���¼ӵ�ͼƬ����Ҫnew ImageView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(params);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setPadding(5, 5, 5, 5);
            imageView.setTag(R.string.image_url, url);

            findColumnToAdd(imageView, imageHeight).addView(imageView);

            // ��һ�ֵ��õ�������ǣ���һ�ο�ʼ����ͼƬ
            // imageViewList����˳�����ȼ����������ǰ�棬��imageUrls.get(pos)��ͼƬ˳��һ��
            // ������pos = 3�ķ���imageViewList��index = 0��λ��......
            // �����������ǣ�onClick Ҫ��pos��DetailActivity, ��DetailActivity�Ǹ���imageUrlsȡͼƬ��
            // ���Ի��ǿ��ǰ�imageViewList����ͼƬ��˳���imageUrls����һ�£�����
            
            //imageViewList.add(imageView); // ��һ����Ҫ ÿ���ӽ�ȥ��image�������˶������Ϣ
            imageViewMap.put(pos, imageView);

            imageView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    Bundle bundle = new Bundle();
                    // ���ͼƬ�� imageViewList �ĵ�ǰλ��
                    bundle.putInt(CommonConstants.IMAGE_POSITION_ID, pos);

                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
                }
            });

        }
    }

    /**
     * �ҵ���ʱӦ�����ͼƬ��һ�С�ԭ����Ƕ����еĸ߶Ƚ����жϣ���ǰ�߶���С��һ�о���Ӧ����ӵ�һ�С�
     * 
     * @param imageView
     * @param imageHeight
     * @return Ӧ�����ͼƬ��һ��
     */
    private LinearLayout findColumnToAdd(ImageView imageView, int imageHeight)
    {
        if (firstColumnHeight <= secondColumnHeight)
        {
            if (firstColumnHeight <= thirdColumnHeight)
            {
                imageView.setTag(R.string.border_top, firstColumnHeight);
                firstColumnHeight += imageHeight;
                imageView.setTag(R.string.border_bottom, firstColumnHeight);
                return firstColumn;
            }
            imageView.setTag(R.string.border_top, thirdColumnHeight);
            thirdColumnHeight += imageHeight;
            imageView.setTag(R.string.border_bottom, thirdColumnHeight);
            return thirdColumn;
        }
        else
        {
            if (secondColumnHeight <= thirdColumnHeight)
            {
                imageView.setTag(R.string.border_top, secondColumnHeight);
                secondColumnHeight += imageHeight;
                imageView.setTag(R.string.border_bottom, secondColumnHeight);
                return secondColumn;
            }
            imageView.setTag(R.string.border_top, thirdColumnHeight);
            thirdColumnHeight += imageHeight;
            imageView.setTag(R.string.border_bottom, thirdColumnHeight);
            return thirdColumn;
        }
    }

}