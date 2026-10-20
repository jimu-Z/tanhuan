# Five-round optimization implementation plan

> **For agentic workers:** Use executing-plans to implement this plan task-by-task. Each round requires two separate four-role test passes before completion.

**Goal:** Back up the current version, deliver five evidence-based improvements, verify all four live roles twice after every round, and write one complete Chinese optimization report without packaging deployment artifacts.

**Architecture:** Preserve the current Spring/Vue application and its four-role college boundaries. Fix business validation and resource safety at the existing service seams; supplement service regression tests with isolated runtime tests when local infrastructure is available.

**Tech Stack:** Existing Java/Spring/MyBatis/MySQL and Vue 2; no production dependency upgrades.

**Spec:** User objective in the active goal; domain and permissions in `RuoYi-Vue-master/CONTEXT.md`.

## Global constraints

- Roles: admin, school_admin, college_admin, talk_teacher. Position never grants permissions.
- Core scope: teachers, students, individual/group talks, records, attachments, tags, templates, unified query, statistics, department/class boundaries and exports.
- Do not package JAR/frontend artifacts or modify remote database/server data.
- Preserve current deployment files; test compilation is allowed.
- Each round: reproduce the issue, add regression coverage, implement, run two independent passes split by all four roles, record results and limitations.
- Back up source, configuration, SQL, spreadsheets and current deployment before changes. Dependency caches, old backups and runtime logs are rebuildable or already retained separately.

## Backup evidence

- `qa-runtime/optimization-20261012-135435/baseline.zip`, 1139 files, 88,301,369 bytes.
- `manifest.json` records SHA-256 per file; archive read-back verification passed.

## Review focus

- Teacher status/move/delete cannot bypass protected administrator rules or silently enable a disabled account.
- Record updates cannot change the authorized session/student relationship to reach another college.
- Talk creation rejects invalid type/date, duplicate participants and mismatched per-student input before any write.
- Attachment paths stay under the upload directory; multi-item deletion validates permissions before any physical file is removed.
- Batch tag queries deduplicate IDs, reject missing/unauthorized sessions and avoid one session query per ID.

## Rounds

- [x] 1. Teacher account consistency and administrator safeguards: `TalkTeacherServiceImpl`, teacher role/status regression tests.
- [x] 2. Record relationship integrity: `TalkStudentRecordServiceImpl`, session/student scope and immutable-link tests.
- [x] 3. Talk input validation: `TalkSessionServiceImpl`, request/domain validation and participant/date/type tests.
- [x] 4. Attachment path and batch deletion safety: shared attachment path resolver, attachment/session deletion services and filesystem regression tests.
- [x] 5. Batch tag performance and authorization: session mapper/service bulk authorization query, binding and query-count tests.

## Verification evidence

- Shared four-role regression matrix must cover core allowed operations and rejected cross-college/ownership/system-definition operations.
- Two passes per round use distinct fixture IDs/user names to avoid accidental hard-coded assumptions.
- Execute existing importer, Excel/DOCX export, frontend Chinese-label and teacher-form checks every round.
- Record commands, exit codes, test counts, role cases and runtime scope in `qa-runtime/optimization-20261012-135435/`.
- Local runtime infrastructure availability and actual endpoint coverage are reported honestly; unit tests alone do not prove full production operation.

## Final deliverable

- [x] Complete report with five before/after changes, backup/restore paths, each round's two-role-pass matrices, risks, runtime limits, unchanged deployment artifact hashes and recommended next test steps.
- [x] Completion audit against every explicit user requirement; keep goal active if any required verification is missing.
