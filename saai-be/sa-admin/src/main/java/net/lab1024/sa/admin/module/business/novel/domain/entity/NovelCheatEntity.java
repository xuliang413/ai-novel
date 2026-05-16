package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说金手指 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_cheat")
public class NovelCheatEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 金手指名称, 如"万倍悟性""老爷爷戒指"
     */
    private String name;

    /**
     * 类型: ABILITY能力型/ITEM_BOUND物品绑定型/SPACE空间型/SYSTEM系统型
     */
    private String type;

    /**
     * 描述
     */
    private String summary;

    /**
     * 来源, 自然语言描述金手指怎么获得的
     */
    private String origin;

    /**
     * 限制/副作用, 自然语言描述使用代价
     */
    private String limitation;

    /**
     * 进化/升级路径, 自然语言描述
     */
    private String evolution;

    /**
     * 当前副作用阶段, 仅通过写作流程修改, 追踪能力代价的渐进演化(如"初期反噬·头痛"→"经脉逆行"→"丹田碎裂")
     */
    private String currentStage;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
