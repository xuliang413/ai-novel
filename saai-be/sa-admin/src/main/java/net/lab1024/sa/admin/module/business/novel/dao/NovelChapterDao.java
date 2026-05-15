package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说章节 DAO。
 */
@Mapper
public interface NovelChapterDao extends BaseMapper<NovelChapterEntity> {
}
