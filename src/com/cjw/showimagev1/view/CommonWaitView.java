package com.cjw.showimagev1.view;

import com.cjw.showimagev1.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommonWaitView extends RelativeLayout
{
	private ImageView mWaitIconImg;
	private AnimationDrawable mWaitAnim;
	private TextView mWaitMsgTxt;

	/**
	 * Default constructor for waitview, will be used by layout inflator
	 * 
	 * @param context
	 * @param attrs
	 */
	public CommonWaitView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setupView(context);
	}

	public CommonWaitView(Context context)
	{
		super(context);
		setupView(context);
	}

	/*
	 * setups wait view
	 */
	private void setupView(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.common_wait_view, this);

		mWaitIconImg = (ImageView)findViewById(R.id.wait_icon_img);
		mWaitIconImg.setBackgroundResource(R.anim.rotation);

		mWaitMsgTxt = (TextView)findViewById(R.id.wait_msg_txt);
		
		post(new Runnable()
		{
			@Override
			public void run()
			{
				mWaitAnim = (AnimationDrawable)mWaitIconImg.getBackground();
				mWaitAnim.setOneShot(false);
				mWaitAnim.start();
			}
		});

	}

	/**
	 * Update wait screen display text content.
	 * 
	 * @param workContent message.
	 */
	public void updateView(String message)
	{
		mWaitMsgTxt.setText(message);

		invalidate();
	}
	
	/**
     * Update wait screen display text content.
     * 
     * @param workContent message.
     */
    public String getWaitMessage()
    {
        return mWaitMsgTxt.getText().toString();
    }
	
	/**
	 * Stops animation.
	 */
	public void stopView()
	{
	    if (mWaitAnim != null)
	    {
	        mWaitAnim.stop();
	    }
	}
	
	/**
	 * Starts animation.
	 */
	public void startView()
	{
	    if (mWaitAnim != null)
	    {
	        mWaitAnim.start();
	    }
	}

}
