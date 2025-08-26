package icu.windea.pls.ep.presentation

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于得到定义的图形表示的数据，从而进一步绘制各种图形表示。
 */
@WithGameTypeEP
interface ParadoxDefinitionPresentationProvider<T : ParadoxDefinitionPresentation> {
    val type: Class<T>

    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getPresentation(definition: ParadoxScriptDefinitionElement): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionPresentationProvider<*>>("icu.windea.pls.definitionPresentationProvider")

        @Suppress("UNCHECKED_CAST")
        fun <T : ParadoxDefinitionPresentation> getPresentation(type: Class<T>, definition: ParadoxScriptDefinitionElement): T? {
            val definitionInfo = definition.definitionInfo ?: return null
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                if (ep.type != type) return@f null
                if (!ep.supports(definition, definitionInfo)) return@f null
                ep.getPresentation(definition) as? T?
            }
        }
    }
}
