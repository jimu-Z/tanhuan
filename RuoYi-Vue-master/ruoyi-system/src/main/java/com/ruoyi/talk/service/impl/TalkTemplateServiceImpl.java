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

@Service
public class TalkTemplateServiceImpl implements ITalkTemplateService
{
    @Autowired
    private TalkTemplateMapper talkTemplateMapper;

    @Override
    public List<TalkTemplate> selectTalkTemplateList(TalkTemplate template)
    {
        return talkTemplateMapper.selectTalkTemplateList(template);
    }

    @Override
    public TalkTemplate selectTalkTemplateById(Long templateId)
    {
        return talkTemplateMapper.selectTalkTemplateById(templateId);
    }

    @Override
    public int insertTalkTemplate(TalkTemplate template)
    {
        template.setCreateBy(SecurityUtils.getUsername());
        template.setCreateTime(DateUtils.getNowDate());
        template.setTemplateTags(normalizeTags(template.getTemplateTags()));
        return talkTemplateMapper.insertTalkTemplate(template);
    }

    @Override
    public int updateTalkTemplate(TalkTemplate template)
    {
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        template.setTemplateTags(normalizeTags(template.getTemplateTags()));
        return talkTemplateMapper.updateTalkTemplate(template);
    }

    @Override
    public int deleteTalkTemplateById(Long templateId)
    {
        return talkTemplateMapper.deleteTalkTemplateById(templateId);
    }

    @Override
    @Transactional
    public int deleteTalkTemplateByIds(Long[] templateIds)
    {
        int result = 0;
        for (Long templateId : templateIds)
        {
            result += talkTemplateMapper.deleteTalkTemplateById(templateId);
        }
        return result;
    }

    @Override
    public List<TalkTemplate> selectSystemTemplates()
    {
        return talkTemplateMapper.selectSystemTemplates();
    }

    private String normalizeTags(String tags) {
        if (StringUtils.isEmpty(tags)) {
            return tags;
        }
        String trimmed = tags.trim();
        if (trimmed.startsWith("[")) {
            return trimmed;
        }
        String jsonArray = Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(",", "[", "]"));
        return jsonArray;
    }
}
