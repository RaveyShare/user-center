package com.ravey.ai.user.api.enums;

import com.ravey.common.api.exception.ErrorCode;

/**
 * 用户中心错误码枚举
 *
 * @author Ravey
 * @since 1.0.0
 */
public enum UserErrorCode implements ErrorCode {

    // 通用错误 1000-1099
    SUCCESS(0, "操作成功"),
    PARAM_ERROR(1001, "参数错误"),
    SYSTEM_ERROR(1002, "系统内部错误"),
    
    // 用户相关错误 1100-1199
    USER_NOT_FOUND(1100, "用户不存在"),
    USER_DISABLED(1101, "用户已被禁用"),
    EMAIL_ALREADY_REGISTERED(1102, "该邮箱已注册"),
    EMAIL_NOT_REGISTERED(1103, "该邮箱未注册"),
    
    // 认证相关错误 1200-1299
    INVALID_CREDENTIALS(1200, "账号或密码错误"),
    PASSWORD_ERROR(1201, "密码错误"),
    INVALID_LOGIN_TYPE(1202, "不支持的登录方式"),
    NOT_LOGGED_IN(1203, "未登录"),
    
    // 验证码相关错误 1300-1399
    VERIFICATION_CODE_ERROR(1300, "验证码错误或已过期"),
    VERIFICATION_CODE_EXPIRED(1301, "验证码已过期"),
    EMAIL_SEND_FAILED(1302, "邮件发送失败"),
    EMAIL_EMPTY(1303, "邮箱不能为空"),
    
    // 二维码登录相关错误 1400-1499
    QR_CODE_NOT_FOUND(1400, "二维码不存在"),
    QR_CODE_EXPIRED(1401, "二维码已过期"),
    
    // 应用相关错误 1500-1599
    APP_NOT_FOUND(1500, "应用不存在"),
    APP_DISABLED(1501, "应用已禁用"),
    
    // 微信相关错误 1600-1699
    WECHAT_LOGIN_FAILED(1600, "微信登录失败");

    private final int code;
    private final String message;

    UserErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}