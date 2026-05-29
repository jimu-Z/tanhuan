package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkTemplate;

public interface ITalkTemplateService
{
    public List<TalkTemplate> selectTalkTemplateList(TalkTemplate template);

    public TalkTemplate selectTalkTemplateById(Long templateId);

    public int insertTalkTemplate(TalkTemplate template);

    public int updateTalkTemplate(TalkTemplate template);

    public int deleteTalkTemplateById(Long templateId);

    public List<TalkTemplate> selectSystemTemplates();
}
