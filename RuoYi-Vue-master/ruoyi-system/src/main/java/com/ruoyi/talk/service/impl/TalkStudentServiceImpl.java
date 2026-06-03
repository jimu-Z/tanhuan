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

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.service.ITalkStudentService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

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
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Autowired
    private TalkSessionMapper talkSessionMapper;

    private static final Long TOP_DEPT_ID = 100L;

    private static final String DEFAULT_PASSWORD = "changeAfterLogin123";

    /**
     * 查询学生信息管理
     * 
     * @param studentId 学生信息管理主键
     * @return 学生信息管理
     */
    @Override
    public TalkStudent selectTalkStudentByStudentId(Long studentId) {
        return talkStudentMapper.selectTalkStudentByStudentId(studentId);
    }

    /**
     * 查询学生信息管理列表
     * 
     * @param talkStudent 学生信息管理
     * @return 学生信息管理
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<TalkStudent> selectTalkStudentList(TalkStudent talkStudent) {
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
    public int updateTalkStudent(TalkStudent talkStudent) {
        talkStudent.setUpdateBy(SecurityUtils.getUsername());
        talkStudent.setUpdateTime(DateUtils.getNowDate());
        return talkStudentMapper.updateTalkStudent(talkStudent);
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
        for (Long studentId : studentIds) {
            int recordCount = talkStudentMapper.countRecordsByStudentId(studentId);
            if (recordCount > 0) {
                TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
                String name = student != null ? student.getStudentName() : "未知";
                throw new RuntimeException("学生 " + name + " 有 " + recordCount + " 条谈话记录，无法删除。请先处理相关谈话记录。");
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
        int recordCount = talkStudentMapper.countRecordsByStudentId(studentId);
        if (recordCount > 0) {
            TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
            String name = student != null ? student.getStudentName() : "未知";
            throw new RuntimeException("学生 " + name + " 有 " + recordCount + " 条谈话记录，无法删除。请先处理相关谈话记录。");
        }
        return talkStudentMapper.deleteTalkStudentByStudentId(studentId);
    }

    @Override
    public Map<String, Object> getStudentDetail(Long studentId) {
        Map<String, Object> result = new LinkedHashMap<>();
        TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
        if (student == null) {
            result.put("student", null);
            result.put("history", new ArrayList<>());
            return result;
        }
        result.put("student", student);
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
                    message = "学号已存在，将跳过此条记录";
                    warnCount++;
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

    /**
     * 执行Excel导入
     * 注意：每行独立处理，不使用全局事务，部分行失败不影响其他行
     */
    @Override
    public Map<String, Object> importExecute(List<Map<String, Object>> confirmedRows, String importMode) {
        Map<String, Object> result = new LinkedHashMap<>();
        int successCount = 0;
        int skipCount = 0;
        int userCreatedCount = 0;
        List<String> errors = new ArrayList<>();

        if (confirmedRows == null || confirmedRows.isEmpty()) {
            result.put("successCount", 0);
            result.put("skipCount", 0);
            result.put("userCreatedCount", 0);
            result.put("errorCount", 0);
            result.put("errors", errors);
            return result;
        }

        Map<String, Long> secretaryRoleCache = new HashMap<>();
        Map<String, Long> counselorRoleCache = new HashMap<>();
        Map<Long, Long> userRoleAssignedCache = new HashMap<>();

        for (int rowIdx = 0; rowIdx < confirmedRows.size(); rowIdx++) {
            Map<String, Object> rowMap = confirmedRows.get(rowIdx);
            int rowNum = rowIdx + 1;
            try {
                Map<String, String> rowData = new HashMap<>();
                for (Map.Entry<String, Object> entry : rowMap.entrySet()) {
                    rowData.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
                }
                log.debug("[IMPORT-DEBUG] Execute row {}: code={}, name={}, college={}",
                        rowNum, rowData.get("student_code"), rowData.get("student_name"), rowData.get("college"));
                String studentCode = rowData.get("student_code");
                String studentName = rowData.get("student_name");
                String college = rowData.get("college");
                String grade = rowData.get("grade");
                String className = rowData.get("class");

                if (StringUtils.isEmpty(studentCode) || StringUtils.isEmpty(studentName)) {
                    skipCount++;
                    errors.add("第" + rowNum + "行: 学号或姓名为空，已跳过");
                    continue;
                }

                String phone = rowData.get("phone");
                if (!StringUtils.isEmpty(phone) && !phone.matches("^1[3-9]\\d{9}$")) {
                    errors.add("第" + rowNum + "行: 学号" + studentCode + " 手机号格式错误: " + phone + "，已清空");
                    rowData.put("phone", "");
                    phone = "";
                }

                if (StringUtils.isEmpty(college) || StringUtils.isEmpty(grade) || StringUtils.isEmpty(className)) {
                    skipCount++;
                    errors.add("第" + rowNum + "行: 学院/年级/班级为空，已跳过");
                    continue;
                }

                Long collegeDeptId = findOrCreateDept(college, TOP_DEPT_ID, "college");
                Long gradeDeptId = findOrCreateDept(grade, collegeDeptId, "grade");
                Long classDeptId = findOrCreateDept(className, gradeDeptId, "class");

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

                if (isStudentCodeExists(studentCode)) {
                    if ("update".equals(importMode)) {
                        TalkStudent eq = new TalkStudent();
                        eq.setStudentCode(studentCode);
                        List<TalkStudent> existingList = talkStudentMapper.selectTalkStudentList(eq);
                        TalkStudent existing = existingList != null && !existingList.isEmpty() ? existingList.get(0)
                                : null;
                        if (existing != null) {
                            student.setStudentId(existing.getStudentId());
                            student.setCreateBy(existing.getCreateBy());
                            student.setCreateTime(existing.getCreateTime());
                            student.setUpdateTime(DateUtils.getNowDate());
                            talkStudentMapper.updateTalkStudent(student);
                            successCount++;
                            errors.add("第" + rowNum + "行: 学号" + studentCode + " 已存在，已更新");
                            continue;
                        }
                    }
                    skipCount++;
                    errors.add("第" + rowNum + "行: 学号" + studentCode + " 已存在，已跳过");
                    continue;
                }

                student.setCreateTime(DateUtils.getNowDate());
                Long gapId = talkStudentMapper.selectMinAvailableStudentId();
                if (gapId != null) {
                    student.setStudentId(gapId);
                }
                talkStudentMapper.insertTalkStudent(student);
                successCount++;

                String secretary = rowData.get("secretary");
                String viceSecretary = rowData.get("vice_secretary");
                String counselor = rowData.get("counselor");
                String headTeacher = rowData.get("head_teacher");

                if (!StringUtils.isEmpty(secretary)) {
                    if (createUserIfNotExists(secretary, collegeDeptId, "talk_secretary",
                            secretaryRoleCache, userRoleAssignedCache)) {
                        userCreatedCount++;
                    }
                }
                if (!StringUtils.isEmpty(viceSecretary)) {
                    if (createUserIfNotExists(viceSecretary, collegeDeptId, "talk_secretary",
                            secretaryRoleCache, userRoleAssignedCache)) {
                        userCreatedCount++;
                    }
                }
                if (!StringUtils.isEmpty(counselor)) {
                    if (createUserIfNotExists(counselor, collegeDeptId, "talk_counselor",
                            counselorRoleCache, userRoleAssignedCache)) {
                        userCreatedCount++;
                    }
                }
                if (!StringUtils.isEmpty(headTeacher)) {
                    if (createUserIfNotExists(headTeacher, collegeDeptId, "talk_counselor",
                            counselorRoleCache, userRoleAssignedCache)) {
                        userCreatedCount++;
                    }
                }
            } catch (Exception e) {
                log.error("[IMPORT-ERROR] 第{}行导入失败: {}", rowNum, e.getMessage(), e);
                errors.add("第" + rowNum + "行: 导入失败 - " + e.getMessage());
            }
        }

        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("userCreatedCount", userCreatedCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        return result;
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

        return data;
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
        List<TalkStudent> list = talkStudentMapper.selectTalkStudentList(query);
        return list != null && !list.isEmpty();
    }

    private Long findOrCreateDept(String deptName, Long parentId, String type) {
        log.debug("[IMPORT-DEBUG] findOrCreateDept: name={}, parentId={}, type={}", deptName, parentId, type);
        SysDept query = new SysDept();
        query.setDeptName(deptName.trim());
        query.setParentId(parentId);
        List<SysDept> depts = sysDeptMapper.selectDeptList(query);
        if (depts != null && !depts.isEmpty()) {
            for (SysDept d : depts) {
                if (d.getDeptName().equals(deptName.trim()) && d.getParentId().equals(parentId)) {
                    return d.getDeptId();
                }
            }
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
        newDept.setOrderNum(1);
        newDept.setStatus("0");
        newDept.setDelFlag("0");
        newDept.setCreateBy(SecurityUtils.getUsername());
        newDept.setCreateTime(new Date());
        sysDeptMapper.insertDept(newDept);
        return newDept.getDeptId();
    }

    private boolean createUserIfNotExists(String name, Long deptId, String roleKey,
            Map<String, Long> roleCache, Map<Long, Long> userRoleAssignedCache) {
        String userName = name.trim().replaceAll("\\s+", "");

        SysUser existingUser = sysUserMapper.selectUserByUserName(userName);
        if (existingUser != null) {
            Long roleId = resolveRoleId(roleKey, roleCache);
            if (roleId != null && !isUserHasRole(existingUser.getUserId(), roleId)) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(existingUser.getUserId());
                userRole.setRoleId(roleId);
                sysUserRoleMapper.batchUserRole(List.of(userRole));
            }
            return false;
        }

        SysUser newUser = new SysUser();
        newUser.setUserName(userName);
        newUser.setNickName(name.trim());
        newUser.setPassword(SecurityUtils.encryptPassword(DEFAULT_PASSWORD));
        newUser.setDeptId(deptId);
        newUser.setStatus("0");
        newUser.setDelFlag("0");
        newUser.setCreateBy(SecurityUtils.getUsername());
        newUser.setCreateTime(new Date());
        sysUserMapper.insertUser(newUser);

        Long roleId = resolveRoleId(roleKey, roleCache);
        if (roleId != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(newUser.getUserId());
            userRole.setRoleId(roleId);
            sysUserRoleMapper.batchUserRole(List.of(userRole));
        }

        return true;
    }

    private Long resolveRoleId(String roleKey, Map<String, Long> roleCache) {
        if (roleCache.containsKey(roleKey)) {
            return roleCache.get(roleKey);
        }
        SysRole query = new SysRole();
        query.setRoleKey(roleKey);
        List<SysRole> roles = sysRoleMapper.selectRoleList(query);
        if (roles != null && !roles.isEmpty()) {
            Long roleId = roles.get(0).getRoleId();
            roleCache.put(roleKey, roleId);
            return roleId;
        }
        roleCache.put(roleKey, null);
        return null;
    }

    private boolean isUserHasRole(Long userId, Long roleId) {
        List<Long> roleIds = sysRoleMapper.selectRoleListByUserId(userId);
        return roleIds != null && roleIds.contains(roleId);
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
        return talkStudentMapper.countStudentsByDeptId(deptId);
    }

    @Override
    public List<TalkStudent> selectUntalkedStudents(Map<String, Object> params) {
        return talkStudentMapper.selectUntalkedStudents(params);
    }

}
