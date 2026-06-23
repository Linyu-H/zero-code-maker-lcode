package com.commul.ailcode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.commul.ailcode.core.AiCodeGeneratorFacade;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.commul.ailcode.exception.ThrowUtils;
import com.commul.ailcode.mapper.AppMapper;
import com.commul.ailcode.model.dto.app.AppAddRequest;
import com.commul.ailcode.model.dto.app.AppQueryRequest;
import com.commul.ailcode.model.dto.app.AppUpdateRequest;
import com.commul.ailcode.model.entity.App;
import com.commul.ailcode.model.entity.User;
import com.commul.ailcode.model.enums.CodeGenTypeEnum;
import com.commul.ailcode.model.vo.AppVO;
import com.commul.ailcode.service.AppService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public Long addApp(AppAddRequest appAddRequest, User loginUser) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // initPrompt 必填
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "应用初始化 prompt 不能为空");

        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        app.setCreateTime(LocalDateTime.now());
        // 新建应用默认优先级 0
        if (app.getPriority() == null) {
            app.setPriority(0);
        }

        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建应用失败");
        return app.getId();
    }

    @Override
    public boolean updateApp(AppUpdateRequest appUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        Long id = appUpdateRequest.getId();
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可修改自己的应用
        ThrowUtils.throwIf(!Objects.equals(oldApp.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR);

        App app = new App();
        app.setId(id);
        // 目前仅支持修改应用名称
        app.setAppName(appUpdateRequest.getAppName());
        return this.updateById(app);
    }

    @Override
    public boolean deleteApp(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);

        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可删除自己的应用
        ThrowUtils.throwIf(!Objects.equals(oldApp.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR);

        return this.removeById(id);
    }

    @Override
    public AppVO getAppVO(App app) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return List.of();
        }
        return appList.stream()
                .map(this::getAppVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, v -> v != null && v > 0)
                .like("appName", appName, StrUtil::isNotBlank)
                .like("cover", cover, StrUtil::isNotBlank)
                .like("initPrompt", initPrompt, StrUtil::isNotBlank)
                .eq("codeGenType", codeGenType, StrUtil::isNotBlank)
                .like("deployKey", deployKey, StrUtil::isNotBlank)
                .eq("priority", priority, v -> v != null)
                .eq("userId", userId, v -> v != null && v > 0);
        // sortField 非空时才追加排序，避免 ORDER BY 空列报错
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper = queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return queryWrapper;
    }

    @Override
    public QueryWrapper getMyQueryWrapper(AppQueryRequest appQueryRequest, Long userId) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userId", userId, v -> v != null && v > 0)
                .like("appName", appName, StrUtil::isNotBlank);
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper = queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间倒序
            queryWrapper = queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appName = appQueryRequest.getAppName();
        // 精选应用：priority > 0，按优先级倒序、创建时间倒序
        return QueryWrapper.create()
                .gt("priority", 0)
                .like("appName", appName, StrUtil::isNotBlank)
                .orderBy("priority", false)
                .orderBy("createTime", false);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String prompt, User loginUser) {

        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID错误");
        ThrowUtils.throwIf(prompt == null || prompt.length() < 5, ErrorCode.PARAMS_ERROR, "请输入至少5个字符");

        // 2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3.权限校验，仅本人可以和自己的应用对话
        ThrowUtils.throwIf(!Objects.equals(app.getUserId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问");

        // 4.获取代码生成类型
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(codeGenType == null, ErrorCode.PARAMS_ERROR, "代码生成类型错误");

        // 5.调用代码生成器
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(prompt, codeGenType, appId);
    }
}
