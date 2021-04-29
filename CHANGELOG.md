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
* [X] 验证`commandScope` `commandField`
* [X] 修复不能changeLocale之类的bug
* [X] 完善对于`commandScope`和`commandField`的提示和验证
* [X] 解析规则文件`types.yml`的规则`predicate`
* [X] 解析规则文件`types.yml`的规则`subTypes`
* [X] 将`serialNumber`重命名为`sequentialNumber`
* [X] 实现`ParadoxScriptExpressionTypeProvider`
* [X] 实现`ParadoxPathReferenceProvider`
* [X] 修复`InvalidFileEncodingInspection`中的NPE
* [X] 完成规则文件`types.yml`
* [X] 改为根据正则指定需要排除的脚本文件
* [X] 提高脚本文件语法兼容性
* [X] 本地化文件使用正确的`localisationColor`
* [X] 添加`com.windea.plugin.idea.paradox.script.psi.ParadoxDefinitionTypeIndex`
* [X] 提高脚本文件和本地化文件的语法兼容性
* [X] 本地化文件渲染`propertyReference`时，如果有颜色参数，即使`propertyReference`未解析或者是变量，仍然正确渲染颜色，保留颜色参数
* [X] 实现本地化属性的`CopyRawTextIntention`、`CopyPlainTextIntention`、`CopyRichTextIntention`
* [X] `DefinitionInfo`改为`TypeInfo`
* [X] `TypeInfo`改为`Definition`，支持解析`alias`，部分提取buildString的扩展
* [X] 更新项目文档和说明
* [X] 脚本文件支持根据规则文件提示propertyName（未测试，规则文件未完成）
* [X] 脚本文件支持根据规则文件提示propertyName，处理不能重复的情况（未测试，规则文件未完成）
* [X] 脚本文件支持根据规则文件提示propertyName，初步实现（已测试，规则文件未完成）
* [X] 脚本文件支持根据规则文件验证propertyName（未测试，规则文件未完成）
* [ ] 为规则文件`types.yml`添加规则`name_prefix`和`name_suffix`（已完成`common`目录）
* [ ] 为规则文件`types.yml`添加规则`icon`
* [ ] 解析规则文件`types.yml`的规则`type_from_file`
* [ ] 解析规则文件`types.yml`的规则`type_per_file`
* [ ] 编写规则文件`enums.yml`
* [ ] 修复图标能加载但不能正常渲染的bug（IDE新版本底层代码错误？）
* [ ] 初步实现功能：添加模块（基于游戏类型）
* [ ] 让`scriptRootBlock`直接或间接继承自`scriptProperty`

### 0.2

* 更新IDEA版本到2021.1
* 修复类加载错误

### 0.3

* 初步支持cwt config语言
* 更新并统一文件图标
* 解析cwt config语言的optionComment和documentComment
* 修复格式化功能的BUG
* 初步实现`CwtConfigResolver`

# TODO

* [ ] 补充stellaris的规则文件
* [ ] 提供接口支持所有paradox游戏
