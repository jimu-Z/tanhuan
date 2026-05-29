package com.ruoyi.talk.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

public class TalkTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long templateId;

    private String templateName;

    private String templateContent;

    private String templateType;

    private String templateTags;

    private String delFlag;

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public String getTemplateContent()
    {
        return templateContent;
    }

    public void setTemplateContent(String templateContent)
    {
        this.templateContent = templateContent;
    }

    public String getTemplateType()
    {
        return templateType;
    }

    public void setTemplateType(String templateType)
    {
        this.templateType = templateType;
    }

    public String getTemplateTags()
    {
        return templateTags;
    }

    public void setTemplateTags(String templateTags)
    {
        this.templateTags = templateTags;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("templateId", getTemplateId())
            .append("templateName", getTemplateName())
            .append("templateContent", getTemplateContent())
            .append("templateType", getTemplateType())
            .append("templateTags", getTemplateTags())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
