package com.ruoyi.talk.service.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.service.ITalkAlertService;
import com.ruoyi.talk.service.ITalkStudentService;
import com.ruoyi.talk.service.ITalkTeacherService;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.common.exception.ServiceException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 学生信息管理Service业务层处理
 * 
 * @author admin
 * @date 2026-05-27
 */
@Service
public class TalkStudentServiceImpl implements ITalkStudentService {
    private static final Logger log = LoggerFactory.getLogger(TalkStudentServiceImpl.class);
    @Autowired
    private TalkStudentMapper talkStudentMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Autowired
    private TalkSessionMapper talkSessionMapper;

    @Autowired
    private ITalkTeacherService talkTeacherService;

    @Autowired
    private ITalkAlertService talkAlertService;

    @Autowired private StudentImportRelationsService importRelations;
    @Autowired private PlatformTransactionManager transactionManager;

    private static final Long TOP_DEPT_ID = 100L;

    /**
     * 查询学生信息管理
     * 
     * @param studentId 学生信息管理主键
     * @return 学生信息管理
     */
    @Override
    public TalkStudent selectTalkStudentByStudentId(Long studentId) {
        TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
        requireStudentAccess(student);
        return student;
    }

    /**
     * 查询学生信息管理列表
     * 
     * @param talkStudent 学生信息管理
     * @return 学生信息管理
     */
    @Override
    public List<TalkStudent> selectTalkStudentList(TalkStudent talkStudent) {
        applyCollegeFilter(talkStudent);
        return talkStudentMapper.selectTalkStudentList(talkStudent);
    }

    /**
     * 新增学生信息管理
     * 
     * @param talkStudent 学生信息管理
     * @return 结果
     */
    @Override
    public int insertTalkStudent(TalkStudent talkStudent) {
        requireClassDepartment(talkStudent.getDeptId());
        TalkPermissionUtils.requireCollegeAccess(resolveCollegeDeptId(talkStudent.getDeptId()));
        talkStudent.setCreateBy(SecurityUtils.getUsername());
        talkStudent.setCreateTime(DateUtils.getNowDate());
        Long gapId = talkStudentMapper.selectMinAvailableStudentId();
        if (gapId != null) {
            talkStudent.setStudentId(gapId);
        }
        return talkStudentMapper.insertTalkStudent(talkStudent);
    }

