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

val CwtMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null
val CwtMemberConfig<*>.values: List<CwtValueConfig>? get() = configs?.filterIsInstance<CwtValueConfig>()
val CwtMemberConfig<*>.properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstance<CwtPropertyConfig>()

/** 通过 [CwtOptionFlags] 获取选项标志。 */
val CwtMemberConfig<*>.optionFlags: CwtOptionFlags get() = CwtOptionFlags.from(this)

/** 通过 [CwtOptionDataAccessor] 获取选项数据。 */
fun <T> CwtMemberConfig<*>.optionData(accessor: CwtOptionDataAccessor<T>): T = accessor.get(this)
/** 通过 [CwtOptionDataAccessor] 获取选项数据。 */
inline fun <T> CwtMemberConfig<*>.optionData(accessorGetter: CwtOptionDataAccessors.() -> CwtOptionDataAccessor<T>): T = optionData(CwtOptionDataAccessors.accessorGetter())

var CwtPropertyConfig.singleAliasConfig: CwtSingleAliasConfig? by createKey(CwtMemberConfig.Keys)
var CwtPropertyConfig.aliasConfig: CwtAliasConfig? by createKey(CwtMemberConfig.Keys)
var CwtPropertyConfig.inlineConfig: CwtInlineConfig? by createKey(CwtMemberConfig.Keys)

var CwtValueConfig.tagType: CwtTagType? by createKey(CwtMemberConfig.Keys)

var CwtMemberConfig<*>.originalConfig: CwtMemberConfig<CwtMemberElement>? by createKey(CwtMemberConfig.Keys)
var CwtMemberConfig<*>.overriddenProvider: CwtOverriddenConfigProvider? by createKey(CwtMemberConfig.Keys)

var CwtMemberConfig<*>.declarationConfigContext: CwtDeclarationConfigContext? by createKey(CwtMemberConfig.Keys)
var CwtMemberConfig<*>.declarationConfigCacheKey: String? by createKey(CwtMemberConfig.Keys)

// endregion

// region CwtOptionMemberConfig Accessors

val CwtOptionMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtOptionMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtOptionMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtOptionMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null
val CwtOptionMemberConfig<*>.options: List<CwtOptionConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionConfig>()
val CwtOptionMemberConfig<*>.optionValues: List<CwtOptionValueConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionValueConfig>()

// endregion
