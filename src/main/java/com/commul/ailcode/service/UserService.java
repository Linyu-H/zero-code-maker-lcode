package com.commul.ailcode.service;

import com.commul.ailcode.model.dto.UserLoginRequest;
import com.commul.ailcode.model.dto.UserQueryRequest;
import com.commul.ailcode.model.vo.LoginUserVO;
import com.commul.ailcode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import com.commul.ailcode.model.dto.UserRegisterRequest;
import com.commul.ailcode.model.entity.User;

import java.util.List;

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
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return
     */
    UserVO getUserVO(User user);


    /**
     * 获取脱敏后的用户List
     *
     * @param userList 用户列表信息
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 校验邮箱验证码
     *
     * @param userVerifyMail 待校验邮箱
     * @param userCode       用户输入的验证码
     * @param request        请求（取 session 中的验证码）
     * @return true=校验通过
     */
    Boolean checkCode(String userVerifyMail, String userCode, HttpServletRequest request);
}
