package com.cjw.showimagev1.activity;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.ShowImageApp;
import com.cjw.showimagev1.constant.CommonConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class GuideActivity extends Activity implements OnClickListener
{
    private Button mGuideBtnVIP;
    private Button mGuideBtnCommon;

    private ImageView mGuidePic;

    private Drawable[] mGuidePics;

    private Animation[] mAnimations;

    private int mCurrentItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        initView();
        initData();
    }

    private void initView()
    {
        mGuideBtnVIP = (Button)findViewById(R.id.guide_btn_vip);
        mGuideBtnCommon = (Button)findViewById(R.id.guide_btn_common);

        mGuidePic = (ImageView)findViewById(R.id.guide_pic);

        mGuidePics = new Drawable[] { 
                getResources().getDrawable(R.drawable.guide1),
                getResources().getDrawable(R.drawable.guide2),
                getResources().getDrawable(R.drawable.guide3)
        };

        mAnimations = new Animation[] {
                AnimationUtils.loadAnimation(this, R.anim.guide_fade_in),
                AnimationUtils.loadAnimation(this, R.anim.guide_fade_in_scale),
                AnimationUtils.loadAnimation(this, R.anim.guide_fade_out)
        };
    }

    private void initData()
    {
        mGuideBtnVIP.setOnClickListener(this);
        mGuideBtnCommon.setOnClickListener(this);

        mAnimations[0].setDuration(1000);
        mAnimations[1].setDuration(1500);
        mAnimations[2].setDuration(1000);

        mAnimations[0].setAnimationListener(new GuideAnimationListener(0));
        mAnimations[1].setAnimationListener(new GuideAnimationListener(1));
        mAnimations[2].setAnimationListener(new GuideAnimationListener(2));

        mGuidePic.setImageDrawable(mGuidePics[0]);
        mGuidePic.startAnimation(mAnimations[0]);
    }

    class GuideAnimationListener implements AnimationListener
    {
        private int mIndex;

        public GuideAnimationListener(int index)
        {
            this.mIndex = index;
        }

        @Override
        public void onAnimationStart(Animation animation)
        {
        }

        // let animation looping
        @Override
        public void onAnimationEnd(Animation animation)
        {
            if (mIndex < mAnimations.length -1)
            {
                mGuidePic.startAnimation(mAnimations[mIndex + 1]);
            }
            else
            {
                mCurrentItem++;
                if (mCurrentItem > mGuidePics.length -1)
                {
                    mCurrentItem = 0;
                }
                mGuidePic.setImageDrawable(mGuidePics[mCurrentItem]);
                mGuidePic.startAnimation(mAnimations[0]);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {
        }
        
    }

    @Override
    public void onClick(View v)
    {
        SharedPreferences preferences = getSharedPreferences(CommonConstants.SHOW_IMAGE_PREFERENCE, MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(CommonConstants.IS_FIRST_IN, false);
        editor.commit();

        switch (v.getId())
        {
            case R.id.guide_btn_vip:
                VIPLogin();
                break;
            case R.id.guide_btn_common:
                ShowImageApp.IS_VIP = false;
                startActivity(new Intent(GuideActivity.this, CoverActivity.class));
                GuideActivity.this.finish();
                break;
            default:
                break;
        }
    }

    private void VIPLogin()
    {
        final EditText inputText = new EditText(GuideActivity.this);
        new AlertDialog.Builder(GuideActivity.this)
            .setTitle(R.string.dialog_title)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setView(inputText)
            .setPositiveButton(R.string.dialog_button, new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String value = inputText.getText().toString();
                    if (null != value && getString(R.string.dialog_password).equals(value))
                    {
                        ShowImageApp.IS_VIP = true;
                    }
                    else
                    {
                        ShowImageApp.IS_VIP = false;
                    }
                    dialog.dismiss();
                    startActivity(new Intent(GuideActivity.this, CoverActivity.class));
                    GuideActivity.this.finish();
                }
            }).show();

    }
}
