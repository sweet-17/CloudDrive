package com.qst.yunpan.utils;

import com.baidubce.BceClientConfiguration;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.doc.DocClient;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileUtils {
    public static String getDataSize(long size) {
        DecimalFormat formater = new DecimalFormat("####.0");
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "-";
        }
    }

    public static String formatTime(long time){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }

    public static String getUrl8(){
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
    public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();
    static{
        FILE_TYPE_MAP.put("jpg", "image");
        FILE_TYPE_MAP.put("png", "image");
        FILE_TYPE_MAP.put("gif", "image");
        FILE_TYPE_MAP.put("tif", "image");
        FILE_TYPE_MAP.put("bmp", "image");
        FILE_TYPE_MAP.put("bmp", "image");
        FILE_TYPE_MAP.put("bmp", "image");

        FILE_TYPE_MAP.put("html", "docum");
        FILE_TYPE_MAP.put("htm" , "docum");
        FILE_TYPE_MAP.put("css" , "docum");
        FILE_TYPE_MAP.put("js"  , "docum");
        FILE_TYPE_MAP.put("ini" , "docum");
        FILE_TYPE_MAP.put("txt" , "docum");
        FILE_TYPE_MAP.put("jsp" , "docum");
        FILE_TYPE_MAP.put("sql" , "docum");
        FILE_TYPE_MAP.put("xml" , "docum");
        FILE_TYPE_MAP.put("java", "docum");
        FILE_TYPE_MAP.put("bat" , "docum");
        FILE_TYPE_MAP.put("mxp" , "docum");
        FILE_TYPE_MAP.put("properties", "docum");

        FILE_TYPE_MAP.put("doc", "office");
        FILE_TYPE_MAP.put("docx", "office");
        FILE_TYPE_MAP.put("vsd", "office");
        FILE_TYPE_MAP.put("mdb", "office");
        FILE_TYPE_MAP.put("pdf", "office");
        FILE_TYPE_MAP.put("xlsx", "office");
        FILE_TYPE_MAP.put("xls", "office");
        FILE_TYPE_MAP.put("pptx", "office");
        FILE_TYPE_MAP.put("ppt", "office");
        FILE_TYPE_MAP.put("wps", "office");

        FILE_TYPE_MAP.put("mov","vido");
        FILE_TYPE_MAP.put("rmvb","vido");
        FILE_TYPE_MAP.put("flv","vido");
        FILE_TYPE_MAP.put("mp4","vido");
        FILE_TYPE_MAP.put("avi","vido");
        FILE_TYPE_MAP.put("wav","vido");
        FILE_TYPE_MAP.put("wmv","vido");
        FILE_TYPE_MAP.put("mpg","vido");

        FILE_TYPE_MAP.put("mp3","audio");
        FILE_TYPE_MAP.put("mid","audio");

        FILE_TYPE_MAP.put("zip", "comp");
        FILE_TYPE_MAP.put("rar", "comp");
        FILE_TYPE_MAP.put("gz" , "comp");

    }
    public static String getFileType(File file) {
        if(file.isDirectory()){
            return "folder-open";
        }
        String fileName = file.getPath();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String fileType = FILE_TYPE_MAP.get(suffix);
        return fileType == null ? "file" : fileType;
    }
    private static DocClient docClient = null;
    public static DocClient getDocClient(){
        if(docClient == null){
            BceClientConfiguration config = new BceClientConfiguration();
            config.setConnectionTimeoutInMillis(3000);
            config.setSocketTimeoutInMillis(2000);
            config.setCredentials(new DefaultBceCredentials("ALTAK4KDpfQzvA7kwQzDE2wQpX", "1853d78a83a747f7b5361a522104e3c8"));
            docClient = new DocClient(config);
        }
        return docClient;
    }

    public static String MD5(File file){
        byte[] bys = null;
        try {
            bys = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            System.out.println(" ");
            System.out.println("进入MD5 ");
            System.out.println("bys "+bys);
            System.out.println("MD5返回值 "+DigestUtils.md5DigestAsHex(bys).toUpperCase());
            return DigestUtils.md5DigestAsHex(bys).toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("MD5为null");
            return null;
        }
    }
}
