package com.ravey.ai.user.api.service;

import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.model.req.*;
import com.ravey.ai.user.api.model.res.LoginRes;

/**
 * 用户服务接口
 *
 * @author ravey
 * @since 1.0.0
 */
public interface UsersService {

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UsersDTO getById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param req    更新请求
     * @return 更新后的用户信息
     */
    UsersDTO update(Long userId, UserUpdateReq req);

    /**
     * 发送邮箱验证码
     * 
     * @param req 请求
     */
    void sendEmailCode(EmailSendCodeReq req);

    /**
     * 邮箱注册
     * 
     * @param req 请求
     * @return 登录信息
     */
    LoginRes registerByEmail(EmailRegisterReq req);

    /**
     * 邮箱登录
     * 
     * @param req 请求
     * @return 登录信息
     */
    LoginRes loginByEmail(EmailLoginReq req);

    /**
     * 重置密码
     * 
     * @param req 请求
     */
    void resetPassword(PasswordResetReq req);
}