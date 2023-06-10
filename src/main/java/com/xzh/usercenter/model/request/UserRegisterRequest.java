package com.xzh.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author xzh
 */
@Data
public class UserRegisterRequest implements Serializable{

    private static final long serialVersionUID = -8213695363324810987L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
