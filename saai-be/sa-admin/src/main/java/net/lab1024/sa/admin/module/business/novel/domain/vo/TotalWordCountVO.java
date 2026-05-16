package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目总字数 VO —— 替换 totalWordCount 接口返回的裸 Map
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalWordCountVO {

    @Schema(description = "项目总字数")
    private Integer totalWords;
}
