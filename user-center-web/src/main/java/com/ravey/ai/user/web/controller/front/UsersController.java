package com.ravey.ai.user.web.controller.front;

import com.ravey.ai.user.api.dto.UsersDTO;
import com.ravey.ai.user.api.model.req.UserUpdateReq;
import com.ravey.ai.user.api.service.UsersService;
import com.ravey.ai.user.service.context.UserContext;
import com.ravey.common.object.store.template.ObjectStoreTemplate;
import com.ravey.common.service.web.result.HttpResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 用户信息控制器
 *
 * @author ravey
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/front/users")
@Tag(name = "用户管理", description = "用户信息相关接口")
public class UsersController {

    @Resource
    private UsersService usersService;

    @Resource
    private ObjectStoreTemplate objectStoreTemplate;

    /**
     * OSS访问域名，配置在nacos common.yml中
     */
    @Value("${object.store.aliyun.access-domain:}")
    private String ossAccessDomain;

    /**
     * 头像上传路径前缀
     */
    private static final String AVATAR_PATH_PREFIX = "user-center/avatars/";

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public HttpResult<UsersDTO> getCurrentUser() {
        UsersDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("未登录");
        }
        // 从数据库获取最新信息
        return HttpResult.success(usersService.getById(currentUser.getId()));
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新当前用户的昵称和头像")
    public HttpResult<UsersDTO> updateUserInfo(@RequestBody UserUpdateReq req) {
        UsersDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("未登录");
        }
        return HttpResult.success(usersService.update(currentUser.getId(), req));
    }

    @PostMapping("/avatar/upload")
    @Operation(summary = "上传头像", description = "上传用户头像图片到OSS")
    public HttpResult<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 生成文件名：路径前缀/日期/UUID.扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            
            // 按日期分目录存储，便于管理
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = AVATAR_PATH_PREFIX + datePath + "/" + UUID.randomUUID().toString() + extension;
            
            // 上传到OSS
            objectStoreTemplate.uploadFile(fileName, file.getBytes());
            log.info("头像上传成功: {}", fileName);
            
            // 返回完整的访问URL
            String accessUrl = ossAccessDomain + "/" + fileName;
            return HttpResult.success(accessUrl);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}
