package icu.windea.pls.lang.resolve

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.codeInsight.documentation.DocumentationBuilder
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterInferredConfigProvider
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.ep.resolve.parameter.support
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

object ParadoxParameterService {
    /**
     * @see ParadoxParameterSupport.findContext
     */
    fun findContext(element: PsiElement): ParadoxDefinitionElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { it.findContext(element) }
    }

    /**
     * @see ParadoxParameterSupport.getContextKeyFromContext
     */
    fun getContextKeyFromContext(element: ParadoxDefinitionElement): String? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { it.getContextKeyFromContext(element) }
    }

    /**
     * @see ParadoxParameterSupport.getContextInfo
     */
    fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.getContextInfo(element)
        }
    }

    /**
     * @see ParadoxParameterSupport.getContextReferenceInfo
     */
    fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.getContextReferenceInfo(element, from, *extraArgs)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.resolveParameter
     */
    fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveParameter(element)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.resolveConditionParameter
     */
    fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveConditionParameter(element)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.resolveArgument
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveArgument(element, rangeInElement, config)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.processContext
     */
    fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        return ParadoxParameterSupport.EP_NAME.extensionList.any { ep ->
            ep.processContext(parameterElement, onlyMostRelevant, processor)
        }
    }

    /**
     * @see ParadoxParameterSupport.processContextReference
     */
    fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        return ParadoxParameterSupport.EP_NAME.extensionList.any { ep ->
            ep.processContextReference(element, contextReferenceInfo, onlyMostRelevant, processor)
        }
    }

    /**
     * @see ParadoxParameterSupport.buildDocumentationDefinition
     */
    fun getDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean {
        return ParadoxParameterSupport.EP_NAME.extensionList.any { ep ->
            ep.buildDocumentationDefinition(parameterElement, builder)
        }
    }

    /**
     * @see ParadoxParameterInferredConfigProvider.getContextConfigs
     */
    fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        // NOTE recursion guard is required here
        val gameType = parameterContextInfo.gameType
        return withRecursionGuard {
            ParadoxParameterInferredConfigProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                if (!ep.supports(parameterInfo, parameterContextInfo)) return@f null
                ep.getContextConfigs(parameterInfo, parameterContextInfo).orNull()
            }
        }
    }
}
