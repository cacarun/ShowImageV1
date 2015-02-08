package com.cjw.showimagev1.util;

import com.cjw.showimagev1.constant.CommonConstants;


public class StrUtils
{
    public static boolean isEmpty(String str)
    {
        if (null == str || "".equals(str)) { return true; }
        return false;
    }

    /**
     * http://127.0.0.1:8080/ShowImageV1/Common/Download/NO.260/NO.260.jpg
     * 
     * result: /ShowImageV1/Common/Download/NO.260/NO.260.jpg
     * 
     * @param urlStr
     * @return
     */
    public static String getFilePathFromHttpUrl(String urlStr)
    {
        return urlStr.substring(urlStr.indexOf(CommonConstants.APP_NAME_STR));
    }

    /**
     * http://127.0.0.1:8080/ShowImageV1/Common/Download/NO.260/NO.260.jpg
     * 
     * local path: /ShowImageV1/Common/Download/NO.260/  NO.260.jpg
     * 
     * result:
     * path:      /ShowImageV1/Common/Download/NO.260/ 
     * filename:  NO.260.jpg
     * 
     * @param urlStr
     * @param isDir
     * @return
     */
    public static String getDirOrFileFromHttpUrl(String urlStr, boolean isDir)
    {
        String filePath = getFilePathFromHttpUrl(urlStr);
        if (isDir)
        {
            return filePath.substring(0, filePath.lastIndexOf("/"));
        }

        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}
