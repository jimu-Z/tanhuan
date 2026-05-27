package com.ruoyi.conversation.mapper;

import java.util.List;
import com.ruoyi.conversation.domain.Student;

public interface StudentMapper {
    public List<Student> selectStudentList(Student student);

    public Student selectStudentById(Long studentId);

    public Student selectStudentByStudentNo(String studentNo);

    public int insertStudent(Student student);

    public int updateStudent(Student student);

    public int deleteStudentById(Long studentId);

    public int deleteStudentByIds(Long[] studentIds);

    public Student checkStudentNoUnique(String studentNo);

    public List<Student> selectStudentByDeptId(Long deptId);

    public Long countTotalStudents();
}