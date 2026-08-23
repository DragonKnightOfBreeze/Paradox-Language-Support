@file:Suppress("unused")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.model.type.CwtExpressionType

// region CwtMemberConfig Accessors

/** 将值解析为布尔值。如果值类型非 [CwtExpressionType.Boolean]，则返回 `null`。 */
val CwtMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtExpressionType.Boolean) value.toBooleanYesNo() else null

/** 将值解析为整数。如果值类型非 [CwtExpressionType.Int] 或解析失败，则返回 `null`。 */
val CwtMemberConfig<*>.intValue: Int? get() = if (valueType == CwtExpressionType.Int) value.toIntOrNull() ?: 0 else null

/** 将值解析为浮点数。如果值类型非 [CwtExpressionType.Float] 或解析失败，则返回 `0f`）。 */
val CwtMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtExpressionType.Float) value.toFloatOrNull() ?: 0f else null

/** 将值解析为字符串。如果值类型非 [CwtExpressionType.String]，则返回 `null`。 */
val CwtMemberConfig<*>.stringValue: String? get() = if (valueType == CwtExpressionType.String) value else null

/** 如果当前成员规则对应属性的值，则返回所属的属性规则。否则返回自身。 */
val CwtMemberConfig<*>.containingDirectConfig: CwtMemberConfig<*> get() = castOrNull<CwtValueConfig>()?.propertyConfig ?: this

/** 绑定到当前属性规则的别名规则（解析阶段填充）。 */
var CwtPropertyConfig.aliasConfig: CwtAliasConfig? by registerKey(CwtMemberConfig.Keys)

/** 绑定到当前属性规则的单别名规则（解析阶段填充）。 */
var CwtPropertyConfig.singleAliasConfig: CwtSingleAliasConfig? by registerKey(CwtMemberConfig.Keys)

/** 绑定到当前属性规则的内联规则（解析阶段填充）。 */
var CwtPropertyConfig.inlineConfig: CwtMacroConfig.InlineScript? by registerKey(CwtMemberConfig.Keys)

/** 当前值规则的标签类型（解析阶段推断，用于渲染和提示）。 */
var CwtValueConfig.tagType: ParadoxTagType? by registerKey(CwtMemberConfig.Keys)

// endregion

// region CwtOptionMemberConfig Accessors

/** 将选项值解析为布尔值。如果值类型非 [CwtExpressionType.Boolean]，则返回 `null`。 */
val CwtOptionMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtExpressionType.Boolean) value.toBooleanYesNo() else null

/** 将选项值解析为整数。如果值类型非 [CwtExpressionType.Int] 或解析失败，则返回 `null`。 */
val CwtOptionMemberConfig<*>.intValue: Int? get() = if (valueType == CwtExpressionType.Int) value.toIntOrNull() ?: 0 else null

/** 将选项值解析为浮点数。如果值类型非 [CwtExpressionType.Float] 或解析失败，则返回 `0f`）。 */
val CwtOptionMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtExpressionType.Float) value.toFloatOrNull() ?: 0f else null

/** 将选项值解析为字符串。如果值类型非 [CwtExpressionType.String]，则返回 `null`。 */
val CwtOptionMemberConfig<*>.stringValue: String? get() = if (valueType == CwtExpressionType.String) value else null

// endregion

// region CwtLinkConfig Accessors

/** 是否为静态链接。 */
val CwtLinkConfig.isStatic: Boolean get() = dataSources.isEmpty()

/** 使用函数调用形式时采用的前缀（作为函数名）。 */
val CwtLinkConfig.prefixFromArgument: String? get() = prefix?.removeSuffix(":")

// endregion
