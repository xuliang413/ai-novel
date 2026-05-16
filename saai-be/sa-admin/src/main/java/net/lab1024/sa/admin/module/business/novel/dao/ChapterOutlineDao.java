package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterOutlineEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 章节细纲 DAO
 *
 * @Author AI-Novel
 */
@Mapper
public interface ChapterOutlineDao extends BaseMapper<ChapterOutlineEntity> {
}
