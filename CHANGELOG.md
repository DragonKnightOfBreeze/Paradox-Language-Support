# 版本

## 0.x

### 0.1

* [X] 从模块`stellaris-modding-support`拷贝文件到该模块
* [X] 文件名和文件内容的重命名
* [X] 重构`intentions`
* [X] 初步实现功能：添加库（基于游戏类型，包括游戏标准库）
* [X] 初步实现功能：库会显示在`External Libraries`中（自动适用，但是图标是默认的）
* [X] 初步实现功能：基于库的索引（自动适用，但需要进行一些调整？）
* [X] 修复外部引用能导航但不能正常渲染文档注释的bug
* [X] 解析`propertyPath`
* [X] 编写规则文件`types.yml`
* [X] 脚本文件兼容不等号`<>`
* [X] 解析`typeMetadata`的`name` `type` `localisation` `scope` `fromVersion`，TODO：`subTypes`
* [X] 解决`scriptProperty.paradoxTypeMetadata.name`和`scriptProperty.name`的兼容性问题
* [X] 编写规则文件`locations.txt`并应用
* [X] `typeMetadata`重命名为`definitionInfo`
* [X] 修复索引的相关bug（不保证索引的key完全正确）
* [X] 修复`00_edicts.txt`无限重复解析的bug（应当是scriptVariable索引的问题）
* [X] definition文档中列出definitionLocalisation
* [X] definition文档中列出definitionLocalisation并且绑定psi链接
* [ ] 为规则文件`definitions.yml`添加规则`name_prefix`
* [ ] 解析`definitionInfo`的`subTypes`
* [ ] 编写规则文件`enums.yml`
* [ ] 编写规则文件`definitions.yml`
* [ ] 修复图标能加载但不能正常渲染的bug（IDE新版本底层代码错误？）
* [ ] 初步实现功能：添加模块（基于游戏类型）

# TODO

* 整合来自cwtools的规则文件，更改扩展名和文件格式，加入到项目的rules目录下
