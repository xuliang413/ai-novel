package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户API Key DAO
 *
 * @Author AI-Novel
 */
@Mapper
public interface UserApiKeyDao extends BaseMapper<UserApiKeyEntity> {
}
