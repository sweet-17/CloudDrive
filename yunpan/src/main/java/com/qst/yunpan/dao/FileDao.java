package com.qst.yunpan.dao;
//11-1-4 创建FileDao接口

import com.qst.yunpan.pojo.RecycleFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDao {
    void insertFiles(@Param("filePath") String filePath, @Param("userName") String userName) throws Exception;
//    18-1-5-1 添加selectFiles方法
    List<RecycleFile> selectFiles(@Param("userName") String userName) throws Exception;

//    18-2-4-1 添加方法
    RecycleFile selectFile(@Param("fileId") int fileId) throws Exception;
    void deleteFile(@Param("fileId") int fileId, @Param("userName") String userName) throws Exception;

//    18-3-5-1 添加deleteFiles方法
    void deleteFiles(@Param("userName") String userName) throws Exception;

}