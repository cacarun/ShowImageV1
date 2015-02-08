package com.cjw.showimagev1.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cjw.showimagev1.constant.CommonConstants;
import com.cjw.showimagev1.model.LocalTypeInfo;
import com.cjw.showimagev1.model.RecordInfo;

public class Images
{
    // ��ű��������ļ���
    public static List<String> LOCAL_IMAGE_PATHS; // ����Ŀ¼
    public static Map<String, List<String>> LOCAL_IMAGE_URLS; // keyΪĿ¼��valueΪĳһĿ¼������urls
    public static List<LocalTypeInfo> COVER_INFOS; // ���ÿ��Ŀ¼������ͼurl 

    ///////////////////////////////////////////////////////////////////
    // for Record
    // http download
    public static Map<Integer, List<String>> contentUrls;

    
    public static void initHttpImageUrls(List<RecordInfo> recordInfos)
    {
        //coverUrls = new ArrayList<String>();
        contentUrls = new HashMap<Integer, List<String>>();
        
        for (int i = 0; i < recordInfos.size(); i++)
        {
            RecordInfo info = recordInfos.get(i);
            String url = info.getHttpUrl();
            //coverUrls.add(url);
            
            ArrayList<String> urlsForEachCover = new ArrayList<String>();
            int prefix = url.lastIndexOf("/");
            int num = info.getNum();
            for (int j = 1; j <= num; j++)
            {
                // from http://127.0.0.1:8080/ShowImageV1/Common/Download/NO.260/NO.260.jpg
                // to   http://127.0.0.1:8080/ShowImageV1/Common/Download/NO.260/1.jpg  ... 23.jpg
                String formatUrl = url.substring(0, prefix) + "/%s.jpg";
                urlsForEachCover.add(String.format(formatUrl, j));
            }
            contentUrls.put(i, urlsForEachCover);
        }
    }

    ///////////////////////////////////////////////////////////////////
    // for Type
    /**
     * �õ��������е�urls(���� Ĭ��Ŀ¼�µĺ��Լ�ѡ���)
     * 
     * @param localSharedImagePaths
     */
    public static void initLocalImageUrls(List<String> localSharedImagePaths)
    {
        LOCAL_IMAGE_PATHS = FileUtils.getPathsFromDir(CommonConstants.LOCAL_DEFAULT_PIC_PATH);
        LOCAL_IMAGE_PATHS.add(CommonConstants.LOCAL_DEFAULT_CAMERA_PATH);
        
        for (String sharedPath : localSharedImagePaths)
        {
            LOCAL_IMAGE_PATHS.add(sharedPath);
        }
        
        LOCAL_IMAGE_URLS = FileUtils.getImgURLsFromSDs(LOCAL_IMAGE_PATHS);
    }
    
    public static List<String> getImageUrlsByPath(String path)
    {
        if (LOCAL_IMAGE_URLS != null && LOCAL_IMAGE_URLS.size() > 0)
        {
            return LOCAL_IMAGE_URLS.get(path);
        }
        return null;
    }
}
