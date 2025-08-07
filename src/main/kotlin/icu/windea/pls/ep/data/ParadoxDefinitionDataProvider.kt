package icu.windea.pls.ep.data

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameTypeEP
interface ParadoxDefinitionDataProvider<T : ParadoxDefinitionData> {
    val dataType: Class<T>

    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getData(definition: ParadoxScriptDefinitionElement): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionDataProvider<*>>("icu.windea.pls.definitionDataProvider")

        @Suppress("UNCHECKED_CAST")
        fun <T : ParadoxDefinitionData> getData(dataType: Class<T>, definition: ParadoxScriptDefinitionElement): T? {
            val definitionInfo = definition.definitionInfo ?: return null
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                if (ep.dataType != dataType) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getData(definition) as? T?
            }
        }
    }
}
