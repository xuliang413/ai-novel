package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说线索 DAO。
 */
@Mapper
public interface NovelClueDao extends BaseMapper<NovelClueEntity> {
}
