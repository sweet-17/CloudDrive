package com.qst.yunpan.service;

import com.qst.yunpan.dao.ShareDao;
import com.qst.yunpan.pojo.Share;
import com.qst.yunpan.pojo.ShareFile;
import com.qst.yunpan.pojo.User;
import com.qst.yunpan.utils.FileUtils;
import com.qst.yunpan.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShareService {
    @Autowired
    private ShareDao shareDao;
//    16-1-4创建文件分享业务层ShareService.java，
//    添加shareFile()方法，用于处理分享文件。
//    使用循环方式将分享地址、分享人等信息进行封装，
//    再由dao层进行保存
    public String shareFile(HttpServletRequest request, String currentPath, String[] shareFile) throws Exception {
        String username = (String) request.getSession().getAttribute(User.NAMESPACE);
        String shareUrl = FileUtils.getUrl8();
        for (String file : shareFile) {
            Share share = new Share();
            share.setPath(currentPath + File.separator + file);
            share.setShareUser(username);
            share.setShareUrl(shareUrl);
            shareDao.shareFile(share);
        }
        return shareUrl;
    }

//    16-2-3 创建findShare方法和getShareFile方法用来获取分享文件
    public List<ShareFile> findShare(HttpServletRequest request, String shareUrl) throws Exception{
        Share share = new Share();
        share.setShareUrl(shareUrl);
        share.setStatus(Share.PUBLIC);
        List<Share> shares = shareDao.findShare(share);
        return getShareFile(request, shares);
    }
    public List<ShareFile> getShareFile(HttpServletRequest request, List<Share> shares){
        List<ShareFile> files = null;
        if(shares != null){
            files = new ArrayList<ShareFile>();
            String rootPath = request.getSession().getServletContext().getRealPath("/") + FileService.PREFIX;
            for (Share share : shares) {
                File file = new File(rootPath + share.getShareUser(), share.getPath());
                ShareFile shareFile = new ShareFile();
                shareFile.setFileType(FileUtils.getFileType(file));
                shareFile.setFileName(file.getName());
                shareFile.setFileSize(file.isFile() ? FileUtils.getDataSize(file.length()) : "-");
                shareFile.setLastTime(FileUtils.formatTime(file.lastModified()));
                shareFile.setShareUser(share.getShareUser());
                shareFile.setUrl(share.getShareUrl());
                shareFile.setFilePath(share.getPath());
                files.add(shareFile);
            }
        }
        return files;
    }

//  17-1-3 用于查询已分享的文件信息
    public List<ShareFile> findShareByName(HttpServletRequest request, int status) throws Exception{
        List<Share> shares = shareDao.findShareByName(UserUtils.getUsername(request), status);
        return getShareFile(request, shares);
    }

//    17-2-3 用来处理取消/删除分享链接操作
    public String cancelShare(String url, String filePath, int status) throws Exception {
		if(Share.CANCEL == status){
			shareDao.cancelShare(url, filePath, Share.DELETE);
			return "删除成功";
		}else{
			shareDao.cancelShare(url, filePath, Share.CANCEL);
			return "链接已失效";
		}
	}
}