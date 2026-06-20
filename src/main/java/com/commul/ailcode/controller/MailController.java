package com.commul.ailcode.controller;


import com.commul.ailcode.common.BaseResponse;
import com.commul.ailcode.common.ResultUtils;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private JavaMailSender sender; // 引入Spring Mail依赖后，会自动装配到IOC容器。用来发送邮件

    @Value("${spring.mail.username}")
    private String sendMail;


    @GetMapping("/getCode")
    public BaseResponse<String> sendMail(String mail, HttpServletRequest request) {
        try{
            // 生成 6 位数字验证码
            Random random = new Random();
            int num = random.nextInt(900000) + 100000;
            String code = String.valueOf(num);

            // 当前时间
            LocalDateTime currentTime = LocalDateTime.now();

            //2min有效时间
            LocalDateTime expireTime = currentTime.plusMinutes(2);

            //存储到session
            request.getSession().setAttribute("expireTime", expireTime);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("欢迎使用Lcode Maker！！！"); // 发送邮件的标题
            message.setText("登录操作，验证码："+ code + "，切勿将验证码泄露给他人，本条验证码有效期2分钟。"); // 发送邮件的内容
            message.setTo(mail); // 指定要接收邮件的用户邮箱账号
            message.setFrom(sendMail); // 发送邮件的邮箱账号

            sender.send(message); // 调用send方法发送邮件即可

            //先用的session可以采用security
            request.getSession().setAttribute("verifyMail",mail);
            request.getSession().setAttribute("code",code);
            request.getSession().setAttribute("expireTime",expireTime);
            request.getSession().setMaxInactiveInterval(60*2);
            return ResultUtils.success("发送成功");
        }
        catch (Exception e){
            log.error("发送验证码邮件失败，mail={}", mail, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送失败，请联系管理");
        }
    }
}
