package com.commul.ailcode.controller;


import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.commul.ailcode.common.BaseResponse;
import com.commul.ailcode.common.ResultUtils;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private JavaMailSender sender; // 引入Spring Mail依赖后，会自动装配到IOC容器。用来发送邮件

    @Value("${spring.mail.username}")
    private String sendMail;

    /**
     * 同一邮箱发送冷却（毫秒），防止邮件轰炸
     */
    private static final long MAIL_COOLDOWN_MS = 60_000L;
    /**
     * 验证码有效期（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 2L;
    /**
     * 验证码位数
     */
    private static final int CODE_LEN = 6;

    /**
     * 邮箱 -> 上次发送时间戳，用于冷却控制（单机内存即可，多实例需换 Redis）
     */
    private final ConcurrentHashMap<String, Long> lastSendMap = new ConcurrentHashMap<>();

    @PostMapping("/getCode")
    public BaseResponse<String> sendMail(@RequestParam("mail") String mail, HttpServletRequest request) {
        // 参数校验：非空 + 邮箱格式
        if (StrUtil.isBlank(mail) || !Validator.isEmail(mail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 冷却控制：同一邮箱 60s 内禁止重发
        long now = System.currentTimeMillis();
        Long last = lastSendMap.get(mail);
        if (last != null && now - last < MAIL_COOLDOWN_MS) {
            long wait = (MAIL_COOLDOWN_MS - (now - last) + 999) / 1000;
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送过于频繁，请 " + wait + " 秒后再试");
        }

        try {
            // 用 SecureRandom 生成 6 位数字验证码，避免可预测
            SecureRandom random = new SecureRandom();
            int num = random.nextInt(900000) + 100000;
            String code = String.valueOf(num);

            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
            helper.setSubject("【Lcode Maker】您的验证码");
            helper.setTo(mail);
            helper.setFrom(sendMail);

            String html = buildVerifyCodeHtml(code, CODE_EXPIRE_MINUTES);
            helper.setText(html, true); // true = HTML

            sender.send(mimeMessage); // 发送 HTML 邮件

            // 发送成功后再写 session，避免失败残留脏数据
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES);
            request.getSession().setAttribute("verifyMail", mail);
            request.getSession().setAttribute("code", code);
            request.getSession().setAttribute("expireTime", expireTime);

            // 记录冷却时间
            lastSendMap.put(mail, now);
            return ResultUtils.success("发送成功");
        } catch (Exception e) {
            log.error("发送验证码邮件失败，mail={}", mail, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送失败，请联系管理");
        }
    }

    /**
     * 构造验证码 HTML 邮件正文。内联样式，兼容各大邮箱客户端。
     */
    private String buildVerifyCodeHtml(String code, long expireMinutes) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','PingFang SC','Microsoft YaHei',sans-serif;color:#333;background:#f6f7fb;border-radius:12px;overflow:hidden;border:1px solid #e6e8ef;">
                  <div style="padding:24px 28px;background:linear-gradient(135deg,#4f7cff,#6a5cff);color:#fff;">
                    <div style="font-size:18px;font-weight:600;letter-spacing:1px;">Lcode Maker</div>
                    <div style="font-size:12px;opacity:.85;margin-top:4px;">零代码应用构建平台</div>
                  </div>
                  <div style="padding:28px;">
                    <div style="font-size:15px;line-height:1.6;">您好，欢迎使用 <strong>Lcode Maker</strong>。您正在进行身份验证，请使用以下验证码完成操作：</div>
                    <div style="margin:22px 0;text-align:center;">
                      <span style="display:inline-block;font-size:32px;font-weight:700;letter-spacing:8px;color:#4f7cff;background:#fff;border:1px dashed #c5cdf6;border-radius:8px;padding:14px 28px;">%s</span>
                    </div>
                    <div style="font-size:13px;color:#8a8f9c;line-height:1.6;">
                      · 验证码有效期为 <strong style="color:#4f7cff;">%d 分钟</strong>，请尽快使用。<br/>
                      · 切勿将验证码泄露给他人，本邮件由系统自动发送，请勿直接回复。
                    </div>
                  </div>
                  <div style="padding:14px 28px;font-size:12px;color:#aab0bd;text-align:center;border-top:1px solid #eef0f5;">
                    如非本人操作，请忽略此邮件。© Lcode Maker
                  </div>
                </div>
                """.formatted(code, expireMinutes);
    }
}
