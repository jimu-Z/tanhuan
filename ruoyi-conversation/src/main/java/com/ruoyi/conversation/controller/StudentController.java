package com.ruoyi.conversation.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.conversation.domain.Student;
import com.ruoyi.conversation.service.IStudentService;

@RestController
@RequestMapping("/conversation/student")
public class StudentController extends BaseController {
    @Autowired
    private IStudentService studentService;

    @PreAuthorize("@ss.hasPermi('conversation:student:list')")
    @DataScope(deptAlias = "s", userAlias = "s")
    @GetMapping("/list")
    public TableDataInfo list(Student student) {
        startPage();
        List<Student> list = studentService.selectStudentList(student);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('conversation:student:query')")
    @GetMapping("/{studentId}")
    public AjaxResult getInfo(@PathVariable Long studentId) {
        return success(studentService.selectStudentById(studentId));
    }

    @PreAuthorize("@ss.hasPermi('conversation:student:add')")
    @Log(title = "学生管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Student student) {
        student.setCreateBy(getUsername());
        return toAjax(studentService.insertStudent(student));
    }

    @PreAuthorize("@ss.hasPermi('conversation:student:edit')")
    @Log(title = "学生管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Student student) {
        student.setUpdateBy(getUsername());
        return toAjax(studentService.updateStudent(student));
    }

    @PreAuthorize("@ss.hasPermi('conversation:student:remove')")
    @Log(title = "学生管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds) {
        return toAjax(studentService.deleteStudentByIds(studentIds));
    }

    @PreAuthorize("@ss.hasPermi('conversation:student:import')")
    @Log(title = "学生管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(@RequestParam("file") MultipartFile file, boolean overwrite) throws Exception {
        ExcelUtil<Student> util = new ExcelUtil<Student>(Student.class);
        List<Student> studentList = util.importExcel(file.getInputStream());
        String message = studentService.importStudents(studentList, overwrite);
        return success(message);
    }

    @PreAuthorize("@ss.hasPermi('conversation:student:export')")
    @Log(title = "学生管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Student student) {
        List<Student> list = studentService.selectStudentList(student);
        ExcelUtil<Student> util = new ExcelUtil<Student>(Student.class);
        util.exportExcel(response, list, "学生数据");
    }

    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        ExcelUtil<Student> util = new ExcelUtil<Student>(Student.class);
        util.importTemplateExcel(response, "学生数据");
    }

    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<Student> list = studentService.selectStudentList(new Student());
        return success(list);
    }
}