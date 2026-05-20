package icu.windea.pls.lang.util.presentation

import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentationProvider
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxPresentationService {
    inline fun <reified T : ParadoxDefinitionPresentation> getDefinitionPresentation(element: ParadoxDefinitionElement, lenient: Boolean = false): T? {
        return getDefinitionPresentation(element, T::class.java, lenient)
    }

    fun <T : ParadoxDefinitionPresentation> getDefinitionPresentation(element: ParadoxDefinitionElement, type: Class<T>, lenient: Boolean = false): T? {
        return ParadoxDefinitionPresentationProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element, type, lenient)) return@f null
            ep.get(element, type)
        }
    }
}
