package com.cjw.showimagev1.model;

import android.graphics.Bitmap;

public class LocalTypeInfo
{
    private int num;
    private String coverUrl; // ·âÃæµÄÍ¼Æ¬
    private String path; // Ä¿Â¼
    private Bitmap bitmap;

    public int getNum()
    {
        return num;
    }

    public void setNum(int num)
    {
        this.num = num;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
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
