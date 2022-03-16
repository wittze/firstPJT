package com.zewei.firstpjt.controller;

import com.zewei.firstpjt.annotation.LoginRequired;
import com.zewei.firstpjt.entity.User;
import com.zewei.firstpjt.service.UserService;
import com.zewei.firstpjt.util.CommunityUtil;
import com.zewei.firstpjt.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        //获取文件原名
        String fileName = headerImage.getOriginalFilename();
        //获取文件的格式(.xxx)
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        // 生成新的随机的文件名，防止重复
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件，将数据保存到一个目标文件中
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)，下一个方法
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //读取头像图片
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    //改密,比较的是MD5，要注意
    @LoginRequired
    @RequestMapping(path = "/changepwd", method = RequestMethod.POST)
    public String changePwd(String oldPwd,String newPwd,String newPwd2, Model model) {
        //取出用户
        User user = hostHolder.getUser();

        //旧密码对不对
        String oldPwdMD5 = CommunityUtil.md5(oldPwd + user.getSalt());
        if (!user.getPassword().equals(oldPwdMD5)) {
            model.addAttribute("changeError1", "原密码不正确!");
            return "/site/setting";
        }
        //没有输入
        if (StringUtils.isBlank(newPwd)) {
            model.addAttribute("changeError", "密码不能为空!");
            return "/site/setting";
        }

        //检查新旧是否一样
        String newPwdMD5 = CommunityUtil.md5(newPwd + user.getSalt());
        if (oldPwdMD5.equals(newPwdMD5)) {
            model.addAttribute("changeError", "新密码不能和旧密码一致!");
            return "/site/setting";
        }

        //检查新旧是否一样
        if (!newPwd.equals(newPwd2)) {
            model.addAttribute("changeError2", "两次输入密码不一致!");
            return "/site/setting";
        }

        //无问题，更新
        userService.updatePassword(user.getId(), newPwdMD5);

        //修改成功跳转到登录界面
        return "redirect:/login";
    }

}
