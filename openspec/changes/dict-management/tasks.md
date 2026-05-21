## 1. 后端 - 字典类型管理

- [ ] 1.1 DictTypeMapper 添加 insert、update、deleteById 方法
- [ ] 1.2 DictTypeService 添加 createType、updateType、deleteType 方法
- [ ] 1.3 DictTypeController 添加 POST、PUT、DELETE 接口

## 2. 后端 - 字典项管理

- [ ] 2.1 DictCodeMapper 添加 insert、update、deleteById、findByDictTypeId 方法
- [ ] 2.2 DictCodeService 添加 createItem、updateItem、deleteItem、getItemsByType 方法
- [ ] 2.3 DictCodeController 添加 POST、PUT、DELETE 接口

## 3. 后端 - 缓存刷新

- [ ] 3.1 修改 DictTypeService.deleteType 添加删除前检查（是否有子项）
- [ ] 3.2 修改创建/更新/删除方法后调用 refreshCache()

## 4. 前端 - 页面和组件

- [ ] 4.1 新增 src/views/admin/Dict.vue 页面（左右布局：类型列表 + 字典项表格）
- [ ] 4.2 字典类型列表支持新增/编辑/删除
- [ ] 4.3 字典项表格支持新增/编辑/删除（新增时自动关联当前选中类型）
- [ ] 4.4 字典项删除前二次确认

## 5. 前端 - API 和菜单

- [ ] 5.1 新增 src/api/dict.js API 模块
- [ ] 5.2 路由添加 /admin/dicts 路由
- [ ] 5.3 侧边菜单添加"业务字典"菜单项
