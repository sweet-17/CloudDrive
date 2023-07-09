package com.qst.yunpan.service;

import com.qst.yunpan.dao.UserDao;
import com.qst.yunpan.pojo.User;
import com.qst.yunpan.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    public User findUser(User user) {
        try {
            user.setPassword(UserUtils.MD5(user.getPassword()));
            User exsitUser = userDao.findUser(user);
            return exsitUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addUser(User user){
        try {
            user.setPassword(UserUtils.MD5(user.getPassword()));
            userDao.addUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    @Autowired
//    private FileService fileService;
//    @Autowired
//    private HttpServletRequest request;

    public User findUser(String username) {
        User user = null;
        try {
            user = userDao.findUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return user;
        }
        return user;
    }

    public String getCountSize(String username){
        String countSize = null;
        try {
            countSize = userDao.getCountSize(username);
        } catch (Exception e) {
            e.printStackTrace();
            return countSize;
        }
        return countSize;
    }
}