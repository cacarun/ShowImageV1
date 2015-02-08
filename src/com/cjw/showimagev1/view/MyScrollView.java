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
 * 自定义的ScrollView，在其中动态地对图片进行添加 每次 先从缓存中取，没有则去存储卡取，如果可以从网上下载，在去下载
 * 
 */
public class MyScrollView extends ScrollView implements OnTouchListener
{

    // 记录当前已加载到第几页
    private int page;

    // 每一列的宽度
    private int columnWidth;

    // 当前第一列的高度
    private int firstColumnHeight;

    // 当前第二列的高度
    private int secondColumnHeight;

    // 当前第三列的高度
    private int thirdColumnHeight;

    // 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
    private boolean loadOnce;

    // 第一列的布局
    private LinearLayout firstColumn;

    // 第二列的布局
    private LinearLayout secondColumn;

    // 第三列的布局
    private LinearLayout thirdColumn;

    // MyScrollView下的直接子布局
    private View scrollLayout;

    // MyScrollView布局的高度
    private int scrollViewHeight;

    // 记录上垂直方向的滚动距离
    private int lastScrollY = -1;

    // 记录所有界面上的图片，用以可以随时控制对图片的释放
    //private List<ImageView> imageViewList = new ArrayList<ImageView>();
    private Map<Integer, ImageView> imageViewMap = new HashMap<Integer, ImageView>();

    // 记录本地当前类别图片的路径
    private static List<String> imageUrls; // FIXME static
    
    // 对图片进行管理的工具类
    private ImageLoader imageLoader;

    /**
     * 要理解这句：
     * if (scrollViewHeight + scrollY >= scrollLayout.getHeight())
     * 屏幕的高 + 不断变化的滑动部分的高  >= 不断变化的整个layout的高(layout是加载出来的所有图片的高，所以这部分一开始比前面部分要大)
     * 
     * 有一个问题：
     * 假设图片很多，第一屏出来后，迅速下滑，中途没停，if (scrollY == lastScrollY)条件通不过，就一直不执行myScrollView.loadMoreImages();
     * 然后，从下往上滑，返回来时候，都是空的图片？
     * 
     * 
     * 在Handler中进行图片可见性检查的判断，以及加载更多图片的操作。
     */
    private Handler handler = new Handler()
    {

        public void handleMessage(android.os.Message msg)
        {
            MyScrollView myScrollView = (MyScrollView)msg.obj;
            int scrollY = myScrollView.getScrollY();
            // 如果当前的滚动位置和上次相同，表示已停止滚动
            if (scrollY == lastScrollY)
            {
                // 当滚动的一屏最底部，开始加载下一页的图片
                // 屏幕的高 + 不断变化的滑动部分的高  >= 不断变化的整个layout的高(layout是加载出来的所有图片的高，所以这部分一开始比前面部分要大)
                if (scrollViewHeight + scrollY >= scrollLayout.getHeight())
                {
                    //Log.e("MyScrollView", "loadMoreImages");
                    myScrollView.loadMoreImages();
                }
                // 可见性检查
                Log.e("MyScrollView", "checkVisibility");
                myScrollView.checkVisibility();
            }
            else
            {
                // 在滚动过程中取消加载
                //Log.e("MyScrollView", "cancelTask");
                imageLoader.cancelTask();

                lastScrollY = scrollY;
                Message message = new Message();
                message.obj = myScrollView;
                // 5毫秒后再次对滚动位置进行判断
                handler.sendMessageDelayed(message, CommonConstants.SCROLL_TIME_DELAY);
            }
        };

    };

