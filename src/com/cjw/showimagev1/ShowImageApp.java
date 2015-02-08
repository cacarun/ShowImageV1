package com.cjw.showimagev1;

import android.app.Application;

public class ShowImageApp extends Application
{
    // 当应用程序退出后 IS_VIP 就为 false
    // 如果想一直保存选择的状态 用： SharedPreferences
    public static boolean IS_VIP = false;

    // 有两种展示图片方式，
    // show record: 可以与服务器交互 --> IS_TYPE_SHOW = false
    // show type  : 只是加载本地图片 --> IS_TYPE_SHOW = true
    public static boolean IS_TYPE_SHOW = false;

    // 如果选择show type  : 只是加载本地图片
    // 还可以可以选择图片类型
    public static String TYPE_PATH;
}
