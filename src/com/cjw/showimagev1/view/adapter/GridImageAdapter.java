package com.cjw.showimagev1.view.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.activity.GalleryCoverFlow;
import com.cjw.showimagev1.activity.ShowTypeActivity;
import com.cjw.showimagev1.activity.ShowWaterfallActivity;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.model.LocalTypeInfo;
import com.cjw.showimagev1.util.Images;

public class GridImageAdapter extends ImageBaseAdapter
{
    private List<LocalTypeInfo> mInfos;

    public GridImageAdapter(Context context, List<LocalTypeInfo> infos)
    {
        super(context);
        this.mInfos = infos;
    }

    @Override
    public int getCount()
    {
        return mInfos.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Holder holder = null;
        if (convertView == null)
        {
            // 给ImageView设置资源
            //imageView = new ImageView(mContext);
            // 设置布局 图片120×120显示
            //imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
            // 设置显示比例类型
            //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            holder = new Holder();
            convertView = mInflater.inflate(R.layout.item_grid, null);

            holder.tv_info = (TextView)convertView.findViewById(R.id.tv_info);
            holder.iv_image = (ImageView)convertView.findViewById(R.id.iv_image);
            holder.iv_image.setTag(position);
            holder.iv_image.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View view)
                {
                    // FIXME 连续点击多次会怎么样？
                    
                    ImageView iv = (ImageView)view;
                    int pos = (Integer)iv.getTag();
                    ShowImageApp.TYPE_PATH = Images.COVER_INFOS.get(pos).getPath();
                    Intent intent = new Intent(mContext, ShowWaterfallActivity.class);
                    mContext.startActivity(intent);
                }
            });

            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder)convertView.getTag();
        }

        LocalTypeInfo info = mInfos.get(position);
        int num = info.getNum();
        String tmp = info.getPath().substring(0, info.getPath().length() - 1);
        String dirName = tmp.substring(tmp.lastIndexOf("/") + 1);
        
        holder.tv_info.setText(dirName + "  " + num);
        holder.iv_image.setImageBitmap(info.getBitmap());

        return convertView;
    }

    private class Holder
    {
        private TextView tv_info;
        private ImageView iv_image;
    }
}