    /**
     * MyScrollView的构造函数。
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
     * 进行一些关键性的初始化操作，获取MyScrollView的高度，以及得到第一列的宽度值。并在这里开始加载第一页的图片。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce) // 只需加载一次
        {
            scrollViewHeight = getHeight();
            scrollLayout = getChildAt(0);// 直接子布局，getHeight方法
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
     * 监听用户的触屏事件，如果用户手指离开屏幕则开始进行滚动检测。
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
     * 开始加载下一页的图片，每张图片都会开启一个异步线程去加载。
     */
    public void loadMoreImages()
    {
        if (FileUtils.isSDMounted())
        {
            int startIndex = page * CommonConstants.SCROLL_PAGE_SIZE;
            int endIndex = page * CommonConstants.SCROLL_PAGE_SIZE + CommonConstants.SCROLL_PAGE_SIZE;
            if (startIndex < imageUrls.size())
            {
                Toast.makeText(getContext(), "正在加载...", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "已没有更多图片", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getContext(), "未发现SD卡", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 遍历imageViewList中的每张图片，对图片的可见性进行检查，如果图片已经离开屏幕可见范围，则将图片替换成一张空图。
     * 实际上，已加载过的图片都保留在内存中由mMemoryCache维护，
     * 可能会出现的情况是，由于内存限制，部分图片从缓存中消失，loadImageInScroll(i, imageView);就是来处理这种情况的，
     * 且它还重用ImageView,节约内存。
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

            // 一张图片是否可见取决于：
            // 图片的底部高度 > 不断变化的滑动部分的高 (参照一屏中最上面图片将要消失)  && 图片的头部的高度 < 不断变化的滑动部分的高 + 屏幕的高 (参照一屏中最下面图片将要消失) 
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
                    // 图片丢失(mMemoryCache维护内存) 重新加载
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
     * 对每一张图片的加载
     * 
     * （之前版本是通过 每一张图片new 一个 AsyncTask来加载）
     * 
     * @param localImageUrl
     * @param imageView
     *        图片有可能上次被加载过了(会被放到imageViewList中 )，这次 checkVisibility时发现不在缓存中了，
     *        这时就可以用imageViewList中它自己的ImageView再来加载这张图片，而不需要在new ImageView，
     *        以达到重用的目的。
     */
    private void loadImageInScroll(final int pos, final ImageView imageView)
    {
        // 设定当前加载只支持从本地默认路径加载
        imageLoader.loadImage(false, columnWidth, imageUrls.get(pos), new OnImageLoaderListener()
        {
            @Override
            public void onImageLoader(Bitmap bitmap, String url)
            {
                if (bitmap != null)
                {
                    // FIXME 每次到这个页面都要scale 浪费
                    // FIXME 此处还需要根据 columnWidth 计算下高，为什么之前decorate的不准确？
                    double ratio = bitmap.getWidth() / (columnWidth * 1.0);
                    int scaledHeight = (int)(bitmap.getHeight() / ratio);
                    addImage(imageView, bitmap, url, columnWidth, scaledHeight, pos); // 计算得到应该放到一列Layout的宽度高度
                                                                                        // 放入
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
     * 向ImageView中添加一张图片
     * 
     * @param bitmap 待添加的图片
     * @param imageWidth 图片的宽度
     * @param imageHeight 图片的高度
     * @param pos -------->>>>  imageUrls 的 index
     */
    private void addImage(ImageView repeatUsedIV, Bitmap bitmap, String url, int imageWidth, int imageHeight, final int pos)
    {
        if (repeatUsedIV != null)
        {
            Log.e("repeatUsedIV", "repeatUsedIV != nullrepeatUsedIV != null");
            // 当 checkVisibility调用这个方法的时候最好重用这个已存在的ImageView，节约内存
            repeatUsedIV.setImageBitmap(bitmap);
        }
        else
        {
            // 每一张新加的图片都需要new ImageView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(params);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setPadding(5, 5, 5, 5);
            imageView.setTag(R.string.image_url, url);

            findColumnToAdd(imageView, imageHeight).addView(imageView);

            // 第一种调用到的情况是：第一次开始加载图片
            // imageViewList里面顺序是先加载完的排在前面，和imageUrls.get(pos)的图片顺序不一致
            // 即可能pos = 3的放在imageViewList的index = 0的位置......
            // 后来经过考虑：onClick 要传pos到DetailActivity, 而DetailActivity是根据imageUrls取图片的
            // 所以还是考虑把imageViewList里面图片的顺序跟imageUrls保持一致！！！
            
            //imageViewList.add(imageView); // 这一步重要 每个加进去的image都附加了额外的信息
            imageViewMap.put(pos, imageView);

            imageView.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    Bundle bundle = new Bundle();
                    // 存放图片在 imageViewList 的当前位置
                    bundle.putInt(CommonConstants.IMAGE_POSITION_ID, pos);

                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
                }
            });

        }
    }

    /**
     * 找到此时应该添加图片的一列。原则就是对三列的高度进行判断，当前高度最小的一列就是应该添加的一列。
     * 
     * @param imageView
     * @param imageHeight
     * @return 应该添加图片的一列
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