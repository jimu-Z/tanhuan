package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.Student;

public interface IStudentService {
    public List<Student> selectStudentList(Student student);

    public Student selectStudentById(Long studentId);

    public Student selectStudentByStudentNo(String studentNo);

    public int insertStudent(Student student);

    public int updateStudent(Student student);

    public int deleteStudentByIds(Long[] studentIds);

    public String importStudents(List<Student> studentList, boolean overwrite);

    public boolean checkStudentNoUnique(Student student);

    public List<Student> selectStudentByDeptId(Long deptId);
}