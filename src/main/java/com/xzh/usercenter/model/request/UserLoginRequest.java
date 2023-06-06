package com.xzh.usercenter.model.request;

import lombok.Data;

/**
 * 用户登录请求体
 *
 * @author xzh
 */
@Data
public class UserLoginRequest {

    private String userAccount;

    private String userPassword;


}
