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
import com.ruoyi.conversation.domain.ClassInfo;
import com.ruoyi.conversation.service.IClassInfoService;

@RestController
@RequestMapping("/conversation/class")
public class ClassInfoController extends BaseController {
    @Autowired
    private IClassInfoService classInfoService;

    @PreAuthorize("@ss.hasPermi('conversation:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassInfo classInfo) {
        startPage();
        List<ClassInfo> list = classInfoService.selectClassList(classInfo);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:class:query')")
    @GetMapping("/{classId}")
    public AjaxResult getInfo(@PathVariable Long classId) {
        return success(classInfoService.selectClassById(classId));
    }

    @PreAuthorize("@ss.hasPermi('conversation:class:query')")
    @GetMapping("/majorTree")
    public AjaxResult majorTree() {
        List<ClassInfo> list = classInfoService.selectClassList(new ClassInfo());
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:class:add')")
    @Log(title = "班级管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ClassInfo classInfo) {
        classInfo.setCreateBy(getUsername());
        return toAjax(classInfoService.insertClassInfo(classInfo));
    }

    @PreAuthorize("@ss.hasPermi('conversation:class:edit')")
    @Log(title = "班级管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ClassInfo classInfo) {
        classInfo.setUpdateBy(getUsername());
        return toAjax(classInfoService.updateClassInfo(classInfo));
    }

    @PreAuthorize("@ss.hasPermi('conversation:class:remove')")
    @Log(title = "班级管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{classIds}")
    public AjaxResult remove(@PathVariable Long[] classIds) {
        return toAjax(classInfoService.deleteClassInfoByIds(classIds));
    }

    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<ClassInfo> list = classInfoService.selectClassList(new ClassInfo());
        return success(list);
    }
}