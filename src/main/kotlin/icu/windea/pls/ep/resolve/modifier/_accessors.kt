package icu.windea.pls.ep.resolve.modifier

import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport.Keys.synced
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.model.ParadoxModifierInfo

val ParadoxModifierSupport.Keys.support by registerKey<ParadoxModifierSupport>(ParadoxModifierSupport.Keys).synced()
val ParadoxModifierSupport.Keys.modifierConfig by registerKey<CwtModifierConfig>(ParadoxModifierSupport.Keys).synced()

var ParadoxModifierInfo.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierInfo.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig

var ParadoxModifierElement.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierElement.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig
