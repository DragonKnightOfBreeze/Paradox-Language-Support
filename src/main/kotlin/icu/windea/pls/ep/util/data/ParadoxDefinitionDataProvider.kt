package icu.windea.pls.ep.util.data

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 用于得到定义的数据。
 */
interface ParadoxDefinitionDataProvider {
    fun <T : ParadoxDefinitionData> supports(element: ParadoxDefinitionElement, type: Class<T>, relax: Boolean = false): Boolean

    fun <T : ParadoxDefinitionData> get(element: ParadoxDefinitionElement, type: Class<T>): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionDataProvider>("icu.windea.pls.definitionDataProvider")
    }
}
