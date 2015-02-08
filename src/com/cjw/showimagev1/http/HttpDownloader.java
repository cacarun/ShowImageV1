package com.cjw.showimagev1.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class HttpDownloader
{

    private HttpURLConnection urlConn = null;

    private static HttpDownloader instance;

    private HttpDownloader()
    {
        // empty
    }

    public static synchronized HttpDownloader getInstance()
    {
        if (instance == null)
        {
            instance = new HttpDownloader();
        }
        return instance;
    }

    /**
     * 根据URL下载文件，前提是这个文件当中的内容是文本，函数的返回值就是文件当中的内容 1.创建一个URL对象 2.通过URL对象，创建一个HttpURLConnection对象
     * 3.得到InputStram 4.从InputStream当中读取数据
     * 
     * @param urlStr
     * @return
     */
    public String downloadXML(String urlStr)
    {
        StringBuffer sb = new StringBuffer();
        String line = null;

        InputStream inputStream = null;
        BufferedReader buffer = null;

        try
        {
            inputStream = getInputStreamFromUrl(urlStr);
            // use I/O stream read data
            buffer = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = buffer.readLine()) != null)
            {
                sb.append(line);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Downloader xml", "Connection exception");
            return null;
        }
        finally
        {
            try
            {
                if (buffer != null)
                {
                    buffer.close();
                    buffer = null;
                }
                if (inputStream != null)
                {
                    inputStream.close();
                    inputStream = null;
                }

                disConnect();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    /**
     * 根据URL得到输入流
     * 
     * @param urlStr
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public InputStream getInputStreamFromUrl(String urlStr)
    {
        InputStream inputStream = null;
        URL url = null;
        try
        {
            url = new URL(urlStr);
            urlConn = (HttpURLConnection)url.openConnection();
            urlConn.setConnectTimeout(10 * 1000);
            urlConn.setReadTimeout(15 * 1000);

            inputStream = urlConn.getInputStream();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return inputStream;
    }

    public void disConnect()
    {
        if (urlConn != null)
        {
            urlConn.disconnect();
            urlConn = null;
        }
    }
}
