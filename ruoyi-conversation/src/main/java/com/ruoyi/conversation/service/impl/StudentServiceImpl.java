package com.ruoyi.conversation.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.conversation.domain.Student;
import com.ruoyi.conversation.mapper.ConversationMapper;
import com.ruoyi.conversation.mapper.StudentMapper;
import com.ruoyi.conversation.service.IStudentService;

@Service
public class StudentServiceImpl implements IStudentService {
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    @DataScope(deptAlias = "d")
    public List<Student> selectStudentList(Student student) {
        return studentMapper.selectStudentList(student);
    }

    @Override
    public Student selectStudentById(Long studentId) {
        return studentMapper.selectStudentById(studentId);
    }

    @Override
    public Student selectStudentByStudentNo(String studentNo) {
        return studentMapper.selectStudentByStudentNo(studentNo);
    }

    @Override
    @Transactional
    public int insertStudent(Student student) {
        student.setCreateBy(SecurityUtils.getUsername());
        return studentMapper.insertStudent(student);
    }

    @Override
    @Transactional
    public int updateStudent(Student student) {
        student.setUpdateBy(SecurityUtils.getUsername());
        return studentMapper.updateStudent(student);
    }

    @Override
    @Transactional
    public int deleteStudentByIds(Long[] studentIds) {
        for (Long studentId : studentIds) {
            Student student = studentMapper.selectStudentById(studentId);
            if (student != null) {
                List<com.ruoyi.conversation.domain.Conversation> conversations = conversationMapper
                        .selectConversationByStudentNo(student.getStudentNo());
                if (conversations != null && !conversations.isEmpty()) {
                    throw new ServiceException("学生[" + student.getStudentName() + "]存在谈话记录，不能删除");
                }
            }
        }
        return studentMapper.deleteStudentByIds(studentIds);
    }

    @Override
    @Transactional
    public String importStudents(List<Student> studentList, boolean overwrite) {
        int successCount = 0;
        int skipCount = 0;
        for (Student student : studentList) {
            boolean unique = checkStudentNoUnique(student);
            if (!unique) {
                if (overwrite) {
                    Student existing = studentMapper.selectStudentByStudentNo(student.getStudentNo());
                    student.setStudentId(existing.getStudentId());
                    student.setUpdateBy(SecurityUtils.getUsername());
                    studentMapper.updateStudent(student);
                    successCount++;
                } else {
                    skipCount++;
                }
            } else {
                student.setCreateBy(SecurityUtils.getUsername());
                studentMapper.insertStudent(student);
                successCount++;
            }
        }
        return "成功导入" + successCount + "条，跳过" + skipCount + "条重复";
    }

    @Override
    public boolean checkStudentNoUnique(Student student) {
        Student info = studentMapper.checkStudentNoUnique(student.getStudentNo());
        if (info != null && !info.getStudentId().equals(student.getStudentId())) {
            return false;
        }
        return true;
    }

    @Override
    public List<Student> selectStudentByDeptId(Long deptId) {
        return studentMapper.selectStudentByDeptId(deptId);
    }
}