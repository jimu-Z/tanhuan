package com.ruoyi.talk.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkTemplate;
import com.ruoyi.talk.service.ITalkTemplateService;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/ruoyi-system/talktemplate")
public class TalkTemplateController extends BaseController
{
    @Autowired
    private ITalkTemplateService talkTemplateService;

    @PreAuthorize("@ss.hasPermi('talk:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkTemplate talkTemplate)
    {
        startPage();
        List<TalkTemplate> list = talkTemplateService.selectTalkTemplateList(talkTemplate);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:template:list')")
    @GetMapping("/system")
    public AjaxResult systemTemplates()
    {
        List<TalkTemplate> list = talkTemplateService.selectSystemTemplates();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:template:query')")
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId)
    {
        return success(talkTemplateService.selectTalkTemplateById(templateId));
    }

    @PreAuthorize("@ss.hasPermi('talk:template:add')")
    @Log(title = "谈话模板管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody TalkTemplate talkTemplate)
    {
        return toAjax(talkTemplateService.insertTalkTemplate(talkTemplate));
    }

    @PreAuthorize("@ss.hasPermi('talk:template:edit')")
    @Log(title = "谈话模板管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody TalkTemplate talkTemplate)
    {
        return toAjax(talkTemplateService.updateTalkTemplate(talkTemplate));
    }

    @PreAuthorize("@ss.hasPermi('talk:template:remove')")
    @Log(title = "谈话模板管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds)
    {
        return toAjax(talkTemplateService.deleteTalkTemplateByIds(templateIds));
    }
}
