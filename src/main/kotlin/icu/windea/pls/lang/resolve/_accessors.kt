package icu.windea.pls.lang.resolve

import com.github.benmanes.caffeine.cache.Cache
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.ep.resolve.config.CwtOverriddenConfigProvider
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo

// region CwtMemberConfig Accessors

/** 当前成员规则的被重载前的原始规则（用于规则覆盖、来源追踪等场景）。 */
var CwtMemberConfig<*>.originalConfig: CwtMemberConfig<CwtMember>? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则对应的重载提供者（来源于可重载的规则分组）。 */
var CwtMemberConfig<*>.overriddenProvider: CwtOverriddenConfigProvider? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则（作为声明规则的顶级成员规则时）对应的声明规则的上下文信息（用于声明/定义相关能力）。 */
var CwtMemberConfig<*>.declarationConfigContext: CwtDeclarationConfigContext? by registerKey(CwtMemberConfig.Keys)

/** 当前成员规则（作为声明规则的顶级成员规则时）对应的声明规则的上下文缓存键（用于缓存/索引加速）。 */
var CwtMemberConfig<*>.declarationConfigCacheKey: String? by registerKey(CwtMemberConfig.Keys)

// endregion

// region CwtConfigContext Accessors

val CwtConfigContext.dynamicCache: Cache<String, List<CwtMemberConfig<*>>> by registerKey(CwtConfigContext.Keys) { CacheBuilder().build() }

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.defineVariableInfo: ParadoxDefineVariableInfo? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterElement: ParadoxParameterLightElement? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterValueQuoted: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptExpression: String? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasConflict: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasRecursion: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.definitionInjectionInfo: ParadoxDefinitionInjectionInfo? by registerKey(CwtConfigContext.Keys)

// endregion

// region CwtDeclarationConfigContext Accessors

var CwtDeclarationConfigContext.gameRuleConfig: CwtExtendedGameRuleConfig? by registerKey(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtExtendedOnActionConfig? by registerKey(CwtDeclarationConfigContext.Keys)

// endregion
