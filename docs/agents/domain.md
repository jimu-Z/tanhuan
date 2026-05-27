# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Before exploring, read these

- **`CONTEXT.md`** at the repo root
- **`docs/adr/`** — read ADRs that touch the area you're about to work in.
- **`设计决策文档.md`** at the workspace root (`RuoYi-Vue-master/../`)

If any of these files don't exist, **proceed silently**. Don't flag their absence; don't suggest creating them upfront.

## File structure

Single-context repo:

```
/
├── CONTEXT.md
├── 设计决策文档.md
├── docs/
│   ├── adr/
│   └── agents/
└── src/ (RuoYi-Vue-master/)
```

## Use the glossary's vocabulary

When your output names a domain concept (in an issue title, a refactor proposal, a hypothesis, a test name), use the term as defined in `CONTEXT.md`. Don't drift to synonyms the glossary explicitly avoids.

Key domain terms:
- **谈话记录 (Conversation)** — 辅导员与学生之间的一次正式谈话，创建时对学生信息做快照
- **跟进记录 (FollowUp)** — 谈话后的跟进操作记录，与谈话记录一对多
- **辅导员 (Counselor)** — 负责管理特定班级学生的一线用户角色
- **学院书记/副书记 (College Leader)** — 可查看本学院全部数据的学院级管理角色
- **快照 (Snapshot)** — 谈话记录创建时从学生表拷贝的当时信息，学生表后续变更不影响历史记录
- **软删除** — 谈话记录删除时将 `del_flag` 设为 `2`，数据保留在库中

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> _Contradicts ADR-0007 — but worth reopening because…_