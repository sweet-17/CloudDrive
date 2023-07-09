package com.qst.yunpan.controller;

import com.qst.yunpan.pojo.User;
import com.qst.yunpan.service.FileService;
import com.qst.yunpan.service.UserService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

@Controller
@RequestMapping("/user")
public class UserController {
//
    @RequestMapping("/regist")
    public String regist(HttpServletRequest request, HttpServletResponse response, User user) throws Exception{
        System.out.println(user.getUsername() +"-------"+user.getPassword());
        if(user.getUsername() == null || user.getPassword() == null||user.getUsername().equals("")||user.getPassword().equals("")){
            request.setAttribute("msg", "请输入用户名和密码");
            return "regist";
        }
        else{
            boolean isSuccess = userService.addUser(user);
            if(isSuccess){
                fileService.addNewNameSpace(request,user.getUsername());
                return "login";
            }
            else{
                request.setAttribute("msg", "注册失败");
                return "regist";
            }
        }
    }
//
    @RequestMapping("/login")
    public String login(HttpServletRequest request, User user){
        if(user.getUsername()==null||user.getUsername().equals("")||user.getPassword()==null||user.getPassword().equals("")){
            request.setAttribute("msg", "请输入用户名或密码");
            return "login";
        }
        User exsitUser = userService.findUser(user);
        if(exsitUser != null){
            HttpSession session = request.getSession();
            session.setAttribute(User.NAMESPACE, exsitUser.getUsername());
            session.setAttribute("totalSize", exsitUser.getTotalSize());
            return "redirect:/index.action";
        }else{
            request.setAttribute("msg", "用户名或密码错误");
            return "login";
        }
    }
//
    @RequestMapping("/logout")
        public String logout(HttpServletRequest request){
        request.getSession().invalidate();
        return "redirect:/user/login.action";
    }

    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;

//    25-3-9
    @RequestMapping("/loginForApp")
    public void getjson(HttpServletRequest req,HttpServletResponse rep)
            throws Exception {
        String username = req.getParameter("username");String password = req. getParameter("password");User user = new User() ;
        user.setUsername(username);
        user.setPassword(password);
        System. out. println("安卓端访问中......");

        PrintWriter writer = rep.getWriter();
        JSONObject object = new JSONObject();
        User exsitUser = userService.findUser(user);
        if(exsitUser != null){
            HttpSession session = req. getSession();
            session. setAttribute(User. NAMESPACE,exsitUser.getUsername());
            session. setAttribute("totalSize",exsitUser.getTotalSize());
            //object.put("result", exsitUser);
            object.put("ret", "1000");
            object. put("msg","登录成功");
            object.put("data", exsitUser);
        }else {
            //object.put ("result"， "fail");
             object.put("ret","1001");
             object.put("msg","登录失败");
        }
        writer. println(object.toString());
        writer. flush();
        writer. close();
    }
}