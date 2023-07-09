package com.qst.yunpan.dao;
//11-1-5-1 创建FileDao接口


import com.qst.yunpan.pojo.Share;
import com.qst.yunpan.pojo.ShareFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareDao {
//16-2-4 在shareDao.java中添加findShare方法
    void shareFile(Share share) throws Exception;

    List<Share> findShare(Share share) throws Exception;
//    List<ShareFile> findShare(Share share) throws Exception;

//    17-1-4-1 用于从share表中查询出该用户的已分享信息
    List<Share> findShareByName(@Param("username") String username, @Param("status")  int status) throws Exception;

//    17-2-4  添加方法
    void cancelShare(@Param("url") String url, @Param("filePath")  String filePath, @Param("status") int status) throws Exception;
}