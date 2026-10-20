package com.ruoyi.system.service;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.domain.entity.SysDept;
import static org.assertj.core.api.Assertions.assertThat;

class SysDeptMapperCollegeGuardProjectionTest {
    @Test void selectDeptByIdExposesDeletionFlagConsumedByCollegeGuard() throws Exception {
        Configuration config=new Configuration();
        config.getTypeAliasRegistry().registerAlias("SysDept",SysDept.class);
        String resource="mapper/system/SysDeptMapper.xml";
        try(InputStream in=Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(in,config,resource,config.getSqlFragments()).parse();
        }
        String statement="com.ruoyi.system.mapper.SysDeptMapper.selectDeptById";
        String sql=config.getMappedStatement(statement).getBoundSql(910001L).getSql();
        assertThat(sql).contains("d.dept_type").contains("d.del_flag");
        assertThat(config.getResultMap("com.ruoyi.system.mapper.SysDeptMapper.SysDeptResult").getMappedColumns()).contains("DEL_FLAG");
    }
}
