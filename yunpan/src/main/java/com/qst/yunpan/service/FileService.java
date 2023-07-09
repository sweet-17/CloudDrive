package com.qst.yunpan.service;

import com.qst.yunpan.dao.FileDao;
import com.qst.yunpan.dao.OfficeDao;
import com.qst.yunpan.dao.UserDao;
import com.qst.yunpan.pojo.FileCustom;
import com.qst.yunpan.pojo.RecycleFile;
import com.qst.yunpan.pojo.SummaryFile;
import com.qst.yunpan.pojo.User;
import com.qst.yunpan.utils.FileUtils;
import com.qst.yunpan.utils.UserUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {
    //文件相对前缀
    public static final String PREFIX = "WEB-INF" + File.separator + "file" + File.separator;
    //新用户注册默认文件夹
    public static final String[] DEFAULT_DIRECTORY = {"vido", "music", "source", "image", User.RECYCLE};

    @Autowired
    private UserDao userDao;

    public void addNewNameSpace(HttpServletRequest request, String namespace) {
        String fileName = getRootPath(request);
        File file = new File(fileName, namespace);
        file.mkdir();
        for (String newFileName : DEFAULT_DIRECTORY) {
            File newFile = new File(file, newFileName);
            newFile.mkdir();
        }
    }

    public String getRootPath(HttpServletRequest request) {
        String rootPath = request.getSession().getServletContext().getRealPath("/") + PREFIX;
        return rootPath;
    }

    /**
     * 获取文件路径
     *
     * @param request
     * @param fileName
     * @return
     */
    public String getFileName(HttpServletRequest request, String fileName) {
        System.out.println(" ");
        System.out.println("进入getFileName");
//        fileName = fileName.replace("\\", "//");
        if (fileName == null || fileName.equals("\\")) {
            fileName = "";
        }

        fileName = fileName.replace("\\", "//");
        String username = UserUtils.getUsername(request);
        String realpath = getRootPath(request) + username + File.separator + fileName;
        System.out.println("fileName: "+fileName);
        System.out.println("username: "+username);
        System.out.println("realpath "+realpath);
        return realpath;
    }

    /**
     * 根据用户名获取文件路径
     *
     * @param request
     * @param fileName
     * @param username
     * @return
     */
    public String getFileName(HttpServletRequest request, String fileName, String username) {
        if (username == null) {
            return getFileName(request, fileName);
        }
        if (fileName == null) {
            fileName = "";
        }
        return getRootPath(request) + username + File.separator + fileName;
    }

    public List<FileCustom> listFile(String realPath) {
        File[] files = new File(realPath).listFiles();
        List<FileCustom> lists = new ArrayList<FileCustom>();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals(User.RECYCLE)) {
                    FileCustom custom = new FileCustom();
                    custom.setFileName(file.getName());
                    custom.setLastTime(FileUtils.formatTime(file.lastModified()));
                    custom.setCurrentPath(realPath);
                    if (file.isDirectory()) {
                        custom.setFileSize("-");
                    } else {
                        custom.setFileSize(FileUtils.getDataSize(file.length()));
                    }
                    custom.setFileType(FileUtils.getFileType(file));
                    lists.add(custom);
                }
            }
        }
        return lists;
    }


    //    5-3 实现文件上传
    @Autowired
    private OfficeDao officeDao;

    public void uploadFilePath(HttpServletRequest request, MultipartFile[] files, String currentPath) throws Exception {
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String filePath = getFileName(request, currentPath);
            File distFile = new File(filePath, fileName);
            if (!distFile.exists()) {
                file.transferTo(distFile);
                if ("office".equals(FileUtils.getFileType(distFile))) {
                    try {
                        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                        String documentId = FileUtils.getDocClient().createDocument(distFile, fileName, suffix).getDocumentId();
                        officeDao.addOffice(documentId, FileUtils.MD5(distFile));
                    } catch (Exception e) {
                    }
                }
            }
        }
        reSize(request);
    }

    //6-1. 重新计算文件大小
    public void reSize(HttpServletRequest request) {
        String userName = UserUtils.getUsername(request);
        try {
            userDao.reSize(userName, countFileSize(request));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //6-2 文件业务层计算文件大小
    public String countFileSize(HttpServletRequest request) {
        long countFileSize = countFileSize(new File(getFileName(request, null)));
        return FileUtils.getDataSize(countFileSize);
    }

    private long countFileSize(File srcFile) {
        File[] listFiles = srcFile.listFiles();
        if (listFiles == null) {
            return 0;
        }
        long count = 0;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                count += countFileSize(file);
            } else {
                count += file.length();
            }
        }
        return count;
    }

    // 8-1 判断用户是单一文件下载还是多个文件下载
    public File downPackage(HttpServletRequest request, String currentPath, String[] fileNames, String username) throws Exception {
        File downloadFile = null;
        if (currentPath == null) {
            currentPath = "";
        }
        //单文件length为1
        if (fileNames.length == 1) {
            downloadFile = new File(getFileName(request, currentPath, username), fileNames[0]);
            if (downloadFile.isFile()) {
                return downloadFile;
            }
        }
        String[] sourcePath = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            sourcePath[i] = getFileName(request, currentPath, username) + File.separator + fileNames[i];
        }
        String packageZipName = packageZip(sourcePath);
        downloadFile = new File(packageZipName);
        return downloadFile;
    }

    //8-2-1 将多个文件临时打包
    private String packageZip(String[] sourcePath) throws Exception {
        String zipName = sourcePath[0] + (sourcePath.length == 1 ? "" : "等" + sourcePath.length + "个文件") + ".zip";
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipName));
            for (String string : sourcePath) {
                writeZos(new File(string), "", zos);
            }
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
        return zipName;
    }

    //    8-2-2 压缩文件
    private void writeZos(File file, String basePath, ZipOutputStream zos) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles.length != 0) {
                for (File childFile : listFiles) {
                    writeZos(childFile, basePath + file.getName() + File.separator, zos);
                }
            }
        } else {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basePath + file.getName());
            zos.putNextEntry(entry);
            int count = 0;
            byte data[] = new byte[1024];
            while ((count = bis.read(data)) != -1) {
                zos.write(data, 0, count);
            }
            bis.close();
        }
    }

    //    8-2-3 将压缩包删去
    public void deleteDownPackage(File downloadFile) {
        if (downloadFile.getName().endsWith("个文件.zip")) {
            downloadFile.delete();
        }
    }

    //9-5 具体获取该分类目录下的文件名以及文件所在目录
    public List<FileCustom> searchFile(HttpServletRequest request, String currentPath, String reg, String regType) {
        List<FileCustom> list = new ArrayList<>();
        matchFile(request, list, new File(getSearchFileName(request, currentPath)), reg, regType == null ? "" : regType);
        return list;
    }

    private String getSearchFileName(HttpServletRequest request, String fileName) {
        if (fileName == null || fileName.equals("\\")) {
            System.out.println(1);
            fileName = "";
        }
        String username = UserUtils.getUsername(request);
        String realpath = getRootPath(request) + username + File.separator + fileName;
        return realpath;
    }

    private void matchFile(HttpServletRequest request, List<FileCustom> list, File dirFile,
                           String reg,
                           String regType) {
        File[] listFiles = dirFile.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile()) {
                    String suffixType = FileUtils.getFileType(file);
                    if (suffixType.equals(regType) || (reg != null && file.getName().contains(reg))) {
                        FileCustom custom = new FileCustom();
                        custom.setFileName(file.getName());
                        custom.setLastTime(FileUtils.formatTime(file.lastModified()));
                        String parentPath = file.getParent();
                        String prePath = parentPath.substring(
                                parentPath.indexOf(getSearchFileName(request, null)) + getSearchFileName(request, null).length());
                        custom.setCurrentPath(File.separator + prePath);
                        if (file.isDirectory()) {
                            custom.setFileSize("-");
                        } else {
                            custom.setFileSize(FileUtils.getDataSize(file.length()));
                        }
                        custom.setFileType(FileUtils.getFileType(file));
                        list.add(custom);
                    }
                } else {
                    matchFile(request, list, file, reg, regType);
                }
            }
        }
    }

    //10-3 添加addDirectory()方法
    public boolean addDirectory(HttpServletRequest request, String currentPath, String directoryName) {
        File file = new File(getFileName(request, currentPath), directoryName);
        return file.mkdir();
    }

    @Autowired
    private FileDao fileDao;

    //    11-1-3
    /*该方法中先将文件移动到回收站目录，即文件删除（调用io.FileUtils.moveToDirectory()可方便实现移动文件），
    然后通过fileDao将本次删除文件操作记录在数据库中；当文件删除成功时，需重新计算该用户使用的空间大小，
    最后将最新数据和删除提示返回前台显示*/
    public void delDirectory(HttpServletRequest request, String currentPath, String[] directoryName) throws Exception {
        for (String fileName : directoryName) {
            //拼接源文件的地址
            String srcPath = currentPath + File.separator + fileName;
            //根据源文件相对地址拼接 绝对路径
            File src = new File(getFileName(request, srcPath));//即将删除的文件地址
            File dest = new File(getRecyclePath(request));//回收站目录地址
            //调用commons.jar包中的moveToDirectory移动文件,移至回收站目录
            org.apache.commons.io.FileUtils.moveToDirectory(src, dest, true);
            //保存本条删除信息
            fileDao.insertFiles(srcPath, UserUtils.getUsername(request));
        }
        //重新计算文件大小
        reSize(request);
    }

    public String getRecyclePath(HttpServletRequest request) {
        return getFileName(request, User.RECYCLE);
    }

    //   12-3 将原名称改为新名称（调用java.io.File.renameTo()可方便实现重命名），最后将新命名和修改提示返回前台显示。

    public boolean renameDirectory(HttpServletRequest request, String currentPath, String srcName, String destName) {
        //根据源文件名  获取  源地址
        File file = new File(getFileName(request, currentPath), srcName);
        //同上
        File descFile = new File(getFileName(request, currentPath), destName);
        return file.renameTo(descFile);//重命名
    }

    //    13-5 循环获取该用户的文件目录信息
    public SummaryFile summarylistFile(String realPath, int number) {
        File file = new File(realPath);
        SummaryFile sF = new SummaryFile();
        List<SummaryFile> returnlist = new ArrayList<SummaryFile>();
        if (file.isDirectory()) {
            sF.setisFile(false);
            if (realPath.length() <= number) {
                sF.setFileName("yun盘");
                sF.setPath("");
            } else {
                String path = file.getPath();
                sF.setFileName(file.getName());
                //截取固定长度 的字符串，从number开始到value.length-number结束.
                sF.setPath(path.substring(number));
            }
            /* 设置抽象文件夹的包含文件集合 */
            for (File filex : file.listFiles()) {
                //获取当前文件的路径，构造该文件
                SummaryFile innersF = summarylistFile(filex.getPath(), number);
                if (!innersF.getisFile()) {
                    returnlist.add(innersF);
                }
            }
            sF.setListFile(returnlist);
            /* 设置抽象文件夹的包含文件夹个数 */
            sF.setListdiretory(returnlist.size());
        } else {
            sF.setisFile(true);
        }
        return sF;
    }

    //    13-7 用于复制目录和文件
    public void copyDirectory(HttpServletRequest request, String currentPath, String[] directoryName, String targetdirectorypath) throws Exception {
        for (String srcName : directoryName) {
            File srcFile = new File(getFileName(request, currentPath), srcName);
            File targetFile = new File(getFileName(request, targetdirectorypath), srcName);
            /* 处理目标目录中存在同名文件或文件夹问题 */
            String srcname = srcName;
            String prefixname = "";
            String targetname = "";
            if (targetFile.exists()) {
                String[] srcnamesplit = srcname.split("\\)");
                if (srcnamesplit.length > 1) {
                    String intstring = srcnamesplit[0].substring(1);
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher isNum = pattern.matcher(intstring);
                    if (isNum.matches()) {
                        srcname = srcname.substring(srcnamesplit[0].length() + 1);
                    }
                }
                for (int i = 1; true; i++) {
                    prefixname = "(" + i + ")";
                    targetname = prefixname + srcname;
                    targetFile = new File(targetFile.getParent(), targetname);
                    if (!targetFile.exists()) {
                        break;
                    }
                }
                targetFile = new File(targetFile.getParent(), targetname);
            }
            /* 复制 */
            copyfile(srcFile, targetFile);
        }
    }

    //    13-7
    private void copyfile(File srcFile, File targetFile) throws IOException {
        if (!srcFile.isDirectory()) {
            /* 如果是文件，直接复制 */
            targetFile.createNewFile();
            FileInputStream src = (new FileInputStream(srcFile));
            FileOutputStream target = new FileOutputStream(targetFile);
            FileChannel in = src.getChannel();
            FileChannel out = target.getChannel();
            in.transferTo(0, in.size(), out);
            src.close();
            target.close();
        } else {
            /* 如果是文件夹，再遍历 */
            File[] listFiles = srcFile.listFiles();
            targetFile.mkdir();
            for (File file : listFiles) {
                File realtargetFile = new File(targetFile, file.getName());
                copyfile(file, realtargetFile);
            }
        }
    }

    //    14-3
    public void moveDirectory(HttpServletRequest request, String currentPath, String[] directoryName, String targetdirectorypath) throws Exception {
        for (String srcName : directoryName) {
            File srcFile = new File(getFileName(request, currentPath), srcName);
            File targetFile = new File(getFileName(request, targetdirectorypath), srcName);
            /* 处理目标目录中存在同名文件或文件夹问题 */
            String srcname = srcName;
            String prefixname = "";
            String targetname = "";
            if (targetFile.exists()) {
                String[] srcnamesplit = srcname.split("\\)");
                if (srcnamesplit.length > 1) {
                    String intstring = srcnamesplit[0].substring(1);
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher isNum = pattern.matcher(intstring);
                    if (isNum.matches()) {
                        srcname = srcname.substring(srcnamesplit[0].length() + 1);
                    }
                }
                for (int i = 1; true; i++) {
                    prefixname = "(" + i + ")";
                    targetname = prefixname + srcname;
                    targetFile = new File(targetFile.getParent(), targetname);
                    if (!targetFile.exists()) {
                        break;
                    }
                }
                targetFile = new File(targetFile.getParent(), targetname);
            }
            /* 移动即先复制，再删除 */
            copyfile(srcFile, targetFile);
            delFile(srcFile);
        }
    }

    //    14-3 delFile()方法用于删除原目录的文件
    private void delFile(File srcFile) throws Exception {
        /* 如果是文件，直接删除 */
        if (!srcFile.isDirectory()) {
            /* 使用map 存储删除的 文件路径，同时保存用户名 */
            srcFile.delete();
            return;
        }
        /* 如果是文件夹，再遍历 */
        File[] listFiles = srcFile.listFiles();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                delFile(file);
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        if (srcFile.exists()) {
            srcFile.delete();
        }
    }

