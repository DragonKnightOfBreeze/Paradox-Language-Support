package icu.windea.pls.lang.util

import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.model.ParadoxDefinitionSource

@Suppress("unused")
object ParadoxDefinitionCandidateManager {
    fun getType(element: ParadoxDefinitionElement): String? {
        return getInfo(element)?.type
    }

    fun getSubtypes(element: ParadoxDefinitionElement): List<String>? {
        return getInfo(element)?.subtypes
    }

    fun getInfo(element: ParadoxDefinitionElement): ParadoxDefinitionCandidateInfo? {
        val definitionInfo = ParadoxDefinitionManager.getInfo(element)
        if (definitionInfo != null && definitionInfo.source != ParadoxDefinitionSource.Injection) return definitionInfo
        if (element is ParadoxScriptProperty) {
            val definitionInjectionInfo = ParadoxDefinitionInjectionManager.getInfo(element)
            if (definitionInjectionInfo != null) return definitionInjectionInfo
        }
        return null
    }
}
