package com.commul.ailcode.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.commul.ailcode.model.dto.UserLoginRequest;
import com.commul.ailcode.model.dto.UserQueryRequest;
import com.commul.ailcode.model.vo.LoginUserVO;
import com.commul.ailcode.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.commul.ailcode.model.dto.UserRegisterRequest;
import com.commul.ailcode.model.entity.User;
import com.commul.ailcode.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import com.commul.ailcode.service.UserService;
import org.springframework.stereotype.Service;
import com.commul.ailcode.exception.ThrowUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.commul.ailcode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/Linyu-H">lcode</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{
    // 盐
    private static final String SALT = "ailcode";
    /** 登录失败上限，达到后锁定账号 */
    private static final int LOGIN_MAX_FAIL = 5;
    /** 锁定时长（毫秒） */
    private static final long LOGIN_LOCK_MS = 5 * 60_000L;

    /** 登录失败计数：key=规整化后的账号/邮箱，value=失败次数 + 锁定截止时间戳 */
    private final ConcurrentHashMap<String, long[]> loginFailMap = new ConcurrentHashMap<>();

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userName = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userEmail = userRegisterRequest.getUserEmail();
        String code = userRegisterRequest.getCode();

        // 1.校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 用户名不能低于4或高于10
        ThrowUtils.throwIf(userName.length() < 4 || userName.length() > 10, ErrorCode.PARAMS_ERROR, "用户名长度不能低于4或高于10");
        // 账号长度不能少于4或大于12
        ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 12, ErrorCode.PARAMS_ERROR, "账号长度不能少于4或大于12");

        // 密码长度不能小于8或大于16
        ThrowUtils.throwIf(userPassword.length() < 8 || userPassword.length() > 16, ErrorCode.PARAMS_ERROR, "密码长度不能小于8或大于16");

        // 校验密码是否一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 2.检查用户是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long acc_count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(acc_count > 0, ErrorCode.PARAMS_ERROR, "用户已存在");

        // 3.加密密码
        String encryptPassword = encryptPassword(userPassword);

        // 4.检查邮箱是否已存在
        queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserEmail, userEmail);
        long email_count = this.mapper.selectCountByQuery(queryWrapper);
        ThrowUtils.throwIf(email_count > 0, ErrorCode.PARAMS_ERROR, "邮箱已存在");

        // 5.检查验证码是否正确
        Boolean codePass = checkCode(userEmail, code, request);
        ThrowUtils.throwIf(!codePass, ErrorCode.PARAMS_ERROR, "邮箱或验证码错误");

        // 6.创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userName);
        user.setUserEmail(userEmail);
        user.setCreateTime(LocalDateTime.now());

        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存用户失败，请联系技术人员");
        return user.getId();// 组件回传用户id
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    // 设计加密密码工具类
    public String encryptPassword(String password) {
        // md5加密
        return DigestUtils.md5DigestAsHex((password + SALT).getBytes());
    }

    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 1.校验参数
        String accountOrMail = userLoginRequest.getAccountOrMail();
        String userPassword = userLoginRequest.getUserPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(accountOrMail, userPassword), ErrorCode.PARAMS_ERROR, "账号或密码为空");

        // 2.规整化：去首尾空格；邮箱转小写，避免大小写不一致查不到
        accountOrMail = accountOrMail.trim();
        String loginKey = Validator.isEmail(accountOrMail) ? accountOrMail.toLowerCase() : accountOrMail;

        // 3.登录失败锁定检查
        long now = System.currentTimeMillis();
        long[] fail = loginFailMap.get(loginKey);
        if (fail != null && fail.length >= 2 && now < fail[1]) {
            long waitSec = (fail[1] - now + 999) / 1000;
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被锁定，请 " + waitSec + " 秒后再试");
        }

        // 4.查询用户（按邮箱或账号）
        QueryWrapper queryWrapper = new QueryWrapper();
        if (Validator.isEmail(loginKey)) {
            queryWrapper.eq(User::getUserEmail, loginKey);
        } else {
            queryWrapper.eq(User::getUserAccount, loginKey);
        }
        User user = this.mapper.selectOneByQuery(queryWrapper);

        // 5.校验密码（用户不存在时也走一次加密，避免通过响应时间枚举账号）
        String encryptPassword = encryptPassword(userPassword);
        boolean passwordMatch = user != null && encryptPassword.equals(user.getUserPassword());

        if (!passwordMatch) {
            // 记录失败次数：fail[0]=失败次数，fail[1]=锁定截止时间戳
            long[] cur = loginFailMap.compute(loginKey, (k, v) -> {
                long cnt = (v == null || now >= v[1]) ? 0 : v[0];
                return new long[]{cnt + 1, 0};
            });
            if (cur[0] >= LOGIN_MAX_FAIL) {
                cur[1] = now + LOGIN_LOCK_MS;
                loginFailMap.put(loginKey, cur);
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "密码错误次数过多，账号已被锁定 " + (LOGIN_LOCK_MS / 60_000L) + " 分钟");
            }
            // 统一报「账号或密码错误」，不区分用户存在与否，防账号枚举
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误，剩余 " + (LOGIN_MAX_FAIL - cur[0]) + " 次机会");
        }

        // 6.登录成功：清空失败计数
        loginFailMap.remove(loginKey);

        // 7.存储脱敏后的登录态（不把密码哈希放进 session）
        LoginUserVO loginUserVO = getLoginUserVO(user);
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        // 判断用户是否登陆（session 中存的是脱敏后的 LoginUserVO）
        Object attr = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(attr instanceof LoginUserVO)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录，请先登录");
        }

        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1.判断用户是否登陆（session 中存的是脱敏后的 LoginUserVO）
        Object attr = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(attr instanceof LoginUserVO)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        LoginUserVO loginUserVO = (LoginUserVO) attr;
        Long userId = loginUserVO.getId();
        ThrowUtils.throwIf(userId == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2.从数据库查询当前用户信息
        QueryWrapper queryWrapper = new QueryWrapper()
                .eq(User::getId, userId);
        User loginUser = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        return loginUser;
    }

    @Override
    public UserVO getUserVO(User user) {
        // 校验参数
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);

        // 获取脱敏后的用户信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        // 校验参数
        if (CollUtil.isEmpty(userList)) {
            return List.of();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userEmail = userQueryRequest.getUserEmail();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, v -> v != null && v > 0)
                .eq("userRole", userRole, StrUtil::isNotBlank)
                .eq("userEmail", userEmail, StrUtil::isNotBlank)
                .like("userAccount", userAccount, StrUtil::isNotBlank)
                .like("userName", userName, StrUtil::isNotBlank)
                .like("userProfile", userProfile, StrUtil::isNotBlank);
        // sortField 非空时才追加排序，避免 ORDER BY 空列报错
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper = queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return queryWrapper;
    }


    // 检验验证码
    @Override
    public Boolean checkCode(String userVerifyMail, String userCode, HttpServletRequest request){
        // 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userVerifyMail, userCode), ErrorCode.PARAMS_ERROR, "参数为空");
        HttpSession session = request.getSession(false);
        ThrowUtils.throwIf(session == null, ErrorCode.PARAMS_ERROR, "验证码不存在或已过期，请重新发送");
        //先用的session可以采用security
        String verifyMail = (String) session.getAttribute("verifyMail");
        String code = (String)session.getAttribute("code");
        LocalDateTime expireTime = (LocalDateTime) session.getAttribute("expireTime");
        // 校验session的参数
        ThrowUtils.throwIf(verifyMail == null || code == null || expireTime == null, ErrorCode.PARAMS_ERROR, "验证码不存在或已过期，请重新发送");

        LocalDateTime currentTime = LocalDateTime.now();
        // 判断是否未过期
        if (currentTime.isBefore(expireTime)) {
            // Objects.equals 自动兼容null，杜绝空指针
            boolean mailMatch = Objects.equals(userVerifyMail, verifyMail);
            boolean codeMatch = Objects.equals(userCode, code);
            if (mailMatch && codeMatch) {
                // 校验成功，立刻清空验证码，禁止重复使用
                clearAllCodeSessionAttr(session);
                return true;
            }
            return false;
        } else {
            // 验证码过期，清理全部缓存
            clearAllCodeSessionAttr(session);
            return false;
        }
    }


    /**
     * 统一清理验证码相关所有session属性，避免残留脏数据
     */
    private void clearAllCodeSessionAttr(HttpSession session) {
        session.removeAttribute("verifyMail");
        session.removeAttribute("code");
        session.removeAttribute("expireTime");
    }
}
