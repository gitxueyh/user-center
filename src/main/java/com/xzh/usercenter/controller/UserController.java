package com.xzh.usercenter.controller;

import com.xzh.usercenter.annotation.AuthCheck;
import com.xzh.usercenter.common.BaseResponse;
import com.xzh.usercenter.common.ErrorCode;
import com.xzh.usercenter.common.ResultUtils;
import com.xzh.usercenter.exception.BusinessException;
import com.xzh.usercenter.model.domain.User;
import com.xzh.usercenter.model.request.UserLoginRequest;
import com.xzh.usercenter.model.request.UserRegisterRequest;
import com.xzh.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

import static com.xzh.usercenter.constant.UserConstant.*;


/**
 * 用户接口
 *
 * @author xzh
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpSession session) {
        if (session == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(session);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        return ResultUtils.success(currentUser);
    }

    @GetMapping("/search")
    @AuthCheck(mustRole = ADMIN_ROLE)
    // 仅管理员可查
    public BaseResponse<List<User>> searchUsers(String username) {
        List<User> userList = userService.searchUsers(username);
        return ResultUtils.success(userList);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    // 仅管理员可删除
    public BaseResponse<Boolean> searchUsers(@RequestBody long id) {
        // id不合法
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

}
