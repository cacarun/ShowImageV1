package com.cjw.showimagev1.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cjw.showimagev1.R;
import com.cjw.showimagev1.view.CommonWaitView;

/**
 * Shows wait screen.
 */
public class WaitScreenActivity extends Activity
{
    private CommonWaitView mWaitView = null;
    private Button mCancelButton = null;
    // keys for extra data of intent
    public static final String KEY_MESSAGE = "com.cjw.showimagev1.WaitScreenActivity.message";
    public static final String KEY_HAS_CANCEL = "com.cjw.showimagev1.WaitScreenActivity.cancel";

    private boolean mIsInWaitView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_wait);

        mWaitView = (CommonWaitView)findViewById(R.id.comm_waitview);
        mCancelButton = (Button)findViewById(R.id.btn_cancel);

        initialize();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mWaitView.stopView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && mIsInWaitView) { return true; }

        return super.onKeyDown(keyCode, event);
    }

    private void initialize()
    {
        Bundle data = getIntent().getExtras();
        String str = null;

        if (data != null)
        {
            str = data.getString(KEY_MESSAGE);
            mWaitView.updateView(str);
            boolean hasCancel = data.getBoolean(KEY_HAS_CANCEL);
            if (hasCancel) mCancelButton.setVisibility(View.VISIBLE);
        }

        mCancelButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });
    }

}
