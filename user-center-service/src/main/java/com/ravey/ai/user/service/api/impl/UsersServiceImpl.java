package com.ravey.ai.user.service.api.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.model.req.*;
import com.ravey.ai.user.api.model.res.LoginRes;
import com.ravey.ai.user.api.service.UsersService;
import com.ravey.ai.user.api.utils.JwtUtils;
import com.ravey.ai.user.service.cache.CacheService;
import com.ravey.ai.user.service.component.MailService;
import com.ravey.ai.user.service.component.VerificationCodeService;
import com.ravey.ai.user.service.converter.UsersConverter;
import com.ravey.ai.user.service.dao.entity.UserSessions;
import com.ravey.ai.user.service.dao.entity.Users;
import com.ravey.ai.user.service.dao.mapper.UserSessionsMapper;
import com.ravey.ai.user.service.dao.mapper.UsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author Ravey
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersMapper usersMapper;
    private final UserSessionsMapper userSessionsMapper;
    private final MailService mailService;
    private final VerificationCodeService verificationCodeService;
    private final JwtUtils jwtUtils;
    private final CacheService cacheService;

    @Override
    public UsersDTO getById(Long userId) {
        log.debug("根据ID获取用户信息: userId={}", userId);

        if (userId == null) {
            log.warn("用户ID不能为空");
            return null;
        }

        try {
            // 通过Mapper查询用户实体
            Users user = usersMapper.selectById(userId);

            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return null;
            }

            // 转换为DTO并返回
            UsersDTO userDTO = UsersConverter.toDTO(user);
            log.debug("成功获取用户信息: userId={}, nickname={}", userId, userDTO.getNickname());

            return userDTO;

        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}", userId, e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public UsersDTO update(Long userId, UserUpdateReq req) {
        log.info("更新用户信息: userId={}, req={}", userId, req);

        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean needUpdate = false;
        if (StringUtils.hasText(req.getNickname()) && !req.getNickname().equals(user.getNickname())) {
            user.setNickname(req.getNickname());
            needUpdate = true;
        }

        if (StringUtils.hasText(req.getAvatarUrl()) && !req.getAvatarUrl().equals(user.getAvatarUrl())) {
            user.setAvatarUrl(req.getAvatarUrl());
            needUpdate = true;
        }

        if (needUpdate) {
            usersMapper.updateById(user);
            log.info("用户信息更新成功: userId={}", userId);
        }

        return UsersConverter.toDTO(user);
    }

    @Override
    public void sendEmailCode(EmailSendCodeReq req) {
        String email = req.getEmail();
        Integer scene = req.getScene();
        log.info("发送邮箱验证码: email={}, scene={}", email, scene);

        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("邮箱不能为空");
        }

        // 生成并发送验证码
        String code = verificationCodeService.generateCode(email, scene);
        String subject = "【小杏仁】验证码";
        String content = "您的验证码为：" + code + "，有效期5分钟，请勿泄露给他人。";
        mailService.sendSimpleMail(email, subject, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRes registerByEmail(EmailRegisterReq req) {
        log.info("邮箱注册开始: email={}", req.getEmail());

        // 1. 校验验证码 (scene=1)
        if (!verificationCodeService.verifyCode(req.getEmail(), 1, req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 2. 检查邮箱是否已存在
        Users existingUser = usersMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getEmail, req.getEmail()));
        if (existingUser != null) {
            throw new RuntimeException("该邮箱已注册");
        }

        // 3. 创建用户
        Users user = new Users();
        user.setEmail(req.getEmail());
        user.setPassword(hashPassword(req.getPassword()));
        user.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname() : "新用户");
        user.setStatus(1);
        usersMapper.insert(user);

        // 4. 生成登录响应
        return buildLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRes loginByEmail(EmailLoginReq req) {
        log.info("邮箱登录开始: email={}, type={}", req.getEmail(), req.getLoginType());

        Users user = usersMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getEmail, req.getEmail()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (req.getLoginType() == 1) {
            // 密码登录
            if (!hashPassword(req.getPassword()).equals(user.getPassword())) {
                throw new RuntimeException("密码错误");
            }
        } else if (req.getLoginType() == 2) {
            // 验证码登录 (scene=2)
            if (!verificationCodeService.verifyCode(req.getEmail(), 2, req.getCode())) {
                throw new RuntimeException("验证码错误或已过期");
            }
        } else {
            throw new RuntimeException("不支持的登录方式");
        }

        return buildLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(PasswordResetReq req) {
        log.info("重置密码开始: email={}", req.getEmail());

        // 校验验证码 (scene=3)
        if (!verificationCodeService.verifyCode(req.getEmail(), 3, req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        Users user = usersMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getEmail, req.getEmail()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(hashPassword(req.getNewPassword()));
        usersMapper.updateById(user);
    }

    private String hashPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    private LoginRes buildLoginResponse(Users user) {
        // 生成Token (这里假设 appId 为 'almond-web'，因为邮箱登录通常在Web端)
        String token = jwtUtils.generateToken(user.getId(), "almond-web");

        // 创建Session记录
        UserSessions session = new UserSessions();
        session.setUserId(user.getId());
        session.setSessionToken(token);
        session.setExpireTime(LocalDateTime.now().plusDays(7));
        userSessionsMapper.insert(session);

        // 写入缓存
        cacheService.cacheUserSession(token, user.getId());
        cacheService.cacheUserInfo(UsersConverter.toDTO(user));

        // 构建响应
        LoginRes res = new LoginRes();
        res.setToken(token);
        res.setExpiresIn(7 * 24 * 3600L);
        LoginRes.UserInfo userInfo = new LoginRes.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setEmail(user.getEmail());
        res.setUserInfo(userInfo);
        return res;
    }
}