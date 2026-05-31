package com.ruoyi.talk.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.service.ITalkStudentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 学生信息管理Controller
 * 
 * @author admin
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/ruoyi-system/talk")
public class TalkStudentController extends BaseController {
    @Autowired
    private ITalkStudentService talkStudentService;

    /**
     * 查询学生信息管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkStudent talkStudent) {
        startPage();
        List<TalkStudent> list = talkStudentService.selectTalkStudentList(talkStudent);
        return getDataTable(list);
    }

    /**
     * 导出学生信息管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:student:export')")
    @Log(title = "学生信息管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TalkStudent talkStudent) {
        List<TalkStudent> list = talkStudentService.selectTalkStudentList(talkStudent);
        ExcelUtil<TalkStudent> util = new ExcelUtil<TalkStudent>(TalkStudent.class);
        util.exportExcel(response, list, "学生信息管理数据");
    }

    /**
     * 获取学生详情（含历史谈话记录）
     */
    @PreAuthorize("@ss.hasPermi('talk:student:query')")
    @GetMapping("/detail/{studentId}")
    public AjaxResult getDetail(@PathVariable("studentId") Long studentId) {
        return success(talkStudentService.getStudentDetail(studentId));
    }

    /**
     * 获取学生信息管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('talk:student:query')")
    @GetMapping(value = "/{studentId}")
    public AjaxResult getInfo(@PathVariable("studentId") Long studentId) {
        return success(talkStudentService.selectTalkStudentByStudentId(studentId));
    }

    /**
     * 新增学生信息管理
     */
    @PreAuthorize("@ss.hasPermi('talk:student:add')")
    @Log(title = "学生信息管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TalkStudent talkStudent) {
        return toAjax(talkStudentService.insertTalkStudent(talkStudent));
    }

    /**
     * 修改学生信息管理
     */
    @PreAuthorize("@ss.hasPermi('talk:student:edit')")
    @Log(title = "学生信息管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TalkStudent talkStudent) {
        return toAjax(talkStudentService.updateTalkStudent(talkStudent));
    }

    /**
     * 删除学生信息管理
     */
    @PreAuthorize("@ss.hasPermi('talk:student:remove')")
    @Log(title = "学生信息管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds) {
        return toAjax(talkStudentService.deleteTalkStudentByStudentIds(studentIds));
    }

    /**
     * 导入预览
     */
    @PreAuthorize("@ss.hasPermi('talk:student:import')")
    @Log(title = "学生信息管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import/preview")
    public AjaxResult importPreview(@RequestParam("file") MultipartFile file) {
        return success(talkStudentService.importPreview(file));
    }

    /**
     * 执行导入
     */
    @PreAuthorize("@ss.hasPermi('talk:student:import')")
    @Log(title = "学生信息管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import/execute")
    public AjaxResult importExecute(@RequestBody List<Map<String, Object>> confirmedRows) {
        return success(talkStudentService.importExecute(confirmedRows));
    }
}
