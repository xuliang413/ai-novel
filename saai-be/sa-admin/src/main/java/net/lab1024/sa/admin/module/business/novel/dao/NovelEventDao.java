package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说事件 DAO
 *
 * @Author AI-Novel
 */
@Mapper
public interface NovelEventDao extends BaseMapper<NovelEventEntity> {
}
