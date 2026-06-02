package com.ruoyi.talk.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkTag;
import com.ruoyi.talk.service.ITalkTagService;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/ruoyi-system/talktag")
public class TalkTagController extends BaseController {
    @Autowired
    private ITalkTagService talkTagService;

    @PreAuthorize("@ss.hasPermi('talk:tag:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkTag talkTag) {
        startPage();
        List<TalkTag> list = talkTagService.selectTalkTagList(talkTag);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:tag:list')")
    @GetMapping("/active")
    public AjaxResult activeTags() {
        List<TalkTag> list = talkTagService.selectActiveTags();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:tag:list')")
    @GetMapping("/labels")
    public AjaxResult labels() {
        List<TalkTag> list = talkTagService.selectActiveTags();
        Map<String, String> labelMap = new LinkedHashMap<>();
        for (TalkTag tag : list) {
            labelMap.put(tag.getTagKey(), tag.getTagName());
        }
        return success(labelMap);
    }

    @PreAuthorize("@ss.hasPermi('talk:tag:query')")
    @GetMapping(value = "/{tagId:\\d+}")
    public AjaxResult getInfo(@PathVariable("tagId") Long tagId) {
        return success(talkTagService.selectTalkTagById(tagId));
    }

    @PreAuthorize("@ss.hasPermi('talk:tag:add')")
    @Log(title = "谈话标签管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody TalkTag talkTag) {
        return toAjax(talkTagService.insertTalkTag(talkTag));
    }

    @PreAuthorize("@ss.hasPermi('talk:tag:edit')")
    @Log(title = "谈话标签管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody TalkTag talkTag) {
        return toAjax(talkTagService.updateTalkTag(talkTag));
    }

    @PreAuthorize("@ss.hasPermi('talk:tag:remove')")
    @Log(title = "谈话标签管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagIds:[\\d,]+}")
    public AjaxResult remove(@PathVariable Long[] tagIds) {
        return toAjax(talkTagService.deleteTalkTagByIds(tagIds));
    }
}
