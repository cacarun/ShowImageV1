package com.cjw.showimagev1.model;

import android.graphics.Bitmap;


public class RecordInfo
{
    private String httpUrl;
    private int num;
    private String date;

    private Bitmap bitmap;

    public String getHttpUrl()
    {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl)
    {
        this.httpUrl = httpUrl;
    }

    public int getNum()
    {
        return num;
    }

    public void setNum(int num)
    {
        this.num = num;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }

}
