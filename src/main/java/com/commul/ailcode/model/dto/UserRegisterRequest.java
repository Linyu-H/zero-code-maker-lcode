package com.commul.ailcode.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 昵称
     */
    private String userName;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 验证码
     */
    private String code;
}
