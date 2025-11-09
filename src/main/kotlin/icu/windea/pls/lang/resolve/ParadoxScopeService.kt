package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.ep.resolve.scope.ParadoxDefinitionInferredScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDefinitionScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDefinitionSupportedScopesProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDynamicValueInferredScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxDynamicValueScopeContextProvider
import icu.windea.pls.ep.resolve.scope.ParadoxOverriddenScopeContextProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.overriddenProvider
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

object ParadoxScopeService {
    /**
     * @see ParadoxDefinitionSupportedScopesProvider.getSupportedScopes
     */
    fun getSupportedScopes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String>? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionSupportedScopesProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            if (!ep.supports(definition, definitionInfo)) return@f null
            ep.getSupportedScopes(definition, definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionScopeContextProvider.getScopeContext
     */
    fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val gameType = definitionInfo.gameType
        return ParadoxDefinitionScopeContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            if (!ep.supports(definition, definitionInfo)) return@f null
            ep.getScopeContext(definition, definitionInfo)
        }
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getScopeContext
     */
    fun getInferredScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContext? {
        val gameType = definitionInfo.gameType
        var map: Map<String, String>? = null
        ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(definition, definitionInfo)) return@f
            val info = ep.getScopeContext(definition, definitionInfo) ?: return@f
            if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
            if (map == null) {
                map = info.scopeContextMap
            } else {
                map = ParadoxScopeManager.mergeScopeContextMap(map, info.scopeContextMap)
            }
        }
        val resultMap = map ?: return null
        val result = ParadoxScopeContext.get(resultMap)
        return result
    }

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getMessage
     */
    @Suppress("unused")
    fun getInferenceMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val gameType = definitionInfo.gameType
        var message: String? = null
        ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
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

    /**
     * @see ParadoxDefinitionInferredScopeContextProvider.getErrorMessage
     */
    fun getInferenceErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val gameType = definitionInfo.gameType
        var errorMessage: String? = null
        ParadoxDefinitionInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
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

    /**
     * @see ParadoxDynamicValueScopeContextProvider.getScopeContext
     */
    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val gameType = element.gameType
        return ParadoxDynamicValueScopeContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            if (!ep.supports(element)) return@f null
            ep.getScopeContext(element)
        }
    }

    /**
     * @see ParadoxDynamicValueInferredScopeContextProvider.getScopeContext
     */
    fun getInferredScopeContext(dynamicValue: ParadoxDynamicValueElement): ParadoxScopeContext? {
        val gameType = dynamicValue.gameType
        var map: Map<String, String>? = null
        ParadoxDynamicValueInferredScopeContextProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(dynamicValue)) return@f
            val info = ep.getScopeContext(dynamicValue) ?: return@f
            if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
            if (map == null) {
                map = info.scopeContextMap
            } else {
                map = ParadoxScopeManager.mergeScopeContextMap(map, info.scopeContextMap)
            }
        }
        val resultMap = map ?: return null
        val result = ParadoxScopeContext.get(resultMap)
        return result
    }

    /**
     * @see ParadoxOverriddenScopeContextProvider.getOverriddenScopeContext
     */
    fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        val gameType = config.configGroup.gameType
        return ParadoxOverriddenScopeContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getOverriddenScopeContext(contextElement, config, parentScopeContext)
                ?.also { it.overriddenProvider = ep }
        }
    }
}
