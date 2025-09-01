package icu.windea.pls.ep.modifier

import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.modifier.ParadoxModifierSupport.Keys.synced
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.model.elementInfo.ParadoxModifierInfo

val ParadoxModifierSupport.Keys.support by createKey<ParadoxModifierSupport>(ParadoxModifierSupport.Keys).synced()
val ParadoxModifierSupport.Keys.modifierConfig by createKey<CwtModifierConfig>(ParadoxModifierSupport.Keys).synced()

var ParadoxModifierInfo.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierInfo.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig

var ParadoxModifierElement.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierElement.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig
