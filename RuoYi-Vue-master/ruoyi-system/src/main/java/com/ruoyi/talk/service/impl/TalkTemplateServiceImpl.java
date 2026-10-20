package com.ruoyi.talk.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.talk.domain.TalkTemplate;
import com.ruoyi.talk.mapper.TalkTemplateMapper;
import com.ruoyi.talk.service.ITalkTemplateService;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.common.exception.ServiceException;

@Service
public class TalkTemplateServiceImpl implements ITalkTemplateService
{
    private static final com.fasterxml.jackson.databind.ObjectMapper TAG_JSON = new com.fasterxml.jackson.databind.ObjectMapper();
    @Autowired
    private TalkTemplateMapper talkTemplateMapper;

    @Override
    public List<TalkTemplate> selectTalkTemplateList(TalkTemplate template)
    {
        TalkPermissionUtils.applyCollegeScope(template);
        return talkTemplateMapper.selectTalkTemplateList(template);
    }

    @Override
    public TalkTemplate selectTalkTemplateById(Long templateId)
    {
        TalkTemplate template = talkTemplateMapper.selectTalkTemplateById(templateId);
        requireRead(template);
        return template;
    }

    @Override
    public int insertTalkTemplate(TalkTemplate template)
    {
        prepareNewScope(template);
        template.setCreateBy(SecurityUtils.getUsername());
        template.setCreateTime(DateUtils.getNowDate());
        template.setTemplateTags(normalizeTags(template.getTemplateTags()));
        return talkTemplateMapper.insertTalkTemplate(template);
    }

    @Override
    public int updateTalkTemplate(TalkTemplate template)
    {
        TalkTemplate existing = requireExisting(template.getTemplateId());
        requireWrite(existing);
        template.setTemplateType(null);
        template.setCollegeDeptId(null);
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        template.setTemplateTags(normalizeTags(template.getTemplateTags()));
        return talkTemplateMapper.updateTalkTemplate(template);
    }

    @Override
    public int deleteTalkTemplateById(Long templateId)
    {
        requireWrite(requireExisting(templateId));
        return talkTemplateMapper.deleteTalkTemplateById(templateId);
    }

    @Override
    @Transactional
    public int deleteTalkTemplateByIds(Long[] templateIds)
    {
        int result = 0;
        for (Long templateId : templateIds)
        {
            requireWrite(requireExisting(templateId));
            result += talkTemplateMapper.deleteTalkTemplateById(templateId);
        }
        return result;
    }

    @Override
    public List<TalkTemplate> selectSystemTemplates()
    {
        return talkTemplateMapper.selectSystemTemplates();
    }

    private void prepareNewScope(TalkTemplate template)
    {
        if (TalkPermissionUtils.isGlobalAdmin())
        {
            if (template.getCollegeDeptId() == null)
            {
                template.setTemplateType("system");
            }
            else
            {
                template.setTemplateType("personal");
            }
            return;
        }
        TalkPermissionUtils.requireCollegeAccess(TalkPermissionUtils.currentCollegeId());
        template.setTemplateType("personal");
        template.setCollegeDeptId(TalkPermissionUtils.currentCollegeId());
    }

    private TalkTemplate requireExisting(Long templateId)
    {
        TalkTemplate template = talkTemplateMapper.selectTalkTemplateById(templateId);
        if (template == null)
        {
            throw new ServiceException("模板不存在");
        }
        return template;
    }

    private void requireRead(TalkTemplate template)
    {
        if (template == null || "system".equals(template.getTemplateType()) || TalkPermissionUtils.isGlobalAdmin())
        {
            return;
        }
        TalkPermissionUtils.requireCollegeAccess(template.getCollegeDeptId());
    }

    private void requireWrite(TalkTemplate template)
    {
        if ("system".equals(template.getTemplateType()))
        {
            if (!TalkPermissionUtils.canManageSystemDefinitions())
            {
                throw new ServiceException("系统内置模板仅限全校管理员维护");
            }
            return;
        }
        TalkPermissionUtils.requireCollegeAccess(template.getCollegeDeptId());
    }

    private String normalizeTags(String tags) {
        if (StringUtils.isEmpty(tags)) {
            return tags;
        }
        String trimmed = tags.trim();
        try {
            if (trimmed.startsWith("[")) {
                com.fasterxml.jackson.databind.JsonNode array = TAG_JSON.readTree(trimmed);
                if (!array.isArray()) throw new IllegalArgumentException();
                for (com.fasterxml.jackson.databind.JsonNode item : array) {
                    if (!item.isTextual()) throw new IllegalArgumentException();
                }
                // Reject trailing input as well as malformed array elements.
                TAG_JSON.reader().with(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(trimmed);
                return TAG_JSON.writeValueAsString(array);
            }
            return TAG_JSON.writeValueAsString(Arrays.stream(trimmed.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
        } catch (java.io.IOException | IllegalArgumentException e) {
            throw new ServiceException("模板标签格式错误，请使用字符串数组或逗号分隔的标签");
        }
    }
}
