package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WritingLogDao extends BaseMapper<WritingLogEntity> {
}
