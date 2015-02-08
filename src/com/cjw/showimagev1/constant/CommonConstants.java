package com.cjw.showimagev1.constant;

public class CommonConstants
{
    public static final String EMPTY_STRING = "";
    public static final String SHOW_IMAGE_PREFERENCE = "showimagepreference";
    public static final String IS_FIRST_IN = "isfirstin";
    public static final String LOCAL_IMAGE_PATH = "localimagepath";
    public static final String LOCAL_IMAGE_PATH_CHILD = "localimagepath_";

    public static final String IMAGE_POSITION_ID = "imagepositionid";

    public static final float MIN_SCALE = 0.95f;
    
    public static final int ANIM_TIME_DELAY = 200;
    public static final int CLICK_TIME_DELAY = 600;
    
    public static final int SCALE_COUNT = 3;
    public static final int ROTATE_DEGREE = 6;
    
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAIL = -1;
    public static final int STATUS_SAME = 2;
    public static final int STATUS_DIFF = -2;
    public static final int STATUS_OFFLINE = 3;
    
    public static final int COMMON_PAGE_SIZE = 5;
    
    public static final int SCROLL_PAGE_SIZE = 20;
    public static final int SCROLL_TIME_DELAY = 5;
    
    public static final int THREAD_NUM = 5;

    public static final String APP_NAME_STR = "ShowImageV1";
    
    public static final int IMAGE_LOAD_ORIGINAL_SIZE = 2000; // 只要这个值大于图片的宽度，就会原图加载！

    public static final int VIEW_PAGE_OFF_SCREEN_LIMIT = 2;

    /////////////////////////////////////////////////////////////////////////////////////////
    /// for Type Show
    public static final int TYPE_CENTER = 0;
    public static final int TYPE_LEFTTOP = 1;
    public static final int TYPE_RIGHTTOP = 2;
    public static final int TYPE_LEFTBELOW = 3;
    public static final int TYPE_RIGHTBELOW = 4;

    // 默认会加载这个目录下的照片
    public static final String LOCAL_DEFAULT_CAMERA_PATH = "/DCIM/Camera/";
    // 默认会加载这个目录下所有文件夹的图片
    public static final String LOCAL_DEFAULT_PIC_PATH = "/Pictures/";

    public static final String LOCAL_LIST_POS = "locallistpos";

    ///////////////////////////////////////////////////////////////////////////////////////
    /// for Record
    public static final String SERVER_XML_PATH_FOR_COMMON = "http://192.168.2.100:8080/ShowImageV1/Common/Resources.xml";
    public static final String SERVER_XML_PATH_FOR_VIP = "http://192.168.2.100:8080/ShowImageV1/VIP/Resources.xml";
    public static final String LOCAL_XML_PATH_FOR_COMMON = "/ShowImageV1/Common/Resources.xml";
    public static final String LOCAL_XML_PATH_FOR_VIP = "/ShowImageV1/VIP/Resources.xml";
    public static final String LOCAL_DOWNLOAD_IMAGE_PATH_FOR_COMMON = "/ShowImageV1/Common/Download/";
    public static final String LOCAL_DOWNLOAD_IMAGE_PATH_FOR_VIP = "/ShowImageV1/VIP/Download/";

    //////////////////////////////////////////////////////////////////////////////////////
    /// for Gallery Cover Flow
    public static final String RECORD_LIST_POS = "recordlistpos";

    
}
