package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说物品 DAO
 *
 * @Author AI-Novel
 */
@Mapper
public interface NovelItemDao extends BaseMapper<NovelItemEntity> {
}
