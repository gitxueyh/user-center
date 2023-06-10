package com.xzh.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除
 * Date 2023/5/18 21:07
 * author:wyf
 */

@Data
public class UserDeleteRequest implements Serializable {

    private static final long serialVersionUID = 395288320416931624L;

    private Long id;
}
