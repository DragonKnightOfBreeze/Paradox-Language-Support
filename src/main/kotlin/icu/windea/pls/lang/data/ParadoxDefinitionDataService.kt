package icu.windea.pls.lang.data

import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.ep.util.data.ParadoxDefinitionDataProvider
import icu.windea.pls.lang.psi.ParadoxDefinitionElement

// TODO 3.0.x refactor

object ParadoxDefinitionDataService {
    inline fun <reified T : ParadoxDefinitionData> get(element: ParadoxDefinitionElement, lenient: Boolean = false): T? {
        return get(element, T::class.java, lenient)
    }

    fun <T : ParadoxDefinitionData> get(element: ParadoxDefinitionElement, type: Class<T>, lenient: Boolean = false): T? {
        return ParadoxDefinitionDataProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element, type, lenient)) return@f null
            ep.get(element, type)
        }
    }
}
