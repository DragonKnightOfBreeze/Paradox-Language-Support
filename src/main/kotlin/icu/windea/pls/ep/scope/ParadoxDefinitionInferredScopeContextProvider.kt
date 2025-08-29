package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxScopeContext
import icu.windea.pls.model.ParadoxScopeContextInferenceInfo
import icu.windea.pls.model.resolve
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 用于为定义提供（基于使用）推断的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDefinitionInferredScopeContextProvider {
    fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean

    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo?

    /**
     * 当推断结果不存在冲突时要显示的消息。
     */
    fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String?

    /**
     * 当推断结果存在冲突时要显示的错误消息。
     */
    fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDefinitionInferredScopeContextProvider>("icu.windea.pls.definitionInferredScopeContextProvider")

        fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
            val gameType = definitionInfo.gameType
            var map: Map<String, String>? = null
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                if (!ep.supports(definition, definitionInfo)) return@f
                val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
                if (info.hasConflict) return null //只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
                if (map == null) {
                    map = info.scopeContextMap
                } else {
                    map = ParadoxScopeManager.mergeScopeContextMap(map!!, info.scopeContextMap)
                }
            }
            val resultMap = map ?: return null
            val result = ParadoxScopeContext.resolve(resultMap)
            return result
        }

        fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
            val gameType = definitionInfo.gameType
            var errorMessage: String? = null
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                if (!ep.supports(definition, definitionInfo)) return@f
                val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
                if (!info.hasConflict) return@f
                if (errorMessage == null) {
                    errorMessage = ep.getErrorMessage(definition, definitionInfo, info)
                } else {
                    return PlsBundle.message("script.annotator.scopeContext.conflict", definitionInfo.name)
                }
            }
            return errorMessage
        }

        fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
            val gameType = definitionInfo.gameType
            var message: String? = null
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                if (!ep.supports(definition, definitionInfo)) return@f
                val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
                if (info.hasConflict) return@f
                if (message == null) {
                    message = ep.getMessage(definition, definitionInfo, info)
                } else {
                    return PlsBundle.message("script.annotator.scopeContext", definitionInfo.name)
                }
            }
            return message
        }
    }
}

