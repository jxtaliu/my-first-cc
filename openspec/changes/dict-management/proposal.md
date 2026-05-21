## Why

目前系统已有业务字典表结构和查询功能，但缺少管理维护界面。管理员需要在后台对业务字典进行 CRUD 操作，包括新增字典类型、新增/编辑/删除字典项，以及按类型查看字典列表。

## What Changes

- 新增字典类型管理接口（CRUD）
- 新增字典项管理接口（CRUD）
- 修改字典后自动清理 Redis 缓存
- 前端新增"业务字典"管理菜单
- 字典类型列表页面（支持新增/编辑/删除）
- 字典项管理页面（左侧类型树，右侧字典项列表，支持 CRUD）

## Capabilities

### New Capabilities
- `dict-type`: 字典类型管理 - 支持 CRUD 操作
- `dict-item`: 字典项管理 - 支持 CRUD 操作，按类型筛选

### Modified Capabilities
- （无）

## Impact

- 后端：新增 DictTypeMapper、DictCodeMapper 的完整 CRUD 方法
- 后端：DictService 新增管理接口，修改后刷新缓存
- 前端：新增管理页面，菜单结构调整
- 数据库：sys_dict_type、sys_dict_code 表已有，无需变更
