package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.wildcard
import icu.windea.pls.config.manipulators.CwtConfigManipulator
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.mergeValue
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterInferredConfigProvider
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.ep.resolve.parameter.support
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.resolve.expression.ParadoxParameterConditionExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameterConditionExpression
import java.util.*
import icu.windea.pls.model.CwtType

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
    fun resolveParameter(element: ParadoxParameter): ParadoxParameterLightElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveParameter(element)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.resolveConditionParameter
     */
    fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterLightElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveConditionParameter(element)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.resolveArgument
     */
    fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        return ParadoxParameterSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
            ep.resolveArgument(element, rangeInElement, config)?.also { it.support = ep }
        }
    }

    /**
     * @see ParadoxParameterSupport.processContext
     */
    fun processContext(parameterElement: ParadoxParameterLightElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
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
    fun getDocumentationDefinition(parameterElement: ParadoxParameterLightElement, builder: DocumentationBuilder): Boolean {
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

    fun resolveContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        val file = element.containingFile
        val project = file.project
        val gameType = selectGameType(file) ?: return null
        val parameters = sortedMapOf<String, MutableList<ParadoxParameterContextInfo.Parameter>>() // 按名字进行排序
        val fileConditionExpressions = ArrayDeque<ParadoxParameterConditionExpression>()
        element.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptParameterConditionExpression) return visitParameterConditionExpression(element)
                if (element is ParadoxConditionParameter) return visitConditionParameter(element)
                if (element is ParadoxParameter) return visitParameter(element)
                super.visitElement(element)
            }

            private fun visitParameterConditionExpression(element: ParadoxScriptParameterConditionExpression) {
                // value may be empty (invalid condition expression)
                fileConditionExpressions.addLast(ParadoxParameterConditionExpression.resolve(element.text))
                super.visitElement(element)
            }

            private fun visitConditionParameter(element: ParadoxConditionParameter) {
                val name = element.name ?: return
                val elementPointer = element.createPointer<PsiElement>(file)
                val info = ParadoxParameterContextInfo.Parameter(elementPointer, name, null, null, project, gameType)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                // 不需要继续向下遍历
            }

            private fun visitParameter(element: ParadoxParameter) {
                val name = element.name ?: return
                val defaultValue = element.defaultValue
                val conditionalExpressions = ArrayDeque(fileConditionExpressions) // not null
                val elementPointer = element.createPointer<PsiElement>(file)
                val info = ParadoxParameterContextInfo.Parameter(elementPointer, name, defaultValue, conditionalExpressions, project, gameType)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                // 不需要继续向下遍历
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxScriptParameterCondition || element is ParadoxScriptInlineParameterCondition) {
                    fileConditionExpressions.removeLast()
                }
            }
        })
        return ParadoxParameterContextInfo(parameters, project, gameType)
    }

    fun getInferredContextConfigsFromConfig(parameterElement: ParadoxParameterLightElement, fast: Boolean = true): List<CwtMemberConfig<*>> {
        return doGetInferredContextConfigsFromConfig(parameterElement, fast)
    }

    private fun doGetInferredContextConfigsFromConfig(parameterElement: ParadoxParameterLightElement, fast: Boolean): List<CwtMemberConfig<*>> {
        val configGroup = PlsFacade.getConfigGroup(parameterElement.project, parameterElement.gameType)
        val configs = configGroup.extendedParameters.findByPattern(parameterElement.name, parameterElement, configGroup)
        if (configs.isNullOrEmpty()) return emptyList()
        val config = when {
            fast -> configs.find { it.contextKey.matchesByPattern(parameterElement.contextKey, parameterElement, configGroup) }
            else -> configs.findLast { it.contextKey.matchesByPattern(parameterElement.contextKey, parameterElement, configGroup) }
        }
        if (config == null) return emptyList()
        return config.getContextConfigs(parameterElement)
    }

    fun getInferredContextConfigsFromUsages(parameterElement: ParadoxParameterLightElement, fast: Boolean = true): List<CwtMemberConfig<*>> {
        return withRecursionGuard {
            withRecursionCheck(parameterElement) {
                doGetInferredContextConfigsFromUsages(parameterElement, fast)
            }
        } ?: emptyList()
    }

    private fun doGetInferredContextConfigsFromUsages(parameterElement: ParadoxParameterLightElement, fast: Boolean): List<CwtMemberConfig<*>> {
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        processContext(parameterElement, true) p@{ context ->
            ProgressManager.checkCanceled()
            val contextInfo = getContextInfo(context) ?: return@p true
            val contextConfigs = doGetInferredContextConfigsFromUsages(parameterElement.name, contextInfo, fast).orNull()
            // merge
            val r = result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }
            if (fast && isFastAvailable(result)) false else r
        }
        return result.get().orEmpty()
    }

    private fun doGetInferredContextConfigsFromUsages(parameterName: String, parameterContextInfo: ParadoxParameterContextInfo, fast: Boolean): List<CwtMemberConfig<*>> {
        val parameterInfos = parameterContextInfo.parameters.get(parameterName)
        if (parameterInfos.isNullOrEmpty()) return emptyList()
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        parameterInfos.process p@{ parameterInfo ->
            ProgressManager.checkCanceled()
            val contextConfigs = getContextConfigs(parameterInfo, parameterContextInfo).orNull()
            // merge
            val r = result.mergeValue(contextConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }
            if (fast && isFastAvailable(result)) false else r
        }
        return result.get().orEmpty()
    }

    private fun isFastAvailable(result: Ref<List<CwtMemberConfig<*>>>): Boolean {
        val v = result.get()
        if (v.isNullOrEmpty()) return false // empty -> not available
        val c = v.singleOrNull()?.configs?.singleOrNull()
        if (c is CwtValueConfig && c.configExpression.wildcard) return false // wildcard (e.g., from condition parameter) -> not available
        return true
    }

    fun getInferredType(contextConfigs: List<CwtMemberConfig<*>>): String? {
        val configs = contextConfigs.singleOrNull()?.configs
        if (configs.isNullOrEmpty()) return null
        if (configs.any { it !is CwtValueConfig || it.valueType == CwtType.Block }) return PlsBundle.message("complex")
        return configs.mapTo(mutableSetOf()) { it.configExpression.expressionString }.joinToString(" | ")
    }

    fun getParameterizedKeyConfigs(contextConfigs: List<CwtMemberConfig<*>>): List<CwtValueConfig> {
        val configs = contextConfigs.singleOrNull()?.configs
            ?.filterNot { it !is CwtValueConfig || it.valueType == CwtType.Block }
        if (configs.isNullOrEmpty()) return emptyList()
        return configs.cast()
    }
}
