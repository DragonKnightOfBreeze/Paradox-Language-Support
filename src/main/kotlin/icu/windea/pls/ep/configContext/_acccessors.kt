package icu.windea.pls.ep.configContext

import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement

// region CwtConfigContext Accessors

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
