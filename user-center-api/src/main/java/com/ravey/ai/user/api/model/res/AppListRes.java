package com.ravey.ai.user.api.model.res;

import lombok.Data;

import java.util.List;

/**
 * 应用列表响应DTO
 *
 * @author ravey
 * @since 1.0.0
 */
@Data
public class AppListRes {

    /**
     * 应用列表
     */
    private List<AppRes> list;

    /**
     * 总数
     */
    private Long total;
}