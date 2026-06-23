package com.commul.ailcode.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改应用请求（目前仅支持修改应用名称）
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
