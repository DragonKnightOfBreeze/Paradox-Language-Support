package icu.windea.pls.config.configContext

import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.paths.ParadoxElementPath

// region CwtConfigContext Accessors

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.elementPathFromRoot: ParadoxElementPath? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptExpression: String? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasConflict: Boolean? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasRecursion: Boolean? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterElement: ParadoxParameterElement? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterValueQuoted: Boolean? by createKey(CwtConfigContext.Keys)

// endregion

// region CwtDeclarationConfigContext Accessors

var CwtDeclarationConfigContext.gameRuleConfig: CwtExtendedGameRuleConfig? by createKey(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtExtendedOnActionConfig? by createKey(CwtDeclarationConfigContext.Keys)

// endregion
