package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.talk.controller.TalkStudentController;
import java.util.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class TalkDepartmentTreeTest {
    @ParameterizedTest @ValueSource(strings={"学生所属部门未归属学院","无权访问其他学院"})
    void invalidOrUnauthorizedNodeIsExcludedWithoutBreakingValidTree(String reason) {
        TalkStudentController controller=new TalkStudentController();ISysDeptService departments=mock(ISysDeptService.class);ITalkStudentService students=mock(ITalkStudentService.class);ReflectionTestUtils.setField(controller,"deptService",departments);ReflectionTestUtils.setField(controller,"talkStudentService",students);
        SysDept college=new SysDept();college.setDeptId(100L);college.setParentId(0L);college.setDeptType("college");SysDept bad=new SysDept();bad.setDeptId(200L);bad.setParentId(100L);bad.setDeptType("class");when(departments.selectDeptList(any())).thenReturn(List.of(college,bad));when(students.countStudentsByDeptId(100L)).thenReturn(2);when(students.countStudentsByDeptId(200L)).thenThrow(new ServiceException(reason));
        List<Map<String,Object>> tree=(List<Map<String,Object>>)controller.deptTree().get("data");assertThat(tree).hasSize(1);assertThat(tree.get(0)).containsEntry("deptId",100L).containsEntry("studentCount",2).doesNotContainKey("children");
    }
}