    /**
     * 修改学生信息管理
     * 
     * @param talkStudent 学生信息管理
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTalkStudent(TalkStudent talkStudent) {
        TalkStudent existing = talkStudentMapper.selectTalkStudentByStudentId(talkStudent.getStudentId());
        requireStudentAccess(existing);
        if(talkStudent.getStudentCode()!=null) {
            talkStudent.setStudentCode(talkStudent.getStudentCode().trim());
            if(talkStudent.getStudentCode().isEmpty()) throw new ServiceException("学号不能为空");
        }
        if (talkStudent.getDeptId() != null) {
            requireClassDepartment(talkStudent.getDeptId());
            TalkPermissionUtils.requireCollegeAccess(resolveCollegeDeptId(talkStudent.getDeptId()));
        }
        talkStudent.setUpdateBy(SecurityUtils.getUsername());
        talkStudent.setUpdateTime(DateUtils.getNowDate());
        int rows = talkStudentMapper.updateTalkStudent(talkStudent);
        if(importRelations.hasImportedAccount(existing.getStudentId()))
            importRelations.syncExistingAccount(existing,talkStudent,
                    resolveCollegeDeptId(talkStudent.getDeptId()==null?existing.getDeptId():talkStudent.getDeptId()));
        // 心理健康状态变化时自动生成预警
        if (StringUtils.isNotEmpty(talkStudent.getMentalHealthStatus()) && talkStudent.getStudentId() != null) {
            talkAlertService.autoGenerateAlertForStudent(talkStudent.getStudentId(),
                    talkStudent.getMentalHealthStatus());
        }
        return rows;
    }

    /**
     * 批量删除学生信息管理
     * 
     * @param studentIds 需要删除的学生信息管理主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTalkStudentByStudentIds(Long[] studentIds) {
        TalkPermissionUtils.requireCollegeManager();
        for (Long studentId : studentIds) {
            requireStudentAccess(talkStudentMapper.selectTalkStudentByStudentId(studentId));
            int recordCount = talkStudentMapper.countRecordsByStudentId(studentId);
            if (recordCount > 0) {
                TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
                String name = student != null ? student.getStudentName() : "未知";
                throw new ServiceException("学生 " + name + " 有 " + recordCount + " 条谈话记录，无法删除。请先处理相关谈话记录。");
            }
        }
        return talkStudentMapper.deleteTalkStudentByStudentIds(studentIds);
    }

    /**
     * 删除学生信息管理信息
     * 
     * @param studentId 学生信息管理主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTalkStudentByStudentId(Long studentId) {
        TalkPermissionUtils.requireCollegeManager();
        requireStudentAccess(talkStudentMapper.selectTalkStudentByStudentId(studentId));
        int recordCount = talkStudentMapper.countRecordsByStudentId(studentId);
        if (recordCount > 0) {
            TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
            String name = student != null ? student.getStudentName() : "未知";
            throw new ServiceException("学生 " + name + " 有 " + recordCount + " 条谈话记录，无法删除。请先处理相关谈话记录。");
        }
        return talkStudentMapper.deleteTalkStudentByStudentId(studentId);
    }

    @Override
    public Map<String, Object> getStudentDetail(Long studentId) {
        Map<String, Object> result = new LinkedHashMap<>();
        TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
        requireStudentAccess(student);
        if (student == null) {
            result.put("student", null);
            result.put("history", new ArrayList<>());
            return result;
        }
        result.put("student", student);
        result.put("managers", importRelations.managers(studentId));
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordByStudentId(studentId);
        List<Map<String, Object>> history = new ArrayList<>();
        if (records != null && !records.isEmpty()) {
            Set<Long> sessionIds = records.stream()
                    .map(TalkStudentRecord::getSessionId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            Map<Long, TalkSession> sessionCache = new HashMap<>();
            List<TalkSession> sessions = talkSessionMapper
                    .selectTalkSessionBySessionIds(sessionIds.toArray(new Long[0]));
            for (TalkSession s : sessions) {
                sessionCache.put(s.getSessionId(), s);
            }
            for (TalkStudentRecord rec : records) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("record", rec);
                item.put("session", sessionCache.get(rec.getSessionId()));
                history.add(item);
            }
        }
        result.put("history", history);
        return result;
    }

    /**
     * Excel导入预览
     */
    @Override
    public Map<String, Object> importPreview(MultipartFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        log.debug("[IMPORT-DEBUG] File received: name={}, size={}", file.getOriginalFilename(), file.getSize());

        try (InputStream is = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            log.debug("[IMPORT-DEBUG] Sheet: lastRowNum={}, physicalRows={}", sheet.getLastRowNum(),
                    sheet.getPhysicalNumberOfRows());
            if (sheet.getLastRowNum() < 1) {
                throw new RuntimeException("Excel文件至少需要表头行和一行数据");
            }

            int headerRowIdx = 0;
            int dataStartRow = 1;
            Row row0 = sheet.getRow(0);
            List<String> row0Cells = readRowCells(row0);
            Map<String, Integer> mapping0 = buildFieldMapping(row0Cells);
            long recognized0 = mapping0.values().stream().filter(c -> c > 0).count();
            if (recognized0 < 2 && sheet.getLastRowNum() >= 2) {
                headerRowIdx = 1;
                dataStartRow = 2;
            }
            log.debug("[IMPORT-DEBUG] headerRowIdx={}, dataStartRow={}, recognized0={}",
                    headerRowIdx, dataStartRow, recognized0);

            Row headerRow = sheet.getRow(headerRowIdx);
            List<String> headers = readRowCells(headerRow);
            log.debug("[IMPORT-DEBUG] Headers count={}, first5={}", headers.size(),
                    headers.subList(0, Math.min(5, headers.size())));
            Map<String, Integer> fieldMapping = buildFieldMapping(headers);
            log.debug("[IMPORT-DEBUG] Field mapping size={}", fieldMapping.size());

            result.put("headers", headers);
            result.put("fieldMapping", convertFieldMappingForDisplay(fieldMapping));

            int totalRows = Math.max(0, sheet.getLastRowNum() - headerRowIdx);
            log.debug("[IMPORT-DEBUG] totalRows={}", totalRows);
            int errorCount = 0;
            int warnCount = 0;
            int previewLimit = totalRows;
            List<Map<String, Object>> previewRows = new ArrayList<>();
            Set<String> seenCodesInExcel = new HashSet<>();

            for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                Map<String, String> rowData = parseRowData(row, headers, fieldMapping);
                if (i == dataStartRow) {
                    log.debug("[IMPORT-DEBUG] First data row: student_code={}, student_name={}, college={}",
                            rowData.get("student_code"), rowData.get("student_name"), rowData.get("college"));
                }

                String studentCode = rowData.get("student_code");
                String studentName = rowData.get("student_name");
                String college = rowData.get("college");
                String grade = rowData.get("grade");
                String className = rowData.get("class");

                String status = "ok";
                String message = "";

                if (StringUtils.isEmpty(studentCode) && StringUtils.isEmpty(studentName)) {
                    status = "error";
                    message = "姓名和学号均为空";
                    errorCount++;
                } else if (StringUtils.isEmpty(studentCode)) {
                    status = "error";
                    message = "学号为空";
                    errorCount++;
                } else if (StringUtils.isEmpty(studentName)) {
                    status = "error";
                    message = "姓名为空";
                    errorCount++;
                } else if (StringUtils.isEmpty(college)) {
                    status = "error";
                    message = "学院为空";
                    errorCount++;
                } else if (StringUtils.isEmpty(grade)) {
                    status = "error";
                    message = "年级为空";
                    errorCount++;
                } else if (StringUtils.isEmpty(className)) {
                    status = "error";
                    message = "班级为空";
                    errorCount++;
                } else if (!StringUtils.isEmpty(studentCode) && seenCodesInExcel.contains(studentCode)) {
                    status = "warn";
                    message = "学号在Excel中重复，将跳过此条记录";
                    warnCount++;
                } else if (isStudentCodeExists(studentCode)) {
                    status = "warn";
                    message = "学号已存在：跳过模式将跳过，覆盖模式将更新";
                    warnCount++;
                }

                if ("ok".equals(status) || "warn".equals(status)) {
                    String scopeError = validateImportScope(college, grade, className);
                    if (scopeError != null) {
                        status = "error";
                        message = scopeError;
                        errorCount++;
                    }
                }

                // 校验Excel中填写的辅导员/书记/班主任/副书记是否存在
                if ("ok".equals(status) || "warn".equals(status)) {
                    String teacherError = validateImportTeachers(college, rowData);
                    if (teacherError != null) {
                        status = "error";
                        message = teacherError;
                        errorCount++;
                    }
                }

                if (!StringUtils.isEmpty(studentCode)) {
                    seenCodesInExcel.add(studentCode);
                }

                if (i - dataStartRow < previewLimit) {
                    Map<String, Object> preview = new LinkedHashMap<>();
                    preview.put("rowNum", i + 1);
                    preview.put("data", rowData);
                    preview.put("status", status);
                    preview.put("message", message);
                    previewRows.add(preview);
                }
            }

            result.put("previewRows", previewRows);
            result.put("totalRows", totalRows);
            result.put("errorCount", errorCount);
            result.put("warnCount", warnCount);
            log.debug("[IMPORT-DEBUG] Preview complete: previewRows={}, totalRows={}, errors={}, warns={}",
                    previewRows.size(), totalRows, errorCount, warnCount);
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            throw new RuntimeException("解析Excel文件失败: " + e.getMessage(), e);
        }
        return result;
    }

    /** 每行独立事务：学生、组织、账号、角色与管理绑定一起提交或回滚。 */
    @Override
    public Map<String, Object> importExecute(List<Map<String, Object>> confirmedRows, String importMode) {
        int successCount=0, skipCount=0, userCreatedCount=0, failureCount=0;
        List<String> messages=new ArrayList<>();
        Set<String> seen=new HashSet<>();
        if (confirmedRows != null) for(int i=0;i<confirmedRows.size();i++) {
            int rowNum=i+1;
            try {
                Map<String,String> data=new HashMap<>();
                for(Map.Entry<String,Object> entry:confirmedRows.get(i).entrySet())
                    data.put(entry.getKey(),entry.getValue()==null?"":entry.getValue().toString().trim());
                normalizeImportEnums(data);
                String code=data.get("student_code");
                if(StringUtils.isEmpty(code) || StringUtils.isEmpty(data.get("student_name")))
                    throw new ServiceException("学号或姓名为空");
                if(!seen.add(code)) { skipCount++;messages.add("第"+rowNum+"行: 学号在Excel中重复，已跳过");continue; }
                TransactionTemplate transaction=new TransactionTemplate(transactionManager);
                transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                ImportRowOutcome outcome=transaction.execute(status->importStudentRow(data,importMode));
                if(outcome.skipped()) skipCount++; else { successCount++;userCreatedCount+=outcome.usersCreated(); }
                for(String message:outcome.messages()) messages.add("第"+rowNum+"行: "+message);
            } catch(Exception error) {
                failureCount++;
                log.error("学生导入第{}行失败，当前行已回滚",rowNum,error);
                messages.add("第"+rowNum+"行: 导入失败 - "+error.getMessage());
            }
        }
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("successCount",successCount);result.put("skipCount",skipCount);
        result.put("userCreatedCount",userCreatedCount);result.put("errorCount",failureCount);
        result.put("errors",messages);return result;
    }

    private record ImportRowOutcome(boolean skipped, int usersCreated, List<String> messages) {}

    private ImportRowOutcome importStudentRow(Map<String,String> rowData, String importMode) {
        String studentCode=rowData.get("student_code"), studentName=rowData.get("student_name");
        String college=rowData.get("college"),grade=rowData.get("grade"),className=rowData.get("class");
        if(StringUtils.isEmpty(college)||StringUtils.isEmpty(grade)||StringUtils.isEmpty(className))
            throw new ServiceException("学院/年级/班级不能为空");
        TalkStudent existing=talkStudentMapper.selectTalkStudentByCode(studentCode);
        if(existing!=null) {
            requireStudentAccess(existing);
            if(!"update".equals(importMode)) return new ImportRowOutcome(true,0,List.of("学号已存在，已跳过"));
        }
        SysUser account=importRelations.validateAccount(studentCode,existing);
        Long collegeDeptId=resolveImportCollege(college);
        if(collegeDeptId==null) throw new ServiceException("学院不存在或不在当前账号管理范围");
        List<TalkTeacher> managers=importRelations.resolveTeachers(collegeDeptId,rowData);
        Long gradeDeptId=resolveImportDept(grade,collegeDeptId,"grade");
        if(gradeDeptId==null) throw new ServiceException("年级不存在；老师不能创建年级或班级");
        Long classDeptId=resolveImportDept(className,gradeDeptId,"class");
        if(classDeptId==null) throw new ServiceException("班级不存在；老师不能创建年级或班级");
        requireClassDepartment(classDeptId);
        List<String> notes=new ArrayList<>();
        String phone=rowData.get("phone");
        if(StringUtils.isNotEmpty(phone)&&!phone.matches("^1[3-9]\\d{9}$")) {
            rowData.put("phone","");notes.add("手机号格式错误，已清空，学生资料继续导入");
        }
        TalkStudent student = new TalkStudent();
        student.setStudentCode(studentCode);
        student.setStudentName(studentName);
        student.setDeptId(classDeptId);
        student.setGender(getString(rowData, "gender", "0"));
        student.setNation(getString(rowData, "nation"));
        student.setPhone(getString(rowData, "phone"));
        student.setIdCard(getString(rowData, "id_card"));
        student.setAddress(getString(rowData, "address"));
        student.setFatherName(getString(rowData, "father_name"));
        student.setFatherPhone(getString(rowData, "father_phone"));
        student.setMotherName(getString(rowData, "mother_name"));
        student.setMotherPhone(getString(rowData, "mother_phone"));
        student.setClassMonitor(getString(rowData, "class_monitor"));
        student.setDormLeader(getString(rowData, "dorm_leader"));
        student.setDormBuilding(getString(rowData, "dorm_building"));
        student.setDormRoom(getString(rowData, "dorm_room"));
        student.setEnrollmentStatus(getString(rowData, "enrollment_status"));
        student.setMentalHealthStatus(getString(rowData, "mental_health_status"));
        student.setPovertyLevel(getString(rowData, "poverty_level"));
        student.setPoliticalStatus(getString(rowData, "political_status"));
        student.setRemark(getString(rowData, "remark"));
        student.setUpdateBy(SecurityUtils.getUsername());
        if(existing!=null) {
            student.setStudentId(existing.getStudentId());student.setUpdateTime(DateUtils.getNowDate());
            if(talkStudentMapper.updateTalkStudent(student)!=1) throw new ServiceException("学生资料更新失败");
            notes.add("学号已存在，已更新");
        } else {
            student.setCreateBy(SecurityUtils.getUsername());student.setCreateTime(DateUtils.getNowDate());
            Long gapId=talkStudentMapper.selectMinAvailableStudentId();if(gapId!=null) student.setStudentId(gapId);
            if(talkStudentMapper.insertTalkStudent(student)!=1) throw new ServiceException("学生资料保存失败");
        }
        int created=importRelations.saveAccountAndBindings(student,className,collegeDeptId,managers,account);
        if(StringUtils.isNotEmpty(student.getMentalHealthStatus()))
            talkAlertService.autoGenerateAlertForStudent(student.getStudentId(),student.getMentalHealthStatus());
        return new ImportRowOutcome(false,created,notes);
    }

    private Map<String, Integer> buildFieldMapping(List<String> headers) {
        Map<String, Integer> mapping = new LinkedHashMap<>();
        int phoneIndex = 0;
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).trim();
            if (h.contains("序号")) {
                mapping.put(String.valueOf(i), -1);
            } else if (h.contains("学院") && !h.contains("书记") && !h.contains("副书记")) {
                mapping.put(String.valueOf(i), 100);
            } else if (h.contains("年级")) {
                mapping.put(String.valueOf(i), 101);
            } else if (h.contains("班级")) {
                mapping.put(String.valueOf(i), 102);
            } else if (h.contains("学生姓名")) {
                mapping.put(String.valueOf(i), 200);
            } else if (h.contains("父亲姓名")) {
                mapping.put(String.valueOf(i), 207);
            } else if (h.contains("母亲姓名")) {
                mapping.put(String.valueOf(i), 209);
            } else if (h.contains("姓名")) {
                mapping.put(String.valueOf(i), 200);
            } else if (h.contains("学号")) {
                mapping.put(String.valueOf(i), 201);
            } else if (h.contains("性别")) {
                mapping.put(String.valueOf(i), 202);
            } else if (h.contains("民族")) {
                mapping.put(String.valueOf(i), 203);
            } else if (h.contains("联系电话") || h.contains("电话")) {
                phoneIndex++;
                if (phoneIndex == 1) {
                    mapping.put(String.valueOf(i), 300);
                } else if (phoneIndex == 2) {
                    mapping.put(String.valueOf(i), 301);
                } else if (phoneIndex == 3) {
                    mapping.put(String.valueOf(i), 302);
                } else {
                    mapping.put(String.valueOf(i), -1);
                }
            } else if (h.contains("身份证")) {
                mapping.put(String.valueOf(i), 205);
            } else if (h.contains("家庭住址") || h.contains("住址")) {
                mapping.put(String.valueOf(i), 206);
            } else if (h.contains("班长")) {
                mapping.put(String.valueOf(i), 211);
            } else if (h.contains("舍长") || h.contains("宿舍长")) {
                mapping.put(String.valueOf(i), 212);
            } else if (h.contains("宿舍楼")) {
                mapping.put(String.valueOf(i), 213);
            } else if (h.contains("宿舍号")) {
                mapping.put(String.valueOf(i), 214);
            } else if (h.contains("学籍状态")) {
                mapping.put(String.valueOf(i), 215);
            } else if (h.contains("心理健康") || h.contains("心理")) {
                mapping.put(String.valueOf(i), 216);
            } else if (h.contains("贫困") || h.contains("等级")) {
                mapping.put(String.valueOf(i), 217);
            } else if (h.contains("备注")) {
                mapping.put(String.valueOf(i), 218);
            } else if (h.contains("学院书记") && !h.contains("副书记")) {
                mapping.put(String.valueOf(i), 400);
            } else if (h.contains("学院副书记")) {
                mapping.put(String.valueOf(i), 401);
            } else if (h.contains("辅导员")) {
                mapping.put(String.valueOf(i), 402);
            } else if (h.contains("班主任")) {
                mapping.put(String.valueOf(i), 403);
            } else if (h.contains("政治面貌")) {
                mapping.put(String.valueOf(i), 219);
            } else {
                mapping.put(String.valueOf(i), -1);
            }
        }
        return mapping;
    }

    private Map<String, String> convertFieldMappingForDisplay(Map<String, Integer> mapping) {
        Map<String, String> display = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : mapping.entrySet()) {
            display.put(entry.getKey(), getFieldNameByCode(entry.getValue()));
        }
        return display;
    }

    private String getFieldNameByCode(int code) {
        switch (code) {
            case -1:
                return "skip";
            case 100:
                return "college";
            case 101:
                return "grade";
            case 102:
                return "class";
            case 200:
                return "student_name";
            case 201:
                return "student_code";
            case 202:
                return "gender";
            case 203:
                return "nation";
            case 205:
                return "id_card";
            case 206:
                return "address";
            case 207:
                return "father_name";
            case 209:
                return "mother_name";
            case 211:
                return "class_monitor";
            case 212:
                return "dorm_leader";
            case 213:
                return "dorm_building";
            case 214:
                return "dorm_room";
            case 215:
                return "enrollment_status";
            case 216:
                return "mental_health_status";
            case 217:
                return "poverty_level";
            case 218:
                return "remark";
            case 219:
                return "political_status";
            case 300:
                return "phone";
            case 301:
                return "father_phone";
            case 302:
                return "mother_phone";
            case 400:
                return "secretary";
            case 401:
                return "vice_secretary";
            case 402:
                return "counselor";
            case 403:
                return "head_teacher";
            default:
                return "skip";
        }
    }

    private Map<String, String> parseRowData(Row row, List<String> headers, Map<String, Integer> fieldMapping) {
        Map<String, String> data = new LinkedHashMap<>();
        List<String> cellValues = readRowCells(row);

        for (Map.Entry<String, Integer> entry : fieldMapping.entrySet()) {
            int colIndex = Integer.parseInt(entry.getKey());
            int code = entry.getValue();
            String value = colIndex < cellValues.size() ? cellValues.get(colIndex) : "";
            String fieldName = getFieldNameByCode(code);

            if (!"skip".equals(fieldName) && code >= 0) {
                data.put(fieldName, value);
            }
        }

        normalizeImportEnums(data);
        return data;
    }

    /** 中文模板与旧编码模板均转换为系统存储值；使用当前字典而非固定标签。 */
    private void normalizeImportEnums(Map<String, String> data) {
        String gender = data.get("gender");
        if (gender != null) {
            gender = gender.trim();
            data.put("gender", Map.of("男", "0", "女", "1", "未知", "2").getOrDefault(gender, gender));
        }
        for (String field : List.of("political_status", "enrollment_status", "mental_health_status", "poverty_level")) {
            String value = data.get(field);
            if (value == null) continue;
            value = value.trim();
            data.put(field, value);
            if (value.codePoints().anyMatch(ch -> ch >= 0x3400 && ch <= 0x9fff)) {
                String code = DictUtils.getDictValue(field, value);
                if (StringUtils.isNotEmpty(code)) data.put(field, code);
            }
        }
    }

    private List<String> readRowCells(Row row) {
        List<String> values = new ArrayList<>();
        if (row == null)
            return values;

        int lastCol = row.getLastCellNum();
        if (lastCol < 0)
            lastCol = 0;

        for (int i = 0; i < lastCol; i++) {
            Cell cell = row.getCell(i);
            values.add(getCellValueAsString(cell));
        }
        return values;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";

        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
            }
            double numValue = cell.getNumericCellValue();
            if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                return String.valueOf((long) numValue);
            }
            return String.valueOf(numValue);
        } else if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cellType == CellType.FORMULA) {
            try {
                return cell.getStringCellValue().trim();
            } catch (Exception e) {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        return "";
    }

    private boolean isStudentCodeExists(String studentCode) {
        if (StringUtils.isEmpty(studentCode))
            return false;
        TalkStudent query = new TalkStudent();
        query.setStudentCode(studentCode);
        applyCollegeFilter(query);
        List<TalkStudent> list = talkStudentMapper.selectTalkStudentList(query);
        return list != null && !list.isEmpty();
    }

    /**
     * 根据学院名称查找学院ID
     */
    private Long findCollegeDeptIdByName(String collegeName) {
        if (StringUtils.isEmpty(collegeName)) {
            return null;
        }
        SysDept query = new SysDept();
        query.setDeptName(collegeName.trim());
        List<SysDept> depts = sysDeptMapper.selectDeptList(query);
        if (depts != null) {
            for (SysDept d : depts) {
                if ("college".equals(d.getDeptType()) || ("dept".equals(d.getDeptType())
                        && (d.getParentId() == null || d.getParentId() == 0 || TOP_DEPT_ID.equals(d.getParentId())))) {
                    return d.getDeptId();
                }
            }
        }
        return null;
    }

    private String validateImportScope(String collegeName, String gradeName, String className) {
        if (TalkPermissionUtils.isGlobalAdmin()) {
            return null;
        }
        Long collegeId = TalkPermissionUtils.currentCollegeId();
        SysDept college = sysDeptMapper.selectDeptById(collegeId);
        if (college == null || !college.getDeptName().equals(collegeName.trim())) {
            return "只能导入本学院学生";
        }
        if (TalkPermissionUtils.isTeacher()) {
            Long gradeId = findDept(gradeName, collegeId);
            if (gradeId == null || findDept(className, gradeId) == null) {
                return "年级或班级不存在；老师不能创建年级或班级";
            }
        }
        return null;
    }

    /**
     * 校验Excel中填写的辅导员/书记/班主任/副书记是否存在于该学院（预览用）
     */
    private String validateImportTeachers(String collegeName, Map<String, String> rowData) {
        Long collegeId = findCollegeDeptIdByName(collegeName);
        if (collegeId == null) {
            return null;
        }
        return validateImportTeachersByDeptId(collegeId, rowData);
    }

    /** 预览与执行复用相同的学院内教师存在性检查，不限制岗位或账号角色。 */
    private String validateImportTeachersByDeptId(Long collegeDeptId, Map<String,String> rowData) {
        boolean hasNames=List.of("counselor","head_teacher","secretary","vice_secretary").stream()
                .anyMatch(key->StringUtils.isNotEmpty(rowData.get(key)));
        if(!hasNames) return null;
        try { importRelations.resolveTeachers(collegeDeptId,rowData); return null; }
        catch(ServiceException error) { return error.getMessage(); }
    }

    private Long findOrCreateDept(String deptName, Long parentId, String type) {
        log.debug("[IMPORT-DEBUG] findOrCreateDept: name={}, parentId={}, type={}", deptName, parentId, type);
        if (StringUtils.isEmpty(deptName) || parentId == null) {
            log.error("[IMPORT-ERROR] findOrCreateDept 参数无效: name={}, parentId={}", deptName, parentId);
            return null;
        }
        // 精确查找：deptName + parentId，避免模糊匹配导致重复创建
        SysDept query = new SysDept();
        query.setDeptName(deptName.trim());
        query.setParentId(parentId);
        List<SysDept> depts = sysDeptMapper.selectDeptList(query);
        if (depts != null && !depts.isEmpty()) {
            for (SysDept d : depts) {
                if (d.getDeptName().equals(deptName.trim()) && d.getParentId().equals(parentId)) {
                    // 修复历史数据：如果已存在部门但缺少dept_type，补齐
                    if (d.getDeptType() == null) {
                        d.setDeptType(type);
                        sysDeptMapper.updateDept(d);
                        log.info("[IMPORT-FIX] 补全部门 {} dept_type = {}", d.getDeptId(), type);
                    }
                    return d.getDeptId();
                }
            }
        }

        // 不存在则创建，但先二次确认防止并发重复创建
        SysDept checkAgain = sysDeptMapper.checkDeptNameUnique(deptName.trim(), parentId);
        if (checkAgain != null) {
            if (checkAgain.getDeptType() == null) {
                checkAgain.setDeptType(type);
                sysDeptMapper.updateDept(checkAgain);
            }
            return checkAgain.getDeptId();
        }

        String ancestors;
        if (TOP_DEPT_ID.equals(parentId)) {
            ancestors = "0,100";
        } else {
            SysDept parentDept = sysDeptMapper.selectDeptById(parentId);
            ancestors = parentDept != null ? parentDept.getAncestors() + "," + parentId : "0," + parentId;
        }

        SysDept newDept = new SysDept();
        newDept.setDeptName(deptName.trim());
        newDept.setParentId(parentId);
        newDept.setAncestors(ancestors);
        newDept.setDeptType(type);
        newDept.setOrderNum(1);
        newDept.setStatus("0");
        newDept.setDelFlag("0");
        newDept.setCreateBy(SecurityUtils.getUsername());
        newDept.setCreateTime(new Date());
        sysDeptMapper.insertDept(newDept);
        // 安全检查：如果主键未回填，用查询获取
        if (newDept.getDeptId() == null) {
            SysDept created = sysDeptMapper.checkDeptNameUnique(deptName.trim(), parentId);
            if (created != null) {
                log.info("[IMPORT-CREATE] 创建部门 {} deptType={} parentId={} (回查获取ID={})", deptName.trim(), type, parentId,
                        created.getDeptId());
                return created.getDeptId();
            }
            log.error("[IMPORT-ERROR] 创建部门失败: name={}, parentId={}", deptName, parentId);
            return null;
        }
        log.info("[IMPORT-CREATE] 创建部门 {} deptType={} parentId={} id={}", deptName.trim(), type, parentId,
                newDept.getDeptId());
        return newDept.getDeptId();
    }

    private String getString(Map<String, String> map, String key) {
        String val = map.get(key);
        return val != null ? val : "";
    }

    private String getString(Map<String, String> map, String key, String defaultValue) {
        String val = map.get(key);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    @Override
    public int countStudentsByDeptId(Long deptId) {
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            TalkPermissionUtils.requireCollegeAccess(resolveCollegeDeptId(deptId));
        }
        return talkStudentMapper.countStudentsByDeptId(deptId);
    }

    @Override
    public List<TalkStudent> selectUntalkedStudents(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            params.put("collegeDeptId", TalkPermissionUtils.currentCollegeId());
        }
        return talkStudentMapper.selectUntalkedStudents(params);
    }

    @Override
    public List<TalkStudent> selectTalkStudentListWithLastTalk(TalkStudent talkStudent) {
        applyCollegeFilter(talkStudent);
        return talkStudentMapper.selectTalkStudentListWithLastTalk(talkStudent);
    }

    private void applyCollegeFilter(TalkStudent talkStudent) {
        TalkPermissionUtils.applyCollegeScope(talkStudent);
        Map<String, Object> params = talkStudent.getParams();
        params.remove("genderLabel");
        String gender = talkStudent.getGender();
        if (gender != null) {
            String label = Map.of("0", "男", "1", "女", "2", "未知").get(gender);
            if (label != null) params.put("genderLabel", label);
        }
        addLegacyFilterLabel(params, "politicalStatusLabel", "political_status", talkStudent.getPoliticalStatus());
        addLegacyFilterLabel(params, "enrollmentStatusLabel", "enrollment_status", talkStudent.getEnrollmentStatus());
        addLegacyFilterLabel(params, "mentalHealthStatusLabel", "mental_health_status", talkStudent.getMentalHealthStatus());
        addLegacyFilterLabel(params, "povertyLevelLabel", "poverty_level", talkStudent.getPovertyLevel());
    }

    /** 查询同时兼容旧导入的中文原值；别名仅由服务端当前字典生成。 */
    private void addLegacyFilterLabel(Map<String, Object> params, String key, String dictType, String value) {
        params.remove(key);
        if (StringUtils.isEmpty(value)) return;
        String label = DictUtils.getDictLabel(dictType, value);
        if (StringUtils.isNotEmpty(label)) params.put(key, label);
    }

    @Override
    public List<TalkStudent> selectUntalkedStudentsInPeriod(Date startTime, Date endTime, Long deptId,
            Map<String, Object> params) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (params == null) {
            params = new HashMap<>();
        }
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            params.put("collegeDeptId", TalkPermissionUtils.currentCollegeId());
        }
        return talkStudentMapper.selectUntalkedStudentsInPeriod(
                sdf.format(startTime), sdf.format(endTime), deptId, params);
    }

    @Override
    public List<TalkStudent> selectByTeacherCode(String teacherCode) {
        TalkTeacher query = new TalkTeacher();
        query.setTeacherCode(teacherCode);
        List<TalkTeacher> teachers = talkTeacherService.selectTalkTeacherList(query);
        if (teachers == null || teachers.isEmpty()) {
            throw new ServiceException("教师不存在");
        }
        return talkStudentMapper.selectByTeacherCode(teacherCode);
    }

    @Override
    public List<TalkStudent> selectByCollegeDeptId(Long deptId) {
        TalkPermissionUtils.requireCollegeAccess(deptId);
        return talkStudentMapper.selectByCollegeDeptId(deptId);
    }

    private void requireStudentAccess(TalkStudent student) {
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            TalkPermissionUtils.requireCollegeAccess(resolveCollegeDeptId(student.getDeptId()));
        }
    }

    private void requireClassDepartment(Long deptId) {
        SysDept department = deptId == null ? null : sysDeptMapper.selectDeptById(deptId);
        if (department == null || !"class".equals(department.getDeptType())) {
            throw new ServiceException("请选择有效班级，不能将学院或年级作为学生班级");
        }
    }

    private Long resolveCollegeDeptId(Long deptId) {
        Long currentId = deptId;
        for (int depth = 0; currentId != null && currentId > 0 && depth < 12; depth++) {
            SysDept dept = sysDeptMapper.selectDeptById(currentId);
            if (dept == null) {
                break;
            }
            if ("college".equals(dept.getDeptType())) {
                return dept.getDeptId();
            }
            currentId = dept.getParentId();
        }
        throw new ServiceException("学生所属部门未归属学院");
    }

    private Long resolveImportCollege(String collegeName) {
        if (TalkPermissionUtils.isGlobalAdmin()) {
            return findOrCreateDept(collegeName, TOP_DEPT_ID, "college");
        }
        Long currentCollegeId = TalkPermissionUtils.currentCollegeId();
        SysDept currentCollege = sysDeptMapper.selectDeptById(currentCollegeId);
        if (currentCollege == null || !currentCollege.getDeptName().equals(collegeName.trim())) {
            return null;
        }
        return currentCollegeId;
    }

    private Long resolveImportDept(String deptName, Long parentId, String type) {
        Long existing = findDept(deptName, parentId);
        if (existing != null) {
            return existing;
        }
        if (TalkPermissionUtils.isTeacher()) {
            return null;
        }
        return findOrCreateDept(deptName, parentId, type);
    }

    private Long findDept(String deptName, Long parentId) {
        SysDept query = new SysDept();
        query.setDeptName(deptName.trim());
        query.setParentId(parentId);
        List<SysDept> depts = sysDeptMapper.selectDeptList(query);
        if (depts != null) {
            for (SysDept dept : depts) {
                if (deptName.trim().equals(dept.getDeptName()) && parentId.equals(dept.getParentId())) {
                    return dept.getDeptId();
                }
            }
        }
        return null;
    }

}
