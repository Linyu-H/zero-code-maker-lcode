package com.commul.ailcode.service;

import com.commul.ailcode.model.dto.UserLoginRequest;
import com.commul.ailcode.model.vo.LoginUserVO;
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
     * @param userRegisterRequest
     * @param request
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request);

    /**
     * 获取登录用户视图
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 密码加密
     * @param password 密码
     * @return
     */
    String encryptPassword(String  password);

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request
     * @return
     */
    Boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getloginUser(HttpServletRequest request);
}
