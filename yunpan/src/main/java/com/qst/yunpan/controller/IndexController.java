package com.qst.yunpan.controller;

import com.qst.yunpan.service.UserService;
import com.qst.yunpan.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {
    @Autowired
    private UserService userService;
    @RequestMapping("/index")
    public String index(HttpServletRequest request){
        String username = UserUtils.getUsername(request);
        String countSize = userService.getCountSize(username);
        request.setAttribute("countSize", countSize);
        return "index";
    }
}
