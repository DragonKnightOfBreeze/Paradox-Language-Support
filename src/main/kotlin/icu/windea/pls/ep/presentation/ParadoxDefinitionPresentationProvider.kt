package icu.windea.pls.ep.presentation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于得到定义的图形表示。
 */
interface ParadoxDefinitionPresentationProvider {
    fun <T : ParadoxDefinitionPresentation> supports(element: ParadoxScriptDefinitionElement, type: Class<T>): Boolean

    fun <T : ParadoxDefinitionPresentation> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionPresentationProvider>("icu.windea.pls.definitionPresentationProvider")

        fun <T : ParadoxDefinitionPresentation> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(element, type)) return@f null
                ep.get(element, type)
            }
        }
    }
}
