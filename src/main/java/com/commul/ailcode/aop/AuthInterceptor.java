package com.commul.ailcode.aop;

import com.commul.ailcode.annotation.AuthCheck;
import com.commul.ailcode.exception.BusinessException;
import com.commul.ailcode.exception.ErrorCode;
import com.commul.ailcode.exception.ThrowUtils;
import com.commul.ailcode.model.entity.User;
import com.commul.ailcode.model.enums.UserRoleEnum;
import com.commul.ailcode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.RequestContextFilter;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;
    @Autowired
    private RequestContextFilter requestContextFilter;

    /**
     * 拦截器
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        // 获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        //不需要权限，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 如果需要权限
        UserRoleEnum loginUserRole = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 判断用户是否已登录
        ThrowUtils.throwIf(loginUserRole == null, ErrorCode.NOT_LOGIN_ERROR);

        // 必须有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(loginUserRole)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
