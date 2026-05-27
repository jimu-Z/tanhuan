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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.conversation.domain.ConversationFollowUp;
import com.ruoyi.conversation.service.IConversationFollowUpService;

@RestController
@RequestMapping("/conversation/followup")
public class ConversationFollowUpController extends BaseController {
    @Autowired
    private IConversationFollowUpService followUpService;

    @PreAuthorize("@ss.hasPermi('conversation:record:query')")
    @GetMapping("/conversation/{conversationId}")
    public AjaxResult getByConversationId(@PathVariable Long conversationId) {
        List<ConversationFollowUp> list = followUpService.selectFollowUpByConversationId(conversationId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:edit')")
    @Log(title = "谈话跟进", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ConversationFollowUp followUp) {
        followUp.setCreateBy(getUsername());
        return toAjax(followUpService.insertFollowUp(followUp));
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:edit')")
    @Log(title = "谈话跟进", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ConversationFollowUp followUp) {
        followUp.setUpdateBy(getUsername());
        return toAjax(followUpService.updateFollowUp(followUp));
    }

    @PreAuthorize("@ss.hasPermi('conversation:record:edit')")
    @Log(title = "谈话跟进", businessType = BusinessType.DELETE)
    @DeleteMapping("/{followId}")
    public AjaxResult remove(@PathVariable Long followId) {
        return toAjax(followUpService.deleteFollowUpById(followId));
    }
}