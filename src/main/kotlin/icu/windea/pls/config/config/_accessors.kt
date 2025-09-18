@file:Suppress("unused")

package icu.windea.pls.config.config

import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.util.data.CwtOptionDataAccessor
import icu.windea.pls.config.util.data.CwtOptionDataAccessors
import icu.windea.pls.config.util.data.CwtOptionFlags
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.model.CwtType

// region CwtMemberConfig Accessors

/**
 * 将规则右值解析为布尔值（当且仅当值类型为 Boolean 时）。
 *
 * - 源格式：`yes`/`no`
 * - 否则返回 `null`。
 */
val CwtMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null

/**
 * 将规则右值解析为整数（当且仅当值类型为 Int 时）。
 *
 * - 解析失败时返回 `0`；否则返回解析结果。
 * - 非 Int 类型返回 `null`。
 */
val CwtMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null

/** 将规则右值解析为浮点数（当且仅当值类型为 Float 时，解析失败时返回 `0f`）。*/
val CwtMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null

/** 当值类型为 String 时返回规则右值，否则返回 `null`。*/
val CwtMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null

/** 过滤并返回当前块下的值规则列表（`CwtValueConfig`）。*/
val CwtMemberConfig<*>.values: List<CwtValueConfig>? get() = configs?.filterIsInstance<CwtValueConfig>()

/** 过滤并返回当前块下的属性规则列表（`CwtPropertyConfig`）。*/
val CwtMemberConfig<*>.properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstance<CwtPropertyConfig>()

/** 通过 [CwtOptionFlags] 获取选项标志。 */
val CwtMemberConfig<*>.optionFlags: CwtOptionFlags get() = CwtOptionFlags.from(this)

/** 通过 [CwtOptionDataAccessor] 获取选项数据。 */
fun <T> CwtMemberConfig<*>.optionData(accessor: CwtOptionDataAccessor<T>): T = accessor.get(this)
/** 通过 [CwtOptionDataAccessor] 获取选项数据。 */
inline fun <T> CwtMemberConfig<*>.optionData(accessorGetter: CwtOptionDataAccessors.() -> CwtOptionDataAccessor<T>): T = optionData(CwtOptionDataAccessors.accessorGetter())

/**
 * 绑定到属性规则的 Single Alias 扩展规则。
 *
 * - 解析阶段填充，用于溯源/快速文档等。
 */
var CwtPropertyConfig.singleAliasConfig: CwtSingleAliasConfig? by createKey(CwtMemberConfig.Keys)

/** 绑定到属性规则的 Alias 扩展规则（解析阶段填充）。*/
var CwtPropertyConfig.aliasConfig: CwtAliasConfig? by createKey(CwtMemberConfig.Keys)

/** 绑定到属性规则的 Inline 扩展规则（解析阶段填充）。*/
var CwtPropertyConfig.inlineConfig: CwtInlineConfig? by createKey(CwtMemberConfig.Keys)

/** 值规则的标签类型（由规则解析阶段推断，用于渲染/提示）。*/
var CwtValueConfig.tagType: CwtTagType? by createKey(CwtMemberConfig.Keys)

/** 被覆写前的原始规则（用于“规则覆盖/来源追踪”场景）。*/
var CwtMemberConfig<*>.originalConfig: CwtMemberConfig<CwtMemberElement>? by createKey(CwtMemberConfig.Keys)

/** 对应的覆写提供者（来源于可覆写的规则分组）。*/
var CwtMemberConfig<*>.overriddenProvider: CwtOverriddenConfigProvider? by createKey(CwtMemberConfig.Keys)

/** 声明类规则的上下文信息（用于声明/定义相关能力）。*/
var CwtMemberConfig<*>.declarationConfigContext: CwtDeclarationConfigContext? by createKey(CwtMemberConfig.Keys)

/** 声明类规则的上下文缓存键（用于缓存/索引加速）。*/
var CwtMemberConfig<*>.declarationConfigCacheKey: String? by createKey(CwtMemberConfig.Keys)

// endregion

// region CwtOptionMemberConfig Accessors

/** 将选项右值解析为布尔值（仅当值类型为 Boolean）。*/
val CwtOptionMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null

/** 将选项右值解析为整数（仅当值类型为 Int，解析失败时返回 `0`）。*/
val CwtOptionMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null

/** 将选项右值解析为浮点数（仅当值类型为 Float，解析失败时返回 `0f`）。*/
val CwtOptionMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null

/** 当值类型为 String 时返回选项右值，否则返回 `null`。*/
val CwtOptionMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null

/** 过滤并返回当前选项块下的选项规则列表（`CwtOptionConfig`）。*/
val CwtOptionMemberConfig<*>.options: List<CwtOptionConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionConfig>()

/** 过滤并返回当前选项块下的选项值规则列表（`CwtOptionValueConfig`）。*/
val CwtOptionMemberConfig<*>.optionValues: List<CwtOptionValueConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionValueConfig>()

// endregion
