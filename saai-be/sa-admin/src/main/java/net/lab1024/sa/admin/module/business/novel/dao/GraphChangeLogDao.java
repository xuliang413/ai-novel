package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.GraphChangeLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图谱变更日志 Dao。
 */
@Mapper
public interface GraphChangeLogDao extends BaseMapper<GraphChangeLogEntity> {
}
