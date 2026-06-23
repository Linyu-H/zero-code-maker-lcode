package com.commul.ailcode.service;

import com.commul.ailcode.model.dto.app.AppAddRequest;
import com.commul.ailcode.model.dto.app.AppQueryRequest;
import com.commul.ailcode.model.dto.app.AppUpdateRequest;
import com.commul.ailcode.model.entity.App;
import com.commul.ailcode.model.entity.User;
import com.commul.ailcode.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用（须填写 initPrompt）
     *
     * @param appAddRequest 创建请求
     * @param loginUser     当前登录用户
     * @return 新建应用 id
     */
    Long addApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 用户修改自己的应用（目前仅支持修改应用名称）
     *
     * @param appUpdateRequest 修改请求
     * @param loginUser         当前登录用户
     * @return true=修改成功
     */
    boolean updateApp(AppUpdateRequest appUpdateRequest, User loginUser);

    /**
     * 用户删除自己的应用
     *
     * @param id        应用 id
     * @param loginUser 当前登录用户
     * @return true=删除成功
     */
    boolean deleteApp(Long id, User loginUser);

    /**
     * 获取脱敏后的应用信息
     *
     * @param app 应用信息
     * @return 应用视图
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后的应用列表
     *
     * @param appList 应用列表
     * @return 应用视图列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取管理员查询条件（支持除时间外的任意字段查询）
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取当前用户的应用查询条件（支持按名称查询）
     *
     * @param appQueryRequest 查询请求
     * @param userId          当前用户 id
     * @return 查询条件
     */
    QueryWrapper getMyQueryWrapper(AppQueryRequest appQueryRequest, Long userId);

    /**
     * 获取精选应用查询条件（priority > 0，支持按名称查询）
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用流式生成代码
     *
     * @param appId      应用 id
     * @param prompt     提示词
     * @param loginUser  当前登录用户
     * @return 流式响应
     */
    Flux<String> chatToGenCode(Long appId, String prompt, User loginUser);
}
