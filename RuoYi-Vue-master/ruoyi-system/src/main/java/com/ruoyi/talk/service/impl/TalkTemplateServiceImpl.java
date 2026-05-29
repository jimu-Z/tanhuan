package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        return talkTemplateMapper.insertTalkTemplate(template);
    }

    @Override
    public int updateTalkTemplate(TalkTemplate template)
    {
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        return talkTemplateMapper.updateTalkTemplate(template);
    }

    @Override
    public int deleteTalkTemplateById(Long templateId)
    {
        return talkTemplateMapper.deleteTalkTemplateById(templateId);
    }

    @Override
    public List<TalkTemplate> selectSystemTemplates()
    {
        return talkTemplateMapper.selectSystemTemplates();
    }
}
