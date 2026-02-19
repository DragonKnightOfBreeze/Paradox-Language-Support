package icu.windea.pls.lang.util.data

import icu.windea.pls.ep.util.data.ParadoxDefinitionData
import icu.windea.pls.ep.util.data.ParadoxDefinitionDataProvider
import icu.windea.pls.script.psi.ParadoxDefinitionElement

object ParadoxDataService {
    inline fun <reified T : ParadoxDefinitionData> getDefinitionData(element: ParadoxDefinitionElement, relax: Boolean = false): T? {
        return getDefinitionData(element, T::class.java, relax)
    }

    fun <T : ParadoxDefinitionData> getDefinitionData(element: ParadoxDefinitionElement, type: Class<T>, relax: Boolean = false): T? {
        return ParadoxDefinitionDataProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!ep.supports(element, type, relax)) return@f null
            ep.get(element, type)
        }
    }
}
