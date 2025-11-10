package icu.windea.pls.config.configGroup

import icu.windea.pls.config.config.delegated.CwtLinkConfig

/** 用于获取符合特定条件的链接规则。 */
data class LinksModel(
    /** 变量对应的链接规则的列表。 */
    val variable: MutableList<CwtLinkConfig> = mutableListOf(),
    val forScopeStatic: MutableList<CwtLinkConfig> = mutableListOf(),
    val forScopeFromArgumentSorted: MutableList<CwtLinkConfig> = mutableListOf(),
    val forScopeFromDataSorted: MutableList<CwtLinkConfig> = mutableListOf(),
    val forScopeFromDataNoPrefixSorted: MutableList<CwtLinkConfig> = mutableListOf(),
    val forValueStatic: MutableList<CwtLinkConfig> = mutableListOf(),
    val forValueFromArgumentSorted: MutableList<CwtLinkConfig> = mutableListOf(),
    val forValueFromDataSorted: MutableList<CwtLinkConfig> = mutableListOf(),
    val forValueFromDataNoPrefixSorted: MutableList<CwtLinkConfig> = mutableListOf(),
)

/** 用于获取符合特定条件的定义类型。 */
data class DefinitionTypesModel(
    /** 必定支持作用域的定义类型。 */
    val supportScope: MutableSet<String> = mutableSetOf(),
    /** 必定间接支持作用域的定义类型。 */
    val indirectSupportScope: MutableSet<String> = mutableSetOf(),
    /** 不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）。 */
    val skipCheckSystemScope: MutableSet<String> = mutableSetOf(),
    /** 支持参数的定义类型。 */
    val supportParameters: MutableSet<String> = mutableSetOf(),
    /** 可能有类型键前缀（type_key_prefix）的定义类型 - 按文件路径计算。 */
    val mayWithTypeKeyPrefix: MutableSet<String> = mutableSetOf(),
)
