package com.xzh.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzh.usercenter.common.ErrorCode;
import com.xzh.usercenter.exception.BusinessException;
import com.xzh.usercenter.mapper.UserMapper;
import com.xzh.usercenter.model.domain.User;
import com.xzh.usercenter.model.request.UserAddRequest;
import com.xzh.usercenter.model.request.UserModifyPasswordRequest;
import com.xzh.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.xzh.usercenter.common.ErrorCode.NOT_LOGIN;
import static com.xzh.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * 用户服务实现类
 *
 * @author xzh
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，用于混淆密码
     */
    private static final String SALT = "Xueyuehua";

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验
        // 账户、密码不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 密码长度不小于8
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
        String validPattern = "^\\w+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }
        // 密码和确认密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码输入不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }

            // 星球编号不能重复
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("planet_code", userAccount);
            count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setPlanetCode(planetCode);
            boolean saveResult = this.save(user);
            // 保存失败
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        // 账户、密码不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 密码长度不小于8
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "^\\w+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 校验账号和密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.getOne(queryWrapper);
        // 账号或密码错误
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        // 3. 用户脱敏, 隐藏敏感信息
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);
        return safetyUser;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        // 先判断是否已登录
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        return this.getSafetyUser(currentUser);
    }

    /**
     * 新增用户
     * @param addUser
     * @return
     */
    @Override
    public int addUser(UserAddRequest addUser) {
        User user = new User();
        BeanUtils.copyProperties(addUser,user);
        String userPassword=addUser.getUserPassword();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user.setUserPassword(encryptPassword);
        return userMapper.insert(user);
    }

    @Override
    public boolean checkPassword(UserModifyPasswordRequest passwordRequest, HttpServletRequest request) {
        String password = passwordRequest.getPassword();
        //获取用户输入的密码的加密状态
        String userPassword = DigestUtils.md5DigestAsHex((SALT +password).getBytes());
        //获取当前用户
        User currentUser = getCurrentUser(request);
        String checkPassword = currentUser.getUserPassword();

        if(userPassword.equals(checkPassword)){
            String newPassword = passwordRequest.getNewPassword();
            String newPassword1 = DigestUtils.md5DigestAsHex((SALT +newPassword).getBytes());
            currentUser.setUserPassword(newPassword1);
            this.updateById(currentUser);
            return true;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"输入密码有误");
        }
    }

    @Override
    public List<User> searchUsers(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> users = this.list(queryWrapper);
        //脱敏
        return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }


    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpSession session) {
        session.removeAttribute(USER_LOGIN_STATUS);
        return 1;
    }
}




