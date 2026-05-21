# 通用字典码配置系统设计规格

> **创建日期**: 2026-05-21
> **状态**: 设计中
> **目标**: 实现可扩展的通用字典码配置系统

## 1. 概述

### 1.1 背景
当前系统中存在多种枚举类型（任务状态、优先级、项目类型等），这些枚举硬编码在代码中，添加新值需要修改代码并重新部署。本设计旨在实现一个通用的字典码配置系统，支持动态配置枚举值，无需修改代码即可扩展。

### 1.2 目标
- 提供统一的字典码管理机制
- 支持多种类型的字典码配置
- 前后端共享统一的字典数据
- 缓存优化，减少数据库查询
- 添加新字典码无需修改代码

## 2. 数据库设计

### 2.1 字典类型表 (sys_dict_type)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| code | VARCHAR(50) | 字典代码，唯一标识，如 'task_status', 'priority' |
| name | VARCHAR(100) | 字典名称 |
| description | VARCHAR(255) | 描述 |
| created_at | DATETIME | 创建时间 |

### 2.2 字典码表 (sys_dict_code)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| dict_type_id | BIGINT | 关联的字典类型ID |
| code | VARCHAR(50) | 字典码值，如 'TODO', 'HIGH' |
| name | VARCHAR(100) | 名称（通用） |
| name_en | VARCHAR(100) | 英文名称 |
| name_zh | VARCHAR(100) | 中文名称 |
| sort_order | INT | 排序号 |
| extra | VARCHAR(500) | 扩展字段（JSON格式，存储颜色、图标等配置） |
| created_at | DATETIME | 创建时间 |

### 2.3 初始数据

**字典类型:**
- task_status: 任务状态
- task_type: 任务类型
- priority: 优先级
- project_type: 项目类型
- project_status: 项目状态

**任务状态 (task_status):**
| code | name_en | name_zh | extra |
|------|---------|---------|-------|
| TODO | Todo | 待办 | {"color": "#909399"} |
| IN_PROGRESS | In Progress | 进行中 | {"color": "#E6A23C"} |
| IN_REVIEW | In Review | 审核中 | {"color": "#409EFF"} |
| DONE | Done | 已完成 | {"color": "#67C23A"} |

**任务类型 (task_type):**
| code | name_en | name_zh |
|------|---------|---------|
| EPIC | Epic | 史诗 |
| FEATURE | Feature | 特性 |
| STORY | Story | 故事 |
| SUBTASK | Sub-task | 子任务 |

**优先级 (priority):**
| code | name_en | name_zh | extra |
|------|---------|---------|-------|
| LOW | Low | 低 | {"color": "#909399"} |
| MEDIUM | Medium | 中 | {"color": "#E6A23C"} |
| HIGH | High | 高 | {"color": "#F56C6C"} |
| URGENT | Urgent | 紧急 | {"color": "#F56C6C"} |

**项目类型 (project_type):**
| code | name_en | name_zh |
|------|---------|---------|
| SCRUM | Scrum | Scrum敏捷 |
| KANBAN | Kanban | 看板 |

**项目状态 (project_status):**
| code | name_en | name_zh | extra |
|------|---------|---------|-------|
| PLANNING | Planning | 规划中 | {"color": "#909399"} |
| ACTIVE | Active | 进行中 | {"color": "#67C23A"} |
| COMPLETED | Completed | 已完成 | {"color": "#409EFF"} |
| ARCHIVED | Archived | 已归档 | {"color": "#909399"} |

## 3. API 设计

### 3.1 接口列表

| 路径 | 方法 | 描述 |
|------|------|------|
| `/api/dicts` | GET | 获取所有字典类型 |
| `/api/dicts/{type}` | GET | 获取指定类型的所有字典码 |
| `/api/dicts/{type}/{code}` | GET | 获取指定字典码详情 |

### 3.2 响应格式

**GET /api/dicts/{type}**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "code": "TODO",
      "name": "Todo",
      "nameEn": "Todo",
      "nameZh": "待办",
      "sortOrder": 1,
      "extra": {"color": "#909399"}
    }
  ]
}
```

## 4. 缓存设计

### 4.1 Redis 缓存

```
Key: dict:{type}  (如 dict:task_status)
Value: JSON数组
TTL: 3600秒 (1小时)
```

### 4.2 缓存策略

- 系统启动时预加载所有字典到缓存
- 字典服务提供刷新缓存接口
- 支持手动刷新缓存

## 5. 后端实现

### 5.1 目录结构

```
src/main/java/com/sme/pm/
├── entity/
│   ├── DictType.java
│   └── DictCode.java
├── mapper/
│   ├── DictTypeMapper.java
│   └── DictCodeMapper.java
├── service/
│   ├── DictService.java
│   └── impl/DictServiceImpl.java
├── controller/
│   └── DictController.java
└── config/
    └── RedisConfig.java
```

### 5.2 核心类设计

**DictType**
- id, code, name, description, createdAt

**DictCode**
- id, dictTypeId, code, name, nameEn, nameZh, sortOrder, extra, createdAt

**DictService**
- `List<DictType> getAllTypes()` - 获取所有字典类型
- `List<DictCode> getCodesByType(String type)` - 获取指定类型的字典码
- `DictCode getCode(String type, String code)` - 获取指定字典码
- `void refreshCache()` - 刷新缓存

## 6. 前端集成

### 6.1 API 调用

```javascript
// 获取任务状态
const taskStatuses = await request.get('/dicts/task_status')

// 渲染
taskStatuses.forEach(status => {
  console.log(status.code, status.nameZh, status.extra.color)
})
```

### 6.2 i18n 翻译

```json
{
  "dict": {
    "task_status": {
      "TODO": "待办",
      "IN_PROGRESS": "进行中",
      "IN_REVIEW": "审核中",
      "DONE": "已完成"
    }
  }
}
```

## 7. 使用流程

### 7.1 添加新字典类型

1. 插入字典类型记录
2. 插入对应的字典码记录
3. 调用刷新缓存接口（或等待自动过期）

### 7.2 添加新字典码

1. 插入字典码记录
2. 调用刷新缓存接口

### 7.3 业务中使用

```java
// 后端返回字符串，前端直接使用
task.setStatus("TODO");

// 或获取字典信息
DictCode status = dictService.getCode("task_status", "TODO");
```

## 8. 扩展性

未来可扩展的方向：
- 字典码分组（父子关系）
- 字典码变更历史记录
- 字典码使用统计
- 多租户支持

## 9. 注意事项

- 字典码一旦被业务数据引用，不应删除，只可禁用
- 扩展字段仅存储展示相关的配置，不要存储业务逻辑
- 缓存刷新时需考虑并发影响
