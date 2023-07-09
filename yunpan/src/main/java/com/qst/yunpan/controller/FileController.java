package com.qst.yunpan.controller;

import com.qst.yunpan.pojo.FileCustom;
import com.qst.yunpan.pojo.RecycleFile;
import com.qst.yunpan.pojo.Result;
import com.qst.yunpan.pojo.SummaryFile;
import com.qst.yunpan.service.FileService;
import com.qst.yunpan.utils.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/file")

public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/getFiles")
    public @ResponseBody
    Result<List<FileCustom>> getFiles(String path) {
        //根据项目路径及用户名、文件名获取上传文件的真实路径
        String realPath = fileService.getFileName(request, path);
        //获取路径下所有的文件信息
        List<FileCustom> listFile = fileService.listFile(realPath);
        //将文件信息封装成Json对象
        Result<List<FileCustom>> result = new Result<List<FileCustom>>(325,
                true, "获取成功");
        result.setData(listFile);
        return result;
    }

    // 任务5-上传文件-第2步
    @RequestMapping("/upload")
    public @ResponseBody
    Result<String> upload(
            @RequestParam("files") MultipartFile[] files, String currentPath) {
        try {
            fileService.uploadFilePath(request, files, currentPath);
        } catch (Exception e) {
            return new Result<>(301, false, "上传失败");
        }
        return new Result<String>(305, true, "上传成功");
    }

    // 7-2. 用于处理下载数据的请求并返回下载的结果给前台
    @RequestMapping("/download")
    public ResponseEntity<byte[]> download(String currentPath, String[] downPath, String username) {
        try {
            String down = request.getParameter("downPath");
            File downloadFile = fileService.downPackage(request, currentPath, downPath, username);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = new String(downloadFile.getName().getBytes("utf-8"), "iso-8859-1");
            headers.setContentDispositionFormData("attachment", fileName);
            byte[] fileToByteArray = org.apache.commons.io.FileUtils.readFileToByteArray(downloadFile);
            fileService.deleteDownPackage(downloadFile);
            return new ResponseEntity<byte[]>(fileToByteArray, headers, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //9-4 根据前台所传来的分类信息，获取该用户所对应的分类文件地址，并返回前台显示
    @RequestMapping("/searchFile")
    public @ResponseBody
    Result<List<FileCustom>> searchFile(
            HttpServletRequest request,
            String currentPath,
            String reg,
            String regType) {
        try {
            List<FileCustom> searchFile = fileService.searchFile(
                    request,
                    currentPath,
                    reg,
                    regType);
            Result<List<FileCustom>> result = new Result<>(376, true, "查找成功");
            result.setData(searchFile);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(371, false, "查找失败");
        }
    }

    //    10-2 接收和处理新建文件夹请求
    @RequestMapping("/addDirectory")
    public @ResponseBody
    Result<String> addDirectory(String currentPath, String directoryName) {
        try {
            fileService.addDirectory(request, currentPath, directoryName);
            return new Result<>(336, true, "添加成功");
        } catch (Exception e) {
            return new Result<>(331, false, "添加失败");
        }
    }

    //    11-1-2 接收和处理删除请求
    @RequestMapping("/delDirectory")
    public @ResponseBody
    Result<String> delDirectory(String currentPath, String[] directoryName) {
        try {
            fileService.delDirectory(request, currentPath, directoryName);
            return new Result<>(346, true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(341, false, "删除失败");
        }
    }

    //    12-2 接收和处理重命名请求
    @RequestMapping("/renameDirectory")
    public @ResponseBody
    Result<String> renameDirectory(String currentPath, String srcName, String destName) {
        try {
            fileService.renameDirectory(request, currentPath, srcName, destName);
            return new Result<>(356, true, "重命名成功");
        } catch (Exception e) {
            return new Result<>(351, false, "重命名失败");
        }
    }

    //    13-4 用于获取该用户的所有目录结构并返回前台
    @RequestMapping("/summarylist")
    public String summarylist(Model model) {
        String webrootpath = fileService.getFileName(request, "");
        int number = webrootpath.length();
        SummaryFile rootlist = fileService.summarylistFile(webrootpath, number);
        model.addAttribute("rootlist", rootlist);
        return "summarylist";
    }

    //    13-6 点击“确定”按钮时，请求将由FileController控制器中的copyDirectory()方法进行处理；copyDirectory()方法中对文件进行复制
    @RequestMapping("/copyDirectory")
    public @ResponseBody
    Result<String> copyDirectory(String currentPath, String[] directoryName, String targetdirectorypath) throws Exception {
        try {
            fileService.copyDirectory(request, currentPath, directoryName,
                    targetdirectorypath);
            return new Result<>(366, true, "复制成功");
        } catch (IOException e) {
            return new Result<>(361, true, "复制失败");
        }
    }

    //    14-2 当点击“确定”按钮时由该方法完成文件的移动
    @RequestMapping("/moveDirectory")
    public @ResponseBody
    Result<String> moveDirectory(String currentPath, String[] directoryName, String targetdirectorypath) {
        try {
            fileService.moveDirectory(request, currentPath, directoryName, targetdirectorypath);
            return new Result<>(366, true, "移动成功");
        } catch (Exception e) {
            return new Result<>(361, true, "移动失败");
        }
    }

//    15-2用于根据前台所传来的分类信息，获取该用户所对应的分类文件地址，并返回前台显示。
//    相比之前的文件查询，本次请求中多了一个关键词的参数
//    @RequestMapping("/searchFile")
//    public @ResponseBody Result<List<FileCustom>> searchFile(String reg,String currentPath, String regType) {
//        try {
//            List<FileCustom> searchFile = fileService.searchFile(request,
//                    currentPath, reg, regType);
//            Result<List<FileCustom>> result = new Result<>(376, true, "查找成功");
//            result.setData(searchFile);
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Result<>(371, false, "查找失败");
//        }
//    }

    //    18-1-3 获取当前用户回收站下的文件信息
    @RequestMapping("/recycleFile")
    public String recycleFile() {
        try {
            List<RecycleFile> findDelFile = fileService.recycleFiles(request);
            if (null != findDelFile && findDelFile.size() != 0) {
                request.setAttribute("findDelFile", findDelFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "recycle";
    }


    //    18-2-2 还原回收站文件-- --获取目的路径以及文件名
    @RequestMapping("/revertDirectory")
    public @ResponseBody
    Result<String> revertDirectory(int[] fileId) {
        try {
            fileService.revertDirectory(request, fileId);
            return new Result<>(327, true, "还原成功");
        } catch (Exception e) {
            return new Result<>(322, false, "还原失败");
        }
    }

    //    18-3-1 清空回收站
    @RequestMapping("/delAllRecycle")
    public @ResponseBody
    Result<String> delAllRecycleDirectory() {
        try {
            fileService.delAllRecycle(request);
            // 返回状态码
            return new Result<>(327, true, "删除成功");
        } catch (Exception e) {
            return new Result<>(322, false, "删除失败");
        }
    }

    //    19-1-3 接受和处理在线图片/txt预览功能
    @RequestMapping("/openFile")
    public void openFile(HttpServletResponse response, String currentPath, String fileName, String fileType) {
        try {
            fileService.respFile(response, request, currentPath, fileName, fileType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //20-1-7 在线预览Office文档
    @RequestMapping("/openOffice")
    public @ResponseBody
    Result<String> open0ffice(String currentPath,
                              String fileName, String fileType) {
        System.out.println(" ");
        System.out.println("进入FileController + openOffice");
        try {
            String openOffice = fileService.openOffice(request, currentPath, fileName);
            if (openOffice != null) {
                System.out.println("openOffice不为null");
                Result<String> result = new Result<>(505, true, "打开成功");
                result.setData(openOffice);
                return result;
            }
            return new Result<>(501, false, "打开失败");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(501, false, "打开失败");
        }
    }

//    21-3-4
@RequestMapping("/openAudioPage")
public String openAudioPage(Model model, String currentPath, String fileName) {
    model.addAttribute("currentPath", currentPath);
    model.addAttribute("fileName", fileName);
    return "audio";
    }
}

