package com.cjw.showimagev1.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.util.CommonUtils;

public class CoverActivity extends Activity implements OnClickListener
{
    private long mWaitTime = 2000;
    private long mTouchTime = 0;

    private LinearLayout mCoverLayout;
    private LinearLayout mCommonPart;
    private LinearLayout mVIPPart;
    private ImageView mPart1;
    private ImageView mPart2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);

        mCoverLayout = (LinearLayout)findViewById(R.id.cover_layout);
        mCommonPart = (LinearLayout)findViewById(R.id.common_part);
        mVIPPart = (LinearLayout)findViewById(R.id.vip_part);

        if (ShowImageApp.IS_VIP)
        {
            mCoverLayout.setBackgroundResource(R.drawable.vip_bg);
            mCommonPart.setVisibility(View.GONE);
            mVIPPart.setVisibility(View.VISIBLE);
            mPart1 = (ImageView)findViewById(R.id.vip_part1);
            mPart2 = (ImageView)findViewById(R.id.vip_part2);
        }
        else
        {
            mCoverLayout.setBackgroundResource(R.drawable.common_bg);
            mCommonPart.setVisibility(View.VISIBLE);
            mVIPPart.setVisibility(View.GONE);
            mPart1 = (ImageView)findViewById(R.id.common_part1);
            mPart2 = (ImageView)findViewById(R.id.common_part2);
        }
        
        mPart1.setOnClickListener(this);
        mPart2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (!CommonUtils.isFastDoubleClick())
        {
            final int viewId = v.getId();
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    switch (viewId)
                    {
                        case R.id.common_part1:
                        case R.id.vip_part1:
                            startActivity(new Intent(CoverActivity.this, ShowRecordActivity.class));
                            ShowImageApp.IS_TYPE_SHOW = false;
                            break;
                        case R.id.common_part2:
                        case R.id.vip_part2:
                            startActivity(new Intent(CoverActivity.this, ShowTypeActivity.class));
                            ShowImageApp.IS_TYPE_SHOW = true;
                            break;
                        default:
                            break;
                    }
                }
            }, CommonConstants.ANIM_TIME_DELAY);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode)
        {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - mTouchTime) >= mWaitTime)
            {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mTouchTime = currentTime;
            }
            else
            {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
