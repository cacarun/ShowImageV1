package com.cjw.showimagev1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.cjw.showimagev1.activity.CoverActivity;
import com.cjw.showimagev1.activity.GuideActivity;
import com.cjw.showimagev1.constant.CommonConstants;

public class WelcomeActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish()
            {
                SharedPreferences preferences = getSharedPreferences(CommonConstants.SHOW_IMAGE_PREFERENCE, MODE_PRIVATE);
                boolean isFirstIn = preferences.getBoolean(CommonConstants.IS_FIRST_IN, true);
                if (isFirstIn)
                {
                    startActivity(new Intent(WelcomeActivity.this, GuideActivity.class));
                }
                else
                {
                    startActivity(new Intent(WelcomeActivity.this, CoverActivity.class));
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
                }
                WelcomeActivity.this.finish();
            }
        }.start();

    }
}