//    15-3用于根据输入的参数匹配文件
//    public List<FileCustom> searchFile(HttpServletRequest request, String currentPath, String reg, String regType) {
//        List<FileCustom> list = new ArrayList<>();
//        matchFile(request, list, new File(getFileName(request, currentPath)), reg, regType == null ? "" : regType);
//        return list;
//    }

//    private void matchFile(HttpServletRequest request, List<FileCustom> list, File dirFile, String reg, String regType) {
//        File[] listFiles = dirFile.listFiles();
//        if (listFiles != null) {
//            for (File file : listFiles) {
//                if (file.isFile()) {
//                    String suffixType = FileUtils.getFileType(file);
//                    if (suffixType.equals(regType) || (reg != null && file.getName().contains(reg))) {
//                        FileCustom custom = new FileCustom();
//                        custom.setFileName(file.getName());
//                        custom.setLastTime(FileUtils.formatTime(file.lastModified()));
//                        String parentPath = file.getParent();
//                        String prePath = parentPath.substring(
//                                parentPath.indexOf(getFileName(request, null)) + getFileName(request, null).length());
//                        custom.setCurrentPath(File.separator + prePath);
//                        if (file.isDirectory()) {
//                            custom.setFileSize("-");
//                        }else {
//                            custom.setFileSize(FileUtils.getDataSize(file.length()));
//                        }
//                        custom.setFileType(FileUtils.getFileType(file));
//                        list.add(custom);
//                    }
//                }else {
//                    matchFile(request, list, file, reg, regType);
//                }
//            }
//        }
//    }

    //    18-1-4 回收站显示所有删除文件
    public List<RecycleFile> recycleFiles(HttpServletRequest request) throws Exception {
        List<RecycleFile> recycleFiles = fileDao.selectFiles(UserUtils.getUsername(request));
        for (RecycleFile file : recycleFiles) {
            File f = new File(getRecyclePath(request), new File(file.getFilePath()).getName());
            file.setFileName(f.getName());
            file.setLastTime(FileUtils.formatTime(f.lastModified()));
        }
        return recycleFiles;
    }

    //  18-2-3 还原文件
    //难点2.还原文件时不等同于移动文件到，因为还原文件需要保存多个地址，而还原只单纯保存一个地址
    //而且还原时需要判断父子文件是否都删除了，此时就需要新建立父文件，再建立子文件，而commons.io.FileUtils则可以很好的解决问题
    public void revertDirectory(HttpServletRequest request, int[] fileId) throws Exception {
        for (int id : fileId) {
            RecycleFile file = fileDao.selectFile(id);
            String fileName = new File(file.getFilePath()).getName();
            File src = new File(getRecyclePath(request), fileName);
            File dest = new File(getFileName(request, file.getFilePath()));
            org.apache.commons.io.FileUtils.moveToDirectory(src, dest.getParentFile(), true);
            fileDao.deleteFile(id, UserUtils.getUsername(request));
        }
    }

    //    18-3-3 依次遍历recycle下各个文件，并逐一删除
    public void delAllRecycle(HttpServletRequest request) throws Exception {
        //获取回收站中的所有文件
        File file = new File(getRecyclePath(request));
        //遍历文件夹下所有文件
        File[] inferiorFile = file.listFiles();
        for (File f : inferiorFile) {
            delFile(f);//调用本类下面的delFile()方法
        }
        //根据用户进行删除
        fileDao.deleteFiles(UserUtils.getUsername(request));
        reSize(request);
    }

    //    19-1-4
    public void respFile(HttpServletResponse response, HttpServletRequest request, String currentPath, String fileName,String type) throws IOException {
        File file = new File(getFileName(request, currentPath), fileName);
        InputStream inputStream = new FileInputStream(file);
        if ("docum".equals(type)) {
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(inputStream, response.getWriter(), "UTF-8");
        } else {
            IOUtils.copy(inputStream, response.getOutputStream());
        }
    }

//    20-7-2-1
    public String openOffice(HttpServletRequest request, String currentPath, String fileName) throws Exception {
        System.out.println(" ");
        System.out.println("进入openOffice");
        System.out.println("openOffice返回值： "+officeDao.getOfficeId(FileUtils.MD5(new File(getFileName(request, currentPath), fileName))));
        return officeDao.getOfficeId(FileUtils.MD5(new File(getFileName(request, currentPath), fileName)));
    }
    //    20-7-2-2
//    public static String MD5(File file){
//        byte[] bys = null;
//        try {
//            bys = org.apache.commons.io.FileUtils.readFileToByteArray(file);
//            System.out.println(bys+"MD5");
//            return DigestUtils.md5DigestAsHex(bys).toUpperCase();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("MD5为null");
//            return null;
//        }
//    }
}
