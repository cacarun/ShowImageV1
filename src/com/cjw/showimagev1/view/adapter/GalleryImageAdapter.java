package com.cjw.showimagev1.view.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cjw.showimagev1.R;

public class GalleryImageAdapter extends ImageBaseAdapter
{
    public GalleryImageAdapter(Context context, List<Bitmap> bitmaps)
    {
        super(context, bitmaps);
    }

    @Override
    public int getCount()
    {
        return mBitmapList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView = null;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_image, null);
            imageView = (ImageView)convertView.findViewById(R.id.item_image);
            convertView.setTag(imageView);
        }
        else
        {
            imageView = (ImageView)convertView.getTag();
        }

        imageView.setImageBitmap(mBitmapList.get(position));

        return imageView;
    }

}
