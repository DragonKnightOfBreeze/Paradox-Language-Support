package icu.windea.pls.config.configContext

import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.paths.ParadoxElementPath

// region CwtConfigContext Accessors

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.elementPathFromRoot: ParadoxElementPath? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterElement: ParadoxParameterElement? by registerKey(CwtConfigContext.Keys)
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
