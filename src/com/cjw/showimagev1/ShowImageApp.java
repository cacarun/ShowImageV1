package com.cjw.showimagev1;

import android.app.Application;

public class ShowImageApp extends Application
{
    // ��Ӧ�ó����˳��� IS_VIP ��Ϊ false
    // �����һֱ����ѡ���״̬ �ã� SharedPreferences
    public static boolean IS_VIP = false;

    // ������չʾͼƬ��ʽ��
    // show record: ��������������� --> IS_TYPE_SHOW = false
    // show type  : ֻ�Ǽ��ر���ͼƬ --> IS_TYPE_SHOW = true
    public static boolean IS_TYPE_SHOW = false;

    // ���ѡ��show type  : ֻ�Ǽ��ر���ͼƬ
    // �����Կ���ѡ��ͼƬ����
    public static String TYPE_PATH;
}
