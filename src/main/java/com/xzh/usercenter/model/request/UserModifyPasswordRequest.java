package com.xzh.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * Date 2023/5/18 22:06
 * author:wyf
 */
@Data
public class UserModifyPasswordRequest implements Serializable {

    private static final long serialVersionUID = 373749613989914200L;

    private String password;
    private String newPassword;

}
