package com.xzh.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xzh.usercenter.annotation.AuthCheck;
import com.xzh.usercenter.common.BaseResponse;
import com.xzh.usercenter.common.ErrorCode;
import com.xzh.usercenter.common.ResultUtils;
import com.xzh.usercenter.exception.BusinessException;
import com.xzh.usercenter.model.domain.User;
import com.xzh.usercenter.model.request.*;
import com.xzh.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xzh.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.xzh.usercenter.constant.UserConstant.USER_LOGIN_STATUS;


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
    public BaseResponse<Boolean> deleteUsers(@RequestBody UserDeleteRequest userDeleteRequest) {
        if (userDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userDeleteRequest.getId();
        // id不合法
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @GetMapping("/search2")
    public BaseResponse<List<User>> searchUsers(UserSearchRequest searchRequest, HttpServletRequest request) {
        // 管理员校验
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        String username = searchRequest.getUsername();
        String userAccount = searchRequest.getUserAccount();
        Integer gender = searchRequest.getGender();
        String phone = searchRequest.getPhone();
        String email = searchRequest.getEmail();
        Integer userStatus = searchRequest.getUserStatus();
        String userRole = searchRequest.getUserRole();
        String userCode = (String) searchRequest.getUserCode();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Date updateTime = searchRequest.getUpdateTime();
        Date createTime = searchRequest.getCreateTime();
        // username
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        // userAccount
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.like("userAccount", userAccount);
        }
        // gender
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.eq("gender", gender);
        }
        // phone
        if (StringUtils.isNotBlank(phone)) {
            queryWrapper.like("phone", phone);
        }
        // email
        if (StringUtils.isNotBlank(email)) {
            queryWrapper.like("email", email);
        }
        // userStatus
        if (userStatus != null) {
            queryWrapper.eq("userStatus", userStatus);
        }

        if (StringUtils.isNotBlank(userRole)) {
            queryWrapper.eq("userRole", userRole);
        }

        if (StringUtils.isNotBlank(userCode)) {
            queryWrapper.eq("plantCode", userCode);
        }

        if (updateTime != null) {
            queryWrapper.like("updateTime", updateTime);
        }
        if (createTime != null) {
            queryWrapper.like("createTime", createTime);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    @PostMapping("/search3")
    public BaseResponse<List<User>> searchUsers2(@RequestBody UserSearchRequest searchRequest, HttpServletRequest request) {
        // 管理员校验
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        String username = searchRequest.getUsername();
        String userAccount = searchRequest.getUserAccount();
        Integer gender = searchRequest.getGender();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // username
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
            List<User> userList = userService.list(queryWrapper);
            List<User> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
            return ResultUtils.success(users);
        }else if(StringUtils.isNotBlank(userAccount)){
            queryWrapper.like(User::getUserAccount, userAccount);
            List<User> userList = userService.list(queryWrapper);
            List<User> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
            return ResultUtils.success(users);
        }else if(gender!=null){
            queryWrapper.like(User::getGender, gender);
            List<User> userList = userService.list(queryWrapper);
            List<User> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
            return ResultUtils.success(users);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> users = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    @PostMapping("/add")
    public BaseResponse<Integer> addUser(@RequestBody UserAddRequest addUser, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"您没有该权限");
        }
        Integer i = userService.addUser(addUser);
        return ResultUtils.success(i);

    }

    @PostMapping("/updateUser")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest updateUser, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"您没有该权限");
        }
        User user = new User();
        BeanUtils.copyProperties(updateUser,user);
        boolean result = userService.updateById(user);
        if (!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新失败");
        }
        return ResultUtils.success(result);
    }
    /**
     * 是否为管理员
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user=(User) userObject;
        return user!=null && Objects.equals(user.getUserRole(), ADMIN_ROLE);
    }

    /**
     * 用户自身修改
     * @param updateUser
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateUserByUser(@RequestBody UserUpdateRequest updateUser,HttpServletRequest request){
        User user = new User();
        BeanUtils.copyProperties(updateUser,user);
        boolean result = userService.updateById(user);
        if (!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新失败");
        }
        return ResultUtils.success(result);
    }

    /**
     * 用户自身修改密码
     * @return
     */
    @PostMapping("/updatePassword")
    public BaseResponse<Boolean> modifyPassword(@RequestBody UserModifyPasswordRequest passwordRequest,HttpServletRequest request){
        if(userService.checkPassword(passwordRequest,request)){
            return ResultUtils.success(true);
        }else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
    }

}
