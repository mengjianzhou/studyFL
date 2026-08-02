package com.learnfl.dto.manage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 新增词库 */
@Data
public class BankCreateRequest {

    @NotNull(message = "词库组不能为空")
    private Long groupId;

    @NotBlank(message = "词库名称不能为空")
    private String name;

    private String description;
}
