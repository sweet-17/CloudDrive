package com.qst.yunpan.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

//    5-4-1.编写officeDao接口
@Repository
public interface OfficeDao {

    void addOffice(@Param("officeId") String officeId, @Param("officeMd5") String officeMd5) throws Exception;

    String getOfficeId(String officeMd5) throws Exception;

}