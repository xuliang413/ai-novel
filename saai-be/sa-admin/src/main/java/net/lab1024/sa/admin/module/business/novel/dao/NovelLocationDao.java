package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说地点 DAO
 *
 * @Author AI-Novel
 */
@Mapper
public interface NovelLocationDao extends BaseMapper<NovelLocationEntity> {
}
