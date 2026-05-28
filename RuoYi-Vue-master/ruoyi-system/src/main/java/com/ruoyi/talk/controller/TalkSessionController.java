package com.ruoyi.talk.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.service.ITalkSessionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 谈话会话管理Controller
 * 
 * @author admin
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/ruoyi-system/talksession")
public class TalkSessionController extends BaseController {
    @Autowired
    private ITalkSessionService talkSessionService;

    /**
     * 查询谈话会话管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:session:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkSession talkSession) {
        startPage();
        List<TalkSession> list = talkSessionService.selectTalkSessionList(talkSession);
        return getDataTable(list);
    }

    /**
     * 导出谈话会话管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TalkSession talkSession) {
        List<TalkSession> list = talkSessionService.selectTalkSessionList(talkSession);
        ExcelUtil<TalkSession> util = new ExcelUtil<TalkSession>(TalkSession.class);
        util.exportExcel(response, list, "谈话会话管理数据");
    }

    /**
     * 获取谈话会话管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId) {
        return success(talkSessionService.selectTalkSessionBySessionId(sessionId));
    }

    /**
     * 新增谈话会话管理
     */
    @PreAuthorize("@ss.hasPermi('talk:session:add')")
    @Log(title = "谈话会话管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TalkSession talkSession) {
        return toAjax(talkSessionService.insertTalkSession(talkSession));
    }

    /**
     * 修改谈话会话管理
     */
    @PreAuthorize("@ss.hasPermi('talk:session:edit')")
    @Log(title = "谈话会话管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TalkSession talkSession) {
        return toAjax(talkSessionService.updateTalkSession(talkSession));
    }

    /**
     * 删除谈话会话管理
     */
    @PreAuthorize("@ss.hasPermi('talk:session:remove')")
    @Log(title = "谈话会话管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds) {
        return toAjax(talkSessionService.deleteTalkSessionBySessionIds(sessionIds));
    }
}
