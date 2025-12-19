package icu.windea.pls.lang.util.presentation

import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentationProvider
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

object ParadoxPresentationService {
    inline fun <reified T : ParadoxDefinitionPresentation> get(element: ParadoxScriptDefinitionElement): T? {
        return get(element, T::class.java)
    }

    fun <T : ParadoxDefinitionPresentation> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
        return ParadoxDefinitionPresentationProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element, type)) return@f null
            ep.get(element, type)
        }
    }
}
