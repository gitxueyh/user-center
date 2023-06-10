package com.xzh.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author xzh
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 6767363025088201548L;

    private String userAccount;

    private String userPassword;

}
