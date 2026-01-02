package com.ravey.ai.user.service.component;

import com.ravey.ai.user.api.enums.UserErrorCode;
import com.ravey.common.api.model.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 邮件发送组件
 *
 * @author Ravey
 */
@Slf4j
@Component
public class MailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单文本邮件
     *
     * @param to      接收者
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
            log.info("邮件已成功发送至: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败至: {}, error: {}", to, e.getMessage());
            throw new ServiceException(UserErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
