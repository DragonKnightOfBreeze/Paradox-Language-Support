package icu.windea.pls.ep.data

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于得到定义的数据。
 */
interface ParadoxDefinitionDataProvider {
    fun <T : ParadoxDefinitionData> supports(element: ParadoxScriptDefinitionElement, type: Class<T>): Boolean

    fun <T : ParadoxDefinitionData> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionDataProvider>("icu.windea.pls.definitionDataProvider")

        fun <T : ParadoxDefinitionData> get(element: ParadoxScriptDefinitionElement, type: Class<T>): T? {
            // if (element.definitionInfo == null) return null // NOTE 不能在这里判断，否则会出现 SOE
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!ep.supports(element, type)) return@f null
                ep.get(element, type)
            }
        }
    }
}
