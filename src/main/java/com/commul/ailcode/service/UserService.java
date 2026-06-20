package com.commul.ailcode.service;

import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import com.commul.ailcode.model.dto.UserRegisterRequest;
import com.commul.ailcode.model.entity.User;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkpassword 确认密码
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request);

    /**
     * 密码加密
     * @param password 密码
     * @return
     */
    String encryptPassword(String  password);
}
