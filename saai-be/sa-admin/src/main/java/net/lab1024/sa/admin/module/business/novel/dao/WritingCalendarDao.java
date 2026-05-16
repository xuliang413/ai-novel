package net.lab1024.sa.admin.module.business.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingCalendarEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 写作打卡日历 DAO。
 */
@Mapper
public interface WritingCalendarDao extends BaseMapper<WritingCalendarEntity> {
}
