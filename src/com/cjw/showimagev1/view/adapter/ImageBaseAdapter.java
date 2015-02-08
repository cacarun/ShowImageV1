package com.cjw.showimagev1.view.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ImageBaseAdapter extends BaseAdapter
{
    protected Context mContext;
    protected LayoutInflater mInflater;

    protected Bitmap[] mBitmapArray;
    protected List<Bitmap> mBitmapList;

    public ImageBaseAdapter(Context context)
    {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public ImageBaseAdapter(Context context, List<Bitmap> bitmaps)
    {
        this(context);
        this.mBitmapList = bitmaps;
    }

    public ImageBaseAdapter(Context context, Bitmap[] bitmaps)
    {
        this(context);
        this.mBitmapArray = bitmaps;
    }

    @Override
    public int getCount()
    {
     // 这个方法必须写到子类 FIXME
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return null;
    }

}
