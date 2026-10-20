package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.service.impl.TalkSessionServiceImpl;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TalkOrganizationCycleTest {
    @Test void cyclicDepartmentChainStopsWithBusinessReason() {
        TalkSessionServiceImpl service=new TalkSessionServiceImpl();SysDeptMapper depts=mock(SysDeptMapper.class);ReflectionTestUtils.setField(service,"sysDeptMapper",depts);
        AtomicInteger queries=new AtomicInteger();when(depts.selectDeptById(anyLong())).thenAnswer(i->{if(queries.incrementAndGet()>40)throw new IllegalStateException("循环组织链查询超过40次");SysDept d=new SysDept();Long id=i.getArgument(0);d.setDeptId(id);d.setParentId(id==100L?101L:100L);return d;});
        assertThatThrownBy(()->ReflectionTestUtils.invokeMethod(service,"resolveCollegeDeptId",100L)).isInstanceOf(ServiceException.class).hasMessageContaining("有效学院");assertThat(queries.get()).isLessThanOrEqualTo(2);
    }
    @Test void excessivelyDeepDepartmentChainStopsBeforeUnboundedQueries() {
        TalkSessionServiceImpl service=new TalkSessionServiceImpl();SysDeptMapper depts=mock(SysDeptMapper.class);ReflectionTestUtils.setField(service,"sysDeptMapper",depts);
        AtomicInteger queries=new AtomicInteger();when(depts.selectDeptById(anyLong())).thenAnswer(i->{if(queries.incrementAndGet()>40)throw new IllegalStateException("组织链查询超过40次");Long id=i.getArgument(0);SysDept d=new SysDept();d.setDeptId(id);d.setParentId(id+1);return d;});
        assertThatThrownBy(()->ReflectionTestUtils.invokeMethod(service,"resolveCollegeDeptId",100L)).isInstanceOf(ServiceException.class).hasMessageContaining("有效学院");assertThat(queries.get()).isLessThanOrEqualTo(32);
    }
}
