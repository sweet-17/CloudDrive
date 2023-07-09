package com.qst.yunpan.dao;

import com.qst.yunpan.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao {
    void addUser(User user)throws Exception;
    User checkUser(User user) throws Exception;
    User findUser(User user) throws Exception;
//    6-4 数据层查询上传信息
    String getCountSize(String username);
//    6-3-1 数据层修改上传信息
    void reSize(@Param("username") String username, @Param("formatSize") String formatSize) throws Exception;
}