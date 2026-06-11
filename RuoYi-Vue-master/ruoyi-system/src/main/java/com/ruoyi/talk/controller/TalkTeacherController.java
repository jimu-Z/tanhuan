package com.ruoyi.talk.controller;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.service.ITalkTeacherService;
import com.ruoyi.talk.service.ITalkTeacherClassService;

/**
 * 教师信息Controller
 *
 * @author admin
 * @date 2026-06-06
 */
@RestController
@RequestMapping("/talk/teacher")
public class TalkTeacherController extends BaseController {

    @Autowired
    private ITalkTeacherService teacherService;

    @Autowired
    private ITalkTeacherClassService teacherClassService;

    @PreAuthorize("@ss.hasPermi('talk:teacher:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkTeacher teacher) {
        startPage();
        List<TalkTeacher> list = teacherService.selectTalkTeacherList(teacher);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:teacher:query')")
    @GetMapping("/{teacherId}")
    public AjaxResult getInfo(@PathVariable Long teacherId) {
        return success(teacherService.selectTalkTeacherById(teacherId));
    }

    @PreAuthorize("@ss.hasPermi('talk:teacher:add')")
    @Log(title = "教师管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody TalkTeacher teacher) {
        teacher.setCreateBy(SecurityUtils.getUsername());
        if (!teacherService.checkTeacherCodeUnique(teacher)) {
            return error("工号 '" + teacher.getTeacherCode() + "' 已存在");
        }
        return toAjax(teacherService.insertTalkTeacher(teacher));
    }

    @PreAuthorize("@ss.hasPermi('talk:teacher:edit')")
    @Log(title = "教师管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody TalkTeacher teacher) {
        return toAjax(teacherService.updateTalkTeacher(teacher));
    }

    @PreAuthorize("@ss.hasPermi('talk:teacher:remove')")
    @Log(title = "教师管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{teacherIds}")
    public AjaxResult remove(@PathVariable Long[] teacherIds) {
        return toAjax(teacherService.deleteTalkTeacherByIds(teacherIds));
    }

    @PreAuthorize("@ss.hasPermi('talk:teacher:import')")
    @Log(title = "教师管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        if (file == null || file.isEmpty()) {
            return error("请选择文件");
        }
        // 自动检测表头行：兼容纯表头Excel和带标题行+表头的台账Excel
        int titleNum = detectTitleRowNum(file.getInputStream());
        ExcelUtil<TalkTeacher> util = new ExcelUtil<>(TalkTeacher.class);
        List<TalkTeacher> teacherList = util.importExcel(file.getInputStream(), titleNum);
        String message = teacherService.importTeacher(teacherList, updateSupport);
        return success(message);
    }

    /**
     * 检测Excel文件表头所在行号
     * 如果第0行包含已知列名（工号/姓名等），返回0；否则返回1（兼容带标题行的台账）
     */
    private int detectTitleRowNum(InputStream is) throws Exception {
        Set<String> knownHeaders = new HashSet<>(Arrays.asList("工号", "姓名", "所属学院", "岗位", "手机号码", "备注", "所属学院ID"));
        Workbook wb = WorkbookFactory.create(is);
        Sheet sheet = wb.getSheetAt(0);
        if (sheet != null) {
            Row row0 = sheet.getRow(0);
            if (row0 != null) {
                for (int i = 0; i < row0.getLastCellNum(); i++) {
                    String val = row0.getCell(i) != null ? row0.getCell(i).toString().trim() : "";
                    if (knownHeaders.contains(val)) {
                        wb.close();
                        return 0;
                    }
                }
            }
            // 第二行是表头，跳过第一行标题
            Row row1 = sheet.getRow(1);
            if (row1 != null) {
                for (int i = 0; i < row1.getLastCellNum(); i++) {
                    String val = row1.getCell(i) != null ? row1.getCell(i).toString().trim() : "";
                    if (knownHeaders.contains(val)) {
                        wb.close();
                        return 1;
                    }
                }
            }
        }
        wb.close();
        return 0; // 兜底，按默认解析
    }

    @PreAuthorize("@ss.hasPermi('talk:teacher:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil<TalkTeacher> util = new ExcelUtil<>(TalkTeacher.class);
        util.importTemplateExcel(response, "教师数据");
    }

    /**
     * 获取指定学院下的辅导员/班主任列表（供书记新增和预约选择用）
     */
    @PreAuthorize("@ss.hasPermi('talk:teacher:list')")
    @GetMapping("/counselors/{deptId}")
    public AjaxResult counselors(@PathVariable Long deptId) {
        List<TalkTeacher> list = teacherService.selectCounselorsByDeptId(deptId);
        return success(list);
    }

    /**
     * 获取教师管理的班级名列表
     */
    @PreAuthorize("@ss.hasPermi('talk:teacher:list')")
    @GetMapping("/{teacherId}/classes")
    public AjaxResult getTeacherClasses(@PathVariable Long teacherId) {
        TalkTeacher teacher = teacherService.selectTalkTeacherById(teacherId);
        if (teacher == null)
            return error("教师不存在");
        return success(teacherClassService.getClassNamesByTeacherCode(teacher.getTeacherCode()));
    }

    /**
     * 保存教师管理的班级
     */
    @PreAuthorize("@ss.hasPermi('talk:teacher:edit')")
    @PutMapping("/{teacherId}/classes")
    public AjaxResult saveTeacherClasses(@PathVariable Long teacherId, @RequestBody List<String> classNames) {
        TalkTeacher teacher = teacherService.selectTalkTeacherById(teacherId);
        if (teacher == null)
            return error("教师不存在");
        teacherClassService.saveTeacherClasses(teacher.getTeacherCode(), classNames);
        return success();
    }

    /**
     * 获取全校所有班级名列表
     */
    @PreAuthorize("@ss.hasPermi('talk:teacher:list')")
    @GetMapping("/allClassNames")
    public AjaxResult getAllClassNames() {
        return success(teacherService.selectAllClassNames());
    }
}
