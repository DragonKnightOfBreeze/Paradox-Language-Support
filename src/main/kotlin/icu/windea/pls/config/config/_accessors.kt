@file:Suppress("unused")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.ep.resolve.config.CwtOverriddenConfigProvider
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxTagType

// region CwtMemberConfig Accessors

/** 将值解析为布尔值。如果值类型非 [CwtType.Boolean]，则返回 `null`。*/
val CwtMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null

/** 将值解析为整数。如果值类型非 [CwtType.Int] 或解析失败，则返回 `null`。*/
val CwtMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null

/** 将值解析为浮点数。如果值类型非 [CwtType.Float] 或解析失败，则返回 `0f`）。*/
val CwtMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null

/** 将值解析为字符串。如果值类型非 [CwtType.String]，则返回 `null`。*/
val CwtMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null

val CwtMemberConfig<*>.isRoot: Boolean
    get() = when (this) {
        is CwtPropertyConfig -> this.parentConfig == null
        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
    }

/** 如果当前成员规则对应属性的值，则返回所属的属性规则。否则返回自身。 */
val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when (this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

/** 绑定到当前属性规则的单别名规则（解析阶段填充）。*/
var CwtPropertyConfig.singleAliasConfig: CwtSingleAliasConfig? by registerKey(CwtMemberConfig.Keys)

/** 绑定到当前属性规则的别名规则（解析阶段填充）。*/
var CwtPropertyConfig.aliasConfig: CwtAliasConfig? by registerKey(CwtMemberConfig.Keys)

/** 绑定到当前属性规则的内联规则（解析阶段填充）。*/
var CwtPropertyConfig.inlineConfig: CwtDirectiveConfig? by registerKey(CwtMemberConfig.Keys)

/** 当前值规则的标签类型（解析阶段推断，用于渲染和提示）。*/
var CwtValueConfig.tagType: ParadoxTagType? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则的被重载前的原始规则（用于规则覆盖、来源追踪等场景）。*/
var CwtMemberConfig<*>.originalConfig: CwtMemberConfig<CwtMember>? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则对应的重载提供者（来源于可重载的规则分组）。*/
var CwtMemberConfig<*>.overriddenProvider: CwtOverriddenConfigProvider? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则（作为声明规则的顶级成员规则时）对应的声明规则的上下文信息（用于声明/定义相关能力）。*/
var CwtMemberConfig<*>.declarationConfigContext: CwtDeclarationConfigContext? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则（作为声明规则的顶级成员规则时）对应的声明规则的上下文缓存键（用于缓存/索引加速）。*/
var CwtMemberConfig<*>.declarationConfigCacheKey: String? by registerKey(CwtMemberConfig.Keys)

// endregion

// region CwtOptionMemberConfig Accessors

/** 将选项值解析为布尔值。如果值类型非 [CwtType.Boolean]，则返回 `null`。*/
val CwtOptionMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null

/** 将选项值解析为整数。如果值类型非 [CwtType.Int] 或解析失败，则返回 `null`。*/
val CwtOptionMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null

/** 将选项值解析为浮点数。如果值类型非 [CwtType.Float] 或解析失败，则返回 `0f`）。*/
val CwtOptionMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null

/** 将选项值解析为字符串。如果值类型非 [CwtType.String]，则返回 `null`。*/
val CwtOptionMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null

// endregion
