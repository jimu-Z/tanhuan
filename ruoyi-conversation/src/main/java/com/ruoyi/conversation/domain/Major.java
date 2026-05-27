package com.ruoyi.conversation.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 专业表 biz_major
 *
 * @author ruoyi
 */
public class Major extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 专业ID */
    private Long majorId;

    /** 专业名称 */
    @Excel(name = "专业名称")
    private String majorName;

    /** 专业代码 */
    @Excel(name = "专业代码")
    private String majorCode;

    /** 所属学院ID */
    private Long deptId;

    /** 状态 */
    private String status;

    public Major() {}

    public Major(Long majorId) {
        this.majorId = majorId;
    }

    public Long getMajorId() { return majorId; }
    public void setMajorId(Long majorId) { this.majorId = majorId; }

    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }

    public String getMajorCode() { return majorCode; }
    public void setMajorCode(String majorCode) { this.majorCode = majorCode; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("majorId", getMajorId())
            .append("majorName", getMajorName())
            .append("majorCode", getMajorCode())
            .append("deptId", getDeptId())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}