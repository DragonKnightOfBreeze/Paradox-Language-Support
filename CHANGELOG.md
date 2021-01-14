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
* [X] 修复索引的相关bug
* [X] 添加配置`renderLineCommentText`
* [X] 尝试得到`definitionInfo`时，`element`必须是`scriptProperty`且`value`必须是`scriptBlock`
* [X] 解析`localisationCommandKey`
* [X] 让`PsiNamedElement`也实现`PsiCheckNameElement`
* [X] 改为使用自定义的`com.windea.plugin.idea.paradox.core.psi.PsiCheckRenameElement`，整理目录
* [X] 兼容`\u00a0`的空格
* [X] 解决`commandKey`无法自动补全的bug（lexer不够完善，没有兼容空格的情况？）
* [X] 解析规则文件`definitions.yml`的规则`value_type`
* [X] 解析规则文件`definitions.yml`的规则`name_from_value`
* [X] 为`types.yml`规则文件的schema补充描述。
* [X] 为`locations.yml`规则文件的schema补充描述。
* [X] 调试可以自动提示commandKey，但暂时不能自动提示localisation，操作被取消（？？？）
* [X] 移除标准库后，调试可以自动提示localisation，怀疑是调试环境性能原因
* [X] 修复规则文件`locations.yml`解析代码中的bug
* [X] 改为从规则文件读取枚举数据
* [X] 为本地化文件提示`locale` `serialNumber` `colorCode` `commandScope` `commandField`
* [ ] 将`serialNumber`重命名为`sequentialNumber`
* [ ] 为规则文件`definitions.yml`添加规则`name_prefix`和`name_suffix`（完成`common`目录的）
* [ ] 为规则文件`definitions.yml`添加规则`icon`
* [ ] 解析规则文件`definitions.yml`的规则`predicate`
* [ ] 解析规则文件`definitions.yml`的规则`subTypes`
* [ ] 解析规则文件`definitions.yml`的规则`type_from_file`
* [ ] 解析规则文件`definitions.yml`的规则`type_per_file`
* [ ] 编写规则文件`enums.yml`
* [ ] 修复图标能加载但不能正常渲染的bug（IDE新版本底层代码错误？）
* [ ] 初步实现功能：添加模块（基于游戏类型）
* [ ] 让`scriptRootBlock`直接或间接继承自`scriptProperty`

# TODO

* [ ] `commandKey`、`localisationProperty`为什么不能进行自定代码补全？为什么一个不调用，另一个`processCanceled`？
* [ ] 整合来自cwtools的规则文件，更改扩展名和文件格式，加入到项目的rules目录下
