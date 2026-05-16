package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小说项目 DAO
 *
 * @Author AI-Novel
 */
@Mapper
public interface NovelProjectDao extends BaseMapper<NovelProjectEntity> {
}
