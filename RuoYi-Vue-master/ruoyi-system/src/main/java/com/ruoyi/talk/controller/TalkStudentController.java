package com.ruoyi.talk.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.service.ITalkStudentService;
import com.ruoyi.talk.service.ITalkTeacherService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 学生信息管理Controller
 * 
 * @author admin
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/ruoyi-system/talk")
public class TalkStudentController extends BaseController {
    @Autowired
    private ITalkStudentService talkStudentService;

    @Autowired
    private ITalkTeacherService talkTeacherService;

    @Autowired
    private ISysDeptService deptService;

    /**
     * 查询学生信息管理列表（含上次谈话时间）
     */
    @PreAuthorize("@ss.hasPermi('talk:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkStudent talkStudent,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") List<Date> dateRange) {
        startPage();
        if (dateRange != null && dateRange.size() == 2) {
            List<TalkStudent> list = talkStudentService.selectUntalkedStudentsInPeriod(
                    dateRange.get(0), dateRange.get(1), talkStudent.getDeptId());
            return getDataTable(list);
        }
        List<TalkStudent> list = talkStudentService.selectTalkStudentListWithLastTalk(talkStudent);
        return getDataTable(list);
    }

    /**
     * 获取班级树（学院→年级→班级），含学生数量
     */
    @PreAuthorize("@ss.hasPermi('talk:student:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree() {
        SysDept dept = new SysDept();
        dept.setDeptType("class");
        List<SysDept> depts = deptService.selectDeptList(dept);
        List<Map<String, Object>> tree = buildDeptTree(depts);
        return success(tree);
    }

    private List<Map<String, Object>> buildDeptTree(List<SysDept> depts) {
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        Map<Long, List<Map<String, Object>>> childrenMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (SysDept d : depts) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("deptId", d.getDeptId());
            node.put("deptName", d.getDeptName());
            node.put("parentId", d.getParentId());
            node.put("deptType", d.getDeptType());
            node.put("studentCount", talkStudentService.countStudentsByDeptId(d.getDeptId()));
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(d.getDeptId(), node);
        }

        for (SysDept d : depts) {
            Map<String, Object> node = nodeMap.get(d.getDeptId());
            if (d.getParentId() == null || d.getParentId() == 0 || !nodeMap.containsKey(d.getParentId())) {
                if (node != null)
                    roots.add(node);
            } else {
                childrenMap.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }

        for (Map.Entry<Long, List<Map<String, Object>>> entry : childrenMap.entrySet()) {
            Map<String, Object> parent = nodeMap.get(entry.getKey());
            if (parent != null) {
                ((List<Map<String, Object>>) parent.get("children")).addAll(entry.getValue());
            }
        }

        nodeMap.values().forEach(n -> {
            List<Map<String, Object>> children = (List<Map<String, Object>>) n.get("children");
            if (children.isEmpty()) {
                n.remove("children");
            }
        });

        List<Map<String, Object>> collegeRoots = new ArrayList<>();
        for (Map<String, Object> root : roots) {
            if ("college".equals(root.get("deptType"))) {
                collegeRoots.add(root);
            }
        }

        return collegeRoots.isEmpty() ? roots : collegeRoots;
    }

    /**
     * 查询未谈学生（指定日期范围内无谈话记录的学生）
     */
    @PreAuthorize("@ss.hasPermi('talk:student:list')")
    @GetMapping("/untalked")
    public TableDataInfo untalked(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {
        startPage();
        Map<String, Object> params = new HashMap<>();
        if (deptId != null)
            params.put("deptId", deptId);
        if (beginDate != null)
            params.put("beginDate", beginDate);
        if (endDate != null)
            params.put("endDate", endDate);
        applyDataScope(params);
        List<TalkStudent> list = talkStudentService.selectUntalkedStudents(params);
        return getDataTable(list);
    }

    /**
     * 查询指定时间段内未被谈话的学生（新接口）
     */
    @PreAuthorize("@ss.hasPermi('talk:student:list')")
    @GetMapping("/untalkedInPeriod")
    public TableDataInfo untalkedInPeriod(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
            @RequestParam(required = false) Long deptId) {
        startPage();
        List<TalkStudent> list = talkStudentService.selectUntalkedStudentsInPeriod(startTime, endTime, deptId);
        return getDataTable(list);
    }

    /**
     * 按教师所属学院查询学生（书记看全院，辅导员看自己工号关联的学生）
     */
    @PreAuthorize("@ss.hasPermi('talk:teacher:list')")
    @GetMapping("/byTeacher/{teacherId}")
    public TableDataInfo listByTeacher(@PathVariable Long teacherId) {
        startPage();
        logger.info("[学生按钮] teacherId={}", teacherId);
        TalkTeacher teacher = talkTeacherService.selectTalkTeacherById(teacherId);
        logger.info("[学生按钮] teacher={}, deptId={}, position={}",
                teacher != null ? teacher.getTeacherName() : "null",
                teacher != null ? teacher.getDeptId() : "null",
                teacher != null ? teacher.getPosition() : "null");
        if (teacher == null || teacher.getDeptId() == null) {
            logger.info("[学生按钮] no teacher or no deptId, returning empty");
            return getDataTable(new ArrayList<>());
        }
        // 辅导员/班主任：通过talk_teacher_class表查自己管理的班级学生
        if ("辅导员".equals(teacher.getPosition()) || "班主任".equals(teacher.getPosition())) {
            List<TalkStudent> students = talkStudentService.selectByTeacherCode(teacher.getTeacherCode());
            logger.info("[学生按钮] 辅导员模式, teacherCode={}, result size={}",
                    teacher.getTeacherCode(), students != null ? students.size() : 0);
            return getDataTable(students);
        }
        // 书记/副书记：按学院查全院学生
        List<TalkStudent> students = talkStudentService.selectByCollegeDeptId(teacher.getDeptId());
        logger.info("[学生按钮] 书记模式, deptId={}, result size={}",
                teacher.getDeptId(), students != null ? students.size() : 0);
        return getDataTable(students);
    }

    private void applyDataScope(Map<String, Object> params) {
        if (SecurityUtils.isAdmin())
            return;
        String username = SecurityUtils.getUsername();
        if (username == null)
            return;
        if (SecurityUtils.hasRole("talk_counselor")) {
            params.put("counselorUsername", username);
        } else if (SecurityUtils.hasRole("talk_secretary")) {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                params.put("secretaryDeptId", deptId);
            }
        }
    }

    /**
     * 导出学生信息管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:student:export')")
    @Log(title = "学生信息管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TalkStudent talkStudent) {
        List<TalkStudent> list = talkStudentService.selectTalkStudentList(talkStudent);
        ExcelUtil<TalkStudent> util = new ExcelUtil<TalkStudent>(TalkStudent.class);
        util.exportExcel(response, list, "学生信息管理数据");
    }

    /**
     * 获取学生详情（含历史谈话记录）
     */
    @PreAuthorize("@ss.hasPermi('talk:student:query')")
    @GetMapping("/detail/{studentId:\\d+}")
    public AjaxResult getDetail(@PathVariable("studentId") Long studentId) {
        return success(talkStudentService.getStudentDetail(studentId));
    }

    /**
     * 获取学生信息管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('talk:student:query')")
    @GetMapping("/byId/{studentId}")
    public AjaxResult getInfo(@PathVariable("studentId") Long studentId) {
        return success(talkStudentService.selectTalkStudentByStudentId(studentId));
    }

    /**
     * 新增学生信息管理
     */
    @PreAuthorize("@ss.hasPermi('talk:student:add')")
    @Log(title = "学生信息管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TalkStudent talkStudent) {
        return toAjax(talkStudentService.insertTalkStudent(talkStudent));
    }

    /**
     * 修改学生信息管理
     */
    @PreAuthorize("@ss.hasPermi('talk:student:edit')")
    @Log(title = "学生信息管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TalkStudent talkStudent) {
        return toAjax(talkStudentService.updateTalkStudent(talkStudent));
    }

    /**
     * 删除学生信息管理
     */
    @PreAuthorize("@ss.hasPermi('talk:student:remove')")
    @Log(title = "学生信息管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{studentIds:[\\d,]+}")
    public AjaxResult remove(@PathVariable Long[] studentIds) {
        return toAjax(talkStudentService.deleteTalkStudentByStudentIds(studentIds));
    }

    /**
     * 导入预览
     */
    @PreAuthorize("@ss.hasPermi('talk:student:import')")
    @Log(title = "学生信息管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import/preview")
    public AjaxResult importPreview(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("请选择文件");
        }
        return success(talkStudentService.importPreview(file));
    }

    /**
     * 执行导入
     */
    @PreAuthorize("@ss.hasPermi('talk:student:import')")
    @Log(title = "学生信息管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import/execute")
    public AjaxResult importExecute(@RequestBody List<Map<String, Object>> confirmedRows,
            @RequestParam(defaultValue = "skip") String importMode) {
        return success(talkStudentService.importExecute(confirmedRows, importMode));
    }
}
