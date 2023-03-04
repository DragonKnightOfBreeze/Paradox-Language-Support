package icu.windea.pls.lang.presentation

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyPresentationProvider : ParadoxDefinitionPresentationProvider {
    override fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Image? {
        return null
    }
}