package com.cjw.showimagev1.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cjw.showimagev1.model.LocalTypeInfo;

import android.os.Environment;

public class FileUtils
{
    private static String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    private static FileUtils fileUtils;

    public String getSDPATH()
    {
        return SDPATH;
    }

    private FileUtils()
    {
        // empty
    }

    public static synchronized FileUtils getInstance()
    {
        if (fileUtils == null)
        {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    /**
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public File createSDFile(String filePath) throws IOException
    {
        File file = new File(SDPATH + "/" + filePath);
        file.createNewFile(); // 存在就不创建
        return file;
    }

    /**
     * 判断SD卡上的文件是否存在
     */
    public boolean isFileExists(String path, String fileName) {
        return new File(SDPATH + "/" + path + "/" + fileName).exists();
    }

    public boolean isFileExists(String filePath) {
        return new File(SDPATH + "/" + filePath).exists();
    }

    public long getFileSize(String path, String fileName) {
        return new File(SDPATH + "/" + path + "/" + fileName).length();
    }

    /** 
     * 获取文件的大小 
     * @param fileName 
     * @return 
     */  
    public long getFileSize(String filePath) {
        return new File(SDPATH + "/" + filePath).length();
    }

    public boolean deleteFile(String filePath)
    {
        File file = new File(SDPATH + "/" + filePath);
        if (file.exists())
        {
            return file.delete();
        }
        return true;
    }

    /**
     * 
     * @param dirName
     * @return
     */
    public File createSDDir(String dirName)
    {
        File dir = new File(SDPATH + "/" + dirName + "/");
        dir.mkdirs(); // 存在就不创建
        return dir;
    }

    /**
     * 
     * @param filePath
     * @param text
     * @return
     */
    public File write2SDFromText(String filePath, String text)
    {
        File file = null;
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        
        try
        {
            deleteFile(filePath); // 先把旧的版本删除

            file = createSDFile(filePath);

            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(text);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                    bufferedWriter = null;
                }
                if (fileWriter != null)
                {
                    fileWriter.close();
                    fileWriter = null;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public File write2SDFromInput(String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;
        
        try {
            createSDDir(path);
            file = createSDFile(path + "/" + fileName);
            
            output = new FileOutputStream(file);
            byte buffer[] = new byte[1 * 1024];
            int temp;
            while((temp = input.read(buffer)) != -1) {
                output.write(buffer, 0, temp);
            }
            output.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return file;
    }

    /**
     * Local all Image urls:
     * from path:  /Pictures/
     * to          /Pictures/Animal,Autum,Draw,Movie,Road
     * 
     * @param imagePath
     * @return
     */
    public static List<String> getPathsFromDir(String imagePath)
    {
        List<String> childImgPaths = new ArrayList<String>();

        File dirFile = new File(SDPATH + "/" + imagePath);
        if (dirFile.exists() && dirFile.isDirectory())
        {
            File[] files = dirFile.listFiles();
            for (File f : files)
            {
                if (f.isDirectory())
                {
                    childImgPaths.add(imagePath + f.getName() + "/"); // put image url file path to list
                }
            }
        }

        return childImgPaths;
    }

    /**
     * Local all Image urls:
     * from path:  /ShowImageV1/Common/Peaceful/road/
     * to          /ShowImageV1/Common/Peaceful/road/roa_001.jpg ...... roa_180.jpg
     * 
     * @param imagePath
     * @return
     */
    public static List<String> getImgURLsFromSD(String imagePath)
    {
        List<String> imgUrls = new ArrayList<String>();

        File dirFile = new File(SDPATH + "/" + imagePath);
        if (dirFile.exists() && dirFile.isDirectory())
        {
            String[] fileNames = dirFile.list();
            for (String fileName : fileNames)
            {
                imgUrls.add(imagePath + fileName); // put image url file path to list
            }
        }

        return imgUrls;
    }

    /**
     * 初始化封面图片 COVER_IMAGES
     * 
     * @param imagePaths
     * @return
     */
    public static Map<String, List<String>> getImgURLsFromSDs(List<String> imagePaths)
    {
        Map<String, List<String>> imgUrlMap = new HashMap<String, List<String>>();
        List<LocalTypeInfo> coverInfo = new ArrayList<LocalTypeInfo>();

        for (String path : imagePaths)
        {
            if (!StrUtils.isEmpty(path))
            {
                List<String> imgUrls = getImgURLsFromSD(path);

                LocalTypeInfo info = new LocalTypeInfo();
                info.setPath(path); // 之后可以根据这个key找到对应map里面的imgUrls
                info.setNum(imgUrls.size());
                info.setCoverUrl(imgUrls.get(imgUrls.size() - 1));
                coverInfo.add(info);

                imgUrlMap.put(path, imgUrls);
            }
        }

        Images.COVER_INFOS = coverInfo;
        return imgUrlMap;
    }
    
    public static String getXMLFromSD(String localXMLPath)
    {
        StringBuffer sb = new StringBuffer();
        File file = new File(SDPATH + "/" + localXMLPath);
        if (file.exists())
        {
            InputStream instream = null;
            InputStreamReader inputReader = null;
            BufferedReader buffReader = null;
            try
            {
                instream = new FileInputStream(file);
                if (instream != null)
                {
                    inputReader = new InputStreamReader(instream);
                    buffReader = new BufferedReader(inputReader);
                    String line = null;
                    while ((line = buffReader.readLine()) != null)
                    {
                        sb.append(line);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (buffReader != null)
                    {
                        buffReader.close();
                        buffReader = null;
                    }
                    if (inputReader != null)
                    {
                        inputReader.close();
                        inputReader = null;
                    }
                    if (instream != null)
                    {
                        instream.close();
                        instream = null;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public static String getImagePath(String imageUrl)
    {
        return (SDPATH + "/" + imageUrl);
    }

    /**
     * Check whether SD OK.
     * 
     * @return true when mounted, false not mounted.
     */
    public static boolean isSDMounted()
    {
        return android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getImageFile(String imageUrl)
    {
        File file = new File(SDPATH + "/" + imageUrl);
        if (file.exists())
        {
            return file;
        }
        return null;
    }
}
