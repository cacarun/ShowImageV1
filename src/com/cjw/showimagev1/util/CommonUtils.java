package com.cjw.showimagev1.util;

import com.cjw.showimagev1.constant.CommonConstants;

public class CommonUtils
{
    private static long mLastClickTime; // prevent double click

    public static boolean isFastDoubleClick()
    {
        long time = System.currentTimeMillis();
        long timeD = time - mLastClickTime;
        if (0 < timeD && timeD < CommonConstants.CLICK_TIME_DELAY) { return true; }
        mLastClickTime = time;
        return false;
    }
}
