package com.cjw.showimagev1.work;

import android.os.AsyncTask;
import android.util.Log;

import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.feedback.Feedback;
import com.cjw.showimagev1.http.HttpDownloader;
import com.cjw.showimagev1.util.FileUtils;
import com.cjw.showimagev1.util.StrUtils;

public class DownloadXMLWork extends AsyncTask<Void, Void, Integer>
{

    private Feedback mFeedback;
    private String mServerXMLPath;
    private String mLocalXMLPath;

    private String mServerXML;

    private FileUtils mFileUtils;
    private HttpDownloader mHttpDownloader;

    public DownloadXMLWork(Feedback feedback, String serverXMLPath, String localXMLPath)
    {
        this.mFeedback = feedback;
        this.mServerXMLPath = serverXMLPath;
        this.mLocalXMLPath = localXMLPath;

        this.mFileUtils = FileUtils.getInstance();
        this.mHttpDownloader = HttpDownloader.getInstance();
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        String localXML = FileUtils.getXMLFromSD(mLocalXMLPath);
        mServerXML = mHttpDownloader.downloadXML(mServerXMLPath);
        if (!localXML.equals(mServerXML) && !StrUtils.isEmpty(mServerXML))
        {
            Log.e("DownloadXMLWork", "server xml newer than local");
            mFileUtils.write2SDFromText(mLocalXMLPath, mServerXML);
            return CommonConstants.STATUS_DIFF;
        }
        else if (localXML.equals(mServerXML) && !StrUtils.isEmpty(mServerXML))
        {
            return CommonConstants.STATUS_SAME;
        }
        return CommonConstants.STATUS_OFFLINE;
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        if (result != CommonConstants.STATUS_DIFF)
        {
            mFeedback.callBackOperator(result, CommonConstants.EMPTY_STRING);
        }
        else
        {
            mFeedback.callBackOperator(CommonConstants.STATUS_DIFF, mServerXML);
        }
    }
}
