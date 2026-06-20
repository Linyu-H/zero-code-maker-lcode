package com.commul.ailcode.controller;


import com.commul.ailcode.common.BaseResponse;
import com.commul.ailcode.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查
     * @return
     */
    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("OK");
    }
}

