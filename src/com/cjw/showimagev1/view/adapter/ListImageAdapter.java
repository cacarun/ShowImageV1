package com.cjw.showimagev1.view.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.activity.GalleryCoverFlow;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.model.RecordInfo;

public class ListImageAdapter extends ImageBaseAdapter
{
    protected List<RecordInfo> mRecordInfos;
    
    public ListImageAdapter(Context context, List<RecordInfo> recordInfos)
    {
        super(context);
        this.mRecordInfos = recordInfos;
    }

    // 保持数据源一致
    public void swapItems(List<RecordInfo> items)
    {
        this.mRecordInfos = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return mRecordInfos.size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Holder h = null;
        if (convertView == null)
        {
            h = new Holder();
            convertView = mInflater.inflate(R.layout.list_item, null);

            h.tv_date = (TextView)convertView.findViewById(R.id.tv_date);
            h.tv_num = (TextView)convertView.findViewById(R.id.tv_num);
            h.iv_icon = (ImageView)convertView.findViewById(R.id.iv_icon);

            h.iv_content = (ImageView)convertView.findViewById(R.id.iv_content);
            h.iv_content.setTag(position);
            h.iv_content.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View view)
                {
                     ImageView ivContent = (ImageView)view;
                     int pos = (Integer)ivContent.getTag();
                     Intent intent = new Intent(mContext, GalleryCoverFlow.class);
                     intent.putExtra(CommonConstants.RECORD_LIST_POS, pos);
                     mContext.startActivity(intent);
                }
            });

            convertView.setTag(h);
            Log.e("getView", "1111111111111111getViewgetViewgetViewgetViewgetView");
        }
        else
        {
            h = (Holder)convertView.getTag();
            Log.e("getView", "22222222222222222getViewgetViewgetViewgetViewgetView");
        }

        RecordInfo recordInfo = mRecordInfos.get(position);

        h.tv_date.setText(recordInfo.getDate());
        h.tv_num.setText(String.format(mContext.getResources().getString(R.string.record_image_num), recordInfo.getNum()));
        h.iv_icon.setBackgroundResource(R.drawable.img_icon);

        h.iv_content.setImageBitmap(recordInfo.getBitmap());

        return convertView;
    }

    private class Holder
    {
        private TextView tv_date;
        private TextView tv_num;
        private ImageView iv_icon;
        private ImageView iv_content;
    }

}
