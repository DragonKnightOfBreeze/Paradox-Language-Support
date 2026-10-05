package icu.windea.pls.lang.presentation

import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentation
import icu.windea.pls.ep.util.presentation.ParadoxDefinitionPresentationProvider
import icu.windea.pls.lang.psi.ParadoxDefinitionElement

// TODO 3.0.x refactor

object ParadoxDefinitionPresentationService {
    inline fun <reified T : ParadoxDefinitionPresentation> get(element: ParadoxDefinitionElement, lenient: Boolean = false): T? {
        return get(element, T::class.java, lenient)
    }

    fun <T : ParadoxDefinitionPresentation> get(element: ParadoxDefinitionElement, type: Class<T>, lenient: Boolean = false): T? {
        return ParadoxDefinitionPresentationProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element, type, lenient)) return@f null
            ep.get(element, type)
        }
    }
}
