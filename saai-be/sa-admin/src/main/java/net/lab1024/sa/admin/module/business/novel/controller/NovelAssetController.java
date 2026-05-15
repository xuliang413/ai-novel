package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationAddForm;
import net.lab1024.sa.admin.module.business.novel.service.NovelAssetService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小说设定接口——角色、地点、线索的录入和查询。
 */
@RestController
@RequestMapping("/novel")
@Tag(name = "AI 小说 - 基础设定")
public class NovelAssetController {

    @Resource
    private NovelAssetService novelAssetService;

    /**
     * 新增角色，并同步建立 Project -> Character 图关系。
     */
    @Operation(summary = "新增小说角色")
    @PostMapping("/character/add")
    public ResponseDTO<Long> addCharacter(@RequestBody @Valid NovelCharacterAddForm addForm) {
        return novelAssetService.addCharacter(addForm);
    }

    /**
     * 新增地点，并同步建立 Project -> Location 图关系。
     */
    @Operation(summary = "新增小说地点")
    @PostMapping("/location/add")
    public ResponseDTO<Long> addLocation(@RequestBody @Valid NovelLocationAddForm addForm) {
        return novelAssetService.addLocation(addForm);
    }

    /**
     * 新增线索，并同步建立 Project -> Clue 图关系。
     */
    @Operation(summary = "新增小说线索")
    @PostMapping("/clue/add")
    public ResponseDTO<Long> addClue(@RequestBody @Valid NovelClueAddForm addForm) {
        return novelAssetService.addClue(addForm);
    }
}
