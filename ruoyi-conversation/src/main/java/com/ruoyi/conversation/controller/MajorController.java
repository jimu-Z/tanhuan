package com.ruoyi.conversation.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.conversation.domain.Major;
import com.ruoyi.conversation.service.IMajorService;

@RestController
@RequestMapping("/conversation/major")
public class MajorController extends BaseController {
    @Autowired
    private IMajorService majorService;

    @PreAuthorize("@ss.hasPermi('conversation:major:list')")
    @GetMapping("/list")
    public TableDataInfo list(Major major) {
        startPage();
        List<Major> list = majorService.selectMajorList(major);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:major:query')")
    @GetMapping("/{majorId}")
    public AjaxResult getInfo(@PathVariable Long majorId) {
        return success(majorService.selectMajorById(majorId));
    }

    @PreAuthorize("@ss.hasPermi('conversation:major:query')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree() {
        List<Major> list = majorService.selectMajorByDeptId(null);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:major:add')")
    @Log(title = "专业管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Major major) {
        major.setCreateBy(getUsername());
        return toAjax(majorService.insertMajor(major));
    }

    @PreAuthorize("@ss.hasPermi('conversation:major:edit')")
    @Log(title = "专业管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Major major) {
        major.setUpdateBy(getUsername());
        return toAjax(majorService.updateMajor(major));
    }

    @PreAuthorize("@ss.hasPermi('conversation:major:remove')")
    @Log(title = "专业管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{majorIds}")
    public AjaxResult remove(@PathVariable Long[] majorIds) {
        return toAjax(majorService.deleteMajorByIds(majorIds));
    }

    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<Major> list = majorService.selectMajorList(new Major());
        return success(list);
    }
}