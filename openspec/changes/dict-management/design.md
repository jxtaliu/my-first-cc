## Context

系统已有业务字典基础表结构（sys_dict_type、sys_dict_code），但只有只读查询接口。本次需要实现完整的管理维护功能，包括字典类型和字典项的增删改查。

现有数据已包含 task_status、task_type、priority、project_type、project_status 五类字典数据。

## Goals / Non-Goals

**Goals:**
- 提供字典类型的完整 CRUD 管理
- 提供字典项的完整 CRUD 管理
- 修改字典数据后自动清理 Redis 缓存，确保数据一致性
- 前端在"管理"菜单下新增业务字典菜单项

**Non-Goals:**
- 不涉及字典数据的导入/导出功能
- 不涉及字典权限细化（暂时全管理员可管理）
- 不涉及多语言国际化界面

## Decisions

**1. 后端采用 RESTful API 设计**

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/dict/types | GET | 查询所有字典类型 |
| /api/dict/types | POST | 创建字典类型 |
| /api/dict/types/:id | GET | 获取单个字典类型 |
| /api/dict/types/:id | PUT | 更新字典类型 |
| /api/dict/types/:id | DELETE | 删除字典类型 |
| /api/dict/items | GET | 查询字典项（支持 typeId 过滤） |
| /api/dict/items | POST | 创建字典项 |
| /api/dict/items/:id | GET | 获取单个字典项 |
| /api/dict/items/:id | PUT | 更新字典项 |
| /api/dict/items/:id | DELETE | 删除字典项 |

**2. 缓存清理策略**

修改（新增/更新/删除）字典类型或字典项时，调用 `refreshCache()` 清理该类型的所有缓存。避免手动刷新遗漏。

**3. 前端页面设计**

采用左右布局：
- 左侧：字典类型树形列表
- 右侧：选中类型的字典项表格

支持在右侧直接新增字典项（自动关联当前选中类型）。

**4. 删除保护**

删除字典类型时，如果该类型下存在字典项，提示用户先删除关联的字典项。

## Risks / Trade-offs

[Risk] 删除字典类型时存在数据关联 → [Mitigation] 后端校验并返回友好提示

[Risk] 并发修改字典数据 → [Mitigation] 使用数据库事务保证一致性

[Risk] 前端未刷新列表 → [Mitigation] 操作后重新获取列表数据
