package icu.windea.pls.ep.util.presentation

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于得到定义的图形表示。
 */
interface ParadoxDefinitionPresentationProvider {
    fun <T : ParadoxDefinitionPresentation> supports(element: ParadoxDefinitionElement, type: Class<T>, relax: Boolean = false): Boolean

    fun <T : ParadoxDefinitionPresentation> get(element: ParadoxDefinitionElement, type: Class<T>): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionPresentationProvider>("icu.windea.pls.definitionPresentationProvider")
    }
}
