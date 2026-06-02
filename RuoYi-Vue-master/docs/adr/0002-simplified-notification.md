# ADR-0002: 简化通知机制

## 日期

2026-06-01

## 状态

已采纳

## 背景

系统需要双向通知能力：
- 老师发起/编辑谈话后通知学生
- 学生提交反馈后通知老师

需要决定通知机制的数据建模方案。

## 决策

**采用 `talk_student_record` 表的标记字段简化实现**（`notified` / `teacher_notified`），不建立独立通知表。

## 考虑的方案

| 方案 | 描述 |
|------|------|
| A. 独立通知表 `talk_notification` | 完整的通知表，支持通知类型、已读/未读、通知内容等字段 |
| B. 记录表标记字段（采纳） | 在 `talk_student_record` 增加 `notified` 和 `teacher_notified` 字段 |
| C. 复用若依 `sys_notice` | 将学生反馈创建为系统通知公告 |

## 理由

1. **业务耦合紧密**：本系统通知与谈话记录严格一一对应，通知的生命周期与记录一致，直接挂在记录上语义清晰
2. **实现简单**：新增两个布尔字段，查询"待处理"只需 `WHERE student_feedback = '无' AND notified = 1`，无需 JOIN
3. **低开销**：方案 A 需要额外的表、CRUD 接口和前端通知中心，当前业务规模下过度设计
4. 方案 C 的 `sys_notice` 面向全体用户公告，不适合个人化消息

## 风险与未来演进

- 若未来需要更多通知类型（如系统公告、预警提醒等），可从标记字段方案迁移到独立通知表
- 标记字段不记录通知时间、不区分已读，若需要这些能力应重新评估方案 A

## 影响

- `talk_student_record` 表新增 `notified` TINYINT(1) DEFAULT 0 和 `teacher_notified` TINYINT(1) DEFAULT 0 两个字段
- 教师端查询"待查看反馈"：`student_feedback != '无' AND teacher_notified = 0`
- 学生端查询"待处理谈话"：`student_feedback = '无' AND notified = 1`
