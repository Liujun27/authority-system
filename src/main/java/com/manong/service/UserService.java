package com.manong.service;

import com.manong.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qing
 * @since 2022-10-27
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户信息
     * @param userName
     * @return
     */
    User findUserByUserName(String userName);
}
