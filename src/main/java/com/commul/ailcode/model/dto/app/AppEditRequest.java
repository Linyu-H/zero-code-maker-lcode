package com.commul.ailcode.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员更新应用请求（支持更新应用名称、应用封面、优先级）
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
@Data
public class AppEditRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
