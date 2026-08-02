package com.learnfl.dto.manage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 新增词库组 */
@Data
public class GroupCreateRequest {

    @NotNull(message = "语言不能为空")
    private Long languageId;

    @NotBlank(message = "词库组名称不能为空")
    private String name;

    private String description;
}
