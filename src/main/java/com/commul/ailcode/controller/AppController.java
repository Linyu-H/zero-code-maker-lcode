package com.commul.ailcode.controller;

import cn.hutool.core.bean.BeanUtil;
import com.commul.ailcode.annotation.AuthCheck;
import com.commul.ailcode.common.BaseResponse;
import com.commul.ailcode.common.DeleteRequest;
import com.commul.ailcode.common.ResultUtils;
import com.commul.ailcode.constant.UserConstant;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.commul.ailcode.exception.ThrowUtils;
import com.commul.ailcode.model.dto.app.AppAddRequest;
import com.commul.ailcode.model.dto.app.AppEditRequest;
import com.commul.ailcode.model.dto.app.AppQueryRequest;
import com.commul.ailcode.model.dto.app.AppUpdateRequest;
import com.commul.ailcode.model.entity.App;
import com.commul.ailcode.model.entity.User;
import com.commul.ailcode.model.vo.AppVO;
import com.commul.ailcode.service.AppService;
import com.commul.ailcode.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    /**
     * 用户端分页每页最大条数
     */
    private static final long USER_PAGE_MAX_SIZE = 20;

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // region 用户端接口

    /**
     * 创建应用（须填写 initPrompt）
     *
     * @param appAddRequest 创建请求
     * @param request       请求
     * @return 新建应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long appId = appService.addApp(appAddRequest, loginUser);
        return ResultUtils.success(appId);
    }

    /**
     * 用户修改自己的应用（目前仅支持修改应用名称）
     *
     * @param appUpdateRequest 修改请求
     * @param request           请求
     * @return true=修改成功
     */
    @PostMapping("/my/update")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.updateApp(appUpdateRequest, loginUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户删除自己的应用
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return true=删除成功
     */
    @PostMapping("/my/delete")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.deleteApp(deleteRequest.getId(), loginUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 查看应用详情（登录用户均可，管理员同样走该接口）
     *
     * @param id 应用 id
     * @return 应用视图
     */
    @GetMapping("/get")
    public BaseResponse<AppVO> getAppById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页查询当前用户的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         请求
     * @return 应用分页
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > USER_PAGE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "每页最多 20 条");

        User loginUser = userService.getLoginUser(request);
        QueryWrapper queryWrapper = appService.getMyQueryWrapper(appQueryRequest, loginUser.getId());
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(getAppVOPage(appPage));
    }

    /**
     * 分页查询精选应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 应用分页
     */
    @PostMapping("/featured/list/page")
    public BaseResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > USER_PAGE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "每页最多 20 条");

        QueryWrapper queryWrapper = appService.getFeaturedQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(getAppVOPage(appPage));
    }

    // endregion

    // region 管理员端接口

    /**
     * 管理员更新任意应用（支持更新应用名称、应用封面、优先级）
     *
     * @param appEditRequest 更新请求
     * @return true=更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppEditRequest appEditRequest) {
        if (appEditRequest == null || appEditRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = new App();
        BeanUtil.copyProperties(appEditRequest, app);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员根据 id 删除任意应用
     *
     * @param deleteRequest 删除请求
     * @return true=删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询应用列表（支持根据除时间外的任意字段查询，每页数量不限）
     *
     * @param appQueryRequest 查询请求
     * @return 应用分页
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(appPage);
    }

    // endregion

    /**
     * 将 App 分页转换为 AppVO 分页
     *
     * @param appPage 应用分页
     * @return 应用视图分页
     */
    private Page<AppVO> getAppVOPage(Page<App> appPage) {
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }
}
