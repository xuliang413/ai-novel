package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterLocationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色当前位置 DAO。
 * <p>
 * 仅提供 MyBatis-Plus 基础能力, 业务写入统一放在 GraphPatch 流程里控制。
 *
 * @Author AI-Novel
 */
@Mapper
public interface NovelCharacterLocationDao extends BaseMapper<NovelCharacterLocationEntity> {
}
