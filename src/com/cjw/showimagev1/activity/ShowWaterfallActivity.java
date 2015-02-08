package com.cjw.showimagev1.activity;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.view.MyScrollView;

import android.app.Activity;
import android.os.Bundle;

public class ShowWaterfallActivity extends Activity
{
    private MyScrollView mMyScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_waterfall);

        mMyScrollView = (MyScrollView)findViewById(R.id.my_scroll_view);
        if (ShowImageApp.IS_VIP)
        {
            mMyScrollView.setBackgroundResource(R.drawable.vip_bg);
        }
        else
        {
            mMyScrollView.setBackgroundResource(R.drawable.common_bg);
        }
    }

}