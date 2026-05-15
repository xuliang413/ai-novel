package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterGenerationSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 章节生成会话 DAO。
 */
@Mapper
public interface ChapterGenerationSessionDao extends BaseMapper<ChapterGenerationSessionEntity> {
}
