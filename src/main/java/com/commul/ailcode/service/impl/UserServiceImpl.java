package com.commul.ailcode.service.impl;

import cn.hutool.core.util.StrUtil;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.commul.ailcode.model.dto.UserRegisterRequest;
import com.commul.ailcode.model.entity.User;
import com.commul.ailcode.mapper.UserMapper;
import org.springframework.util.DigestUtils;
import com.commul.ailcode.service.UserService;
import org.springframework.stereotype.Service;
import com.commul.ailcode.exception.ThrowUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{
    // 盐
    private static final String SALT = "ailcode";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userName = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userEmail = userRegisterRequest.getUserEmail();
        String code = userRegisterRequest.getCode();

        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 用户名不能低于4或高于10
        ThrowUtils.throwIf(userName.length() < 4 || userName.length() > 10, ErrorCode.PARAMS_ERROR, "用户名长度不能低于4或高于10");
        // 账号长度不能少于4或大于12
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 12, ErrorCode.PARAMS_ERROR, "账号长度不能少于4或大于12");

        // 密码长度不能小于8或大于16
        ThrowUtils.throwIf(userPassword.length() < 8 || userPassword.length() > 16, ErrorCode.PARAMS_ERROR, "密码长度不能小于8或大于16");

        // 校验密码是否一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 2.检查用户是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long acc_count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(acc_count > 0, ErrorCode.PARAMS_ERROR, "用户已存在");

        // 3.加密密码
        String encryptPassword = encryptPassword(userPassword);

        // 4.检查邮箱是否已存在
        queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserEmail, userEmail);
        long email_count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(email_count > 0, ErrorCode.PARAMS_ERROR, "邮箱已存在");

        // 5.检查验证码是否正确
        Boolean codePass = checkCode(userEmail, code, request);
        ThrowUtils.throwIf(!codePass, ErrorCode.PARAMS_ERROR, "邮箱或验证码错误");

        // 6.创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userName);
        user.setUserEmail(userEmail);
        user.setCreateTime(LocalDateTime.now());

        int useId = this.mapper.insert(user);
        return useId;
    }

    @Override
    // 设计加密密码工具类
    public String encryptPassword(String password) {
        // md5加密
        return DigestUtils.md5DigestAsHex((password + SALT).getBytes());
    }

    // 检验验证码
    public Boolean checkCode(String userVerifyMail, String userCode, HttpServletRequest request){
        // 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userVerifyMail, userCode), ErrorCode.PARAMS_ERROR, "参数为空");
        HttpSession session = request.getSession(false);
        ThrowUtils.throwIf(session == null, ErrorCode.PARAMS_ERROR, "验证码不存在或已过期，请重新发送");
        //先用的session可以采用security
        String verifyMail = (String) session.getAttribute("verifyMail");
        String code = (String)session.getAttribute("code");
        LocalDateTime expireTime = (LocalDateTime) session.getAttribute("expireTime");
        // 校验session的参数
        ThrowUtils.throwIf(verifyMail == null || code == null || expireTime == null, ErrorCode.PARAMS_ERROR, "验证码不存在或已过期，请重新发送");

        LocalDateTime currentTime = LocalDateTime.now();
        // 判断是否未过期
        if (currentTime.isBefore(expireTime)) {
            // Objects.equals 自动兼容null，杜绝空指针
            boolean mailMatch = Objects.equals(userVerifyMail, verifyMail);
            boolean codeMatch = Objects.equals(userCode, code);
            if (mailMatch && codeMatch) {
                // 校验成功，立刻清空验证码，禁止重复使用
                clearAllCodeSessionAttr(session);
                return true;
            }
            return false;
        } else {
            // 验证码过期，清理全部缓存
            clearAllCodeSessionAttr(session);
            return false;
        }
    }


    /**
     * 统一清理验证码相关所有session属性，避免残留脏数据
     */
    private void clearAllCodeSessionAttr(HttpSession session) {
        session.removeAttribute("verifyMail");
        session.removeAttribute("code");
        session.removeAttribute("expireTime");
    }
}
