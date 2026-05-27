package com.ruoyi.conversation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.conversation.domain.Conversation;
import com.ruoyi.conversation.service.IConversationService;
import com.ruoyi.conversation.service.IConversationFollowUpService;

@RestController
@RequestMapping("/conversation/record")
public class ConversationController extends BaseController {
    @Autowired
    private IConversationService conversationService;

    @Autowired
    private IConversationFollowUpService followUpService;

    @PreAuthorize("@ss.hasPermi('conversation:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(Conversation conversation) {
        startPage();
        List<Conversation> list = conversationService.selectConversationList(conversation);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:query')")
    @GetMapping("/{conversationId}")
    public AjaxResult getInfo(@PathVariable Long conversationId) {
        AjaxResult ajax = AjaxResult.success();
        Conversation conversation = conversationService.selectConversationById(conversationId);
        ajax.put(AjaxResult.DATA_TAG, conversation);
        ajax.put("followUps", followUpService.selectFollowUpByConversationId(conversationId));
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:add')")
    @Log(title = "谈话记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Map<String, Object> params) {
        Conversation conversation = parseConversation(params);
        conversation.setCreateBy(getUsername());
        Long[] studentIds = parseStudentIds(params);
        return toAjax(conversationService.insertConversation(conversation, studentIds));
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:edit')")
    @Log(title = "谈话记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Conversation conversation) {
        conversation.setUpdateBy(getUsername());
        return toAjax(conversationService.updateConversation(conversation));
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:remove')")
    @Log(title = "谈话记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{conversationIds}")
    public AjaxResult remove(@PathVariable Long[] conversationIds) {
        return toAjax(conversationService.deleteConversationByIds(conversationIds));
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:query')")
    @GetMapping("/timeline/{studentNo}")
    public AjaxResult timeline(@PathVariable String studentNo) {
        List<Conversation> list = conversationService.selectConversationTimeLine(studentNo);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:my')")
    @GetMapping("/my")
    public TableDataInfo my(Conversation conversation) {
        conversation.setCreateBy(getUsername());
        startPage();
        List<Conversation> list = conversationService.selectMyConversationList(conversation);
        return getDataTable(list);
    }

    private Conversation parseConversation(Map<String, Object> params) {
        return null;
    }

    private Long[] parseStudentIds(Map<String, Object> params) {
        Object studentIdsObj = params.get("studentIds");
        if (studentIdsObj instanceof List) {
            List<?> list = (List<?>) studentIdsObj;
            return list.stream()
                    .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
                    .toArray(Long[]::new);
        }
        return new Long[0];
    }
}