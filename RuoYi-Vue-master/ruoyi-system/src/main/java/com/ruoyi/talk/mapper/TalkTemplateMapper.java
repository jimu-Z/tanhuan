package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkTemplate;

public interface TalkTemplateMapper
{
    public List<TalkTemplate> selectTalkTemplateList(TalkTemplate template);

    public TalkTemplate selectTalkTemplateById(Long templateId);

    public int insertTalkTemplate(TalkTemplate template);

    public int updateTalkTemplate(TalkTemplate template);

    public int deleteTalkTemplateById(Long templateId);

    public List<TalkTemplate> selectSystemTemplates();
}
