package com.xzh.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xzh.usercenter.model.domain.User;
import com.xzh.usercenter.model.request.UserAddRequest;
import com.xzh.usercenter.model.request.UserModifyPasswordRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 用户服务
 *
 * @author xzh
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @param planetCode 星球编号
     * @return 新用户 ID
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    List<User> searchUsers(String username);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

  /**
   * 用户注销
   *
   * @param session
   * @return
   */
  int userLogout(HttpSession session);

    /**
     * 添加新用户
     * @param addUser
     * @return
     */

    int addUser(UserAddRequest addUser);

    /**
     * 校验密码是否一致
     * @param request
     * @return
     */
    boolean checkPassword(UserModifyPasswordRequest passwordRequest, HttpServletRequest request);
}
