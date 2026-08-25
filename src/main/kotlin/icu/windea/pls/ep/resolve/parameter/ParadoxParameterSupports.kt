package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.modificationTrackerModel
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.injection.ChronicleInjectionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueReferenceExpression
import icu.windea.pls.lang.resolve.util.ParadoxParameterSupportFactory
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.ParadoxParameterInfo
import icu.windea.pls.model.definitionName
import icu.windea.pls.model.definitionTypes
import icu.windea.pls.model.inlineScriptExpression
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxDefinitionParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxDefinitionElement): Boolean {
        if (element !is ParadoxScriptProperty) return false
        val definitionInfo = element.definitionInfo ?: return false
        // NOTE 简单判断 - 目前不需要兼容子类型
        return definitionInfo.type in definitionInfo.configGroup.typeModel.supportParameters
    }

    override fun findContext(element: PsiElement): ParadoxDefinitionElement? {
        // NOTE 这里需要兼容通过语言注入注入到脚本文件中的脚本片段中的参数（此时需要先获取最外面的 `injectionHost`）
        val finalElement = ChronicleInjectionManager.findTopHostElementOrThis(element, element.project)
        val context = selectScope { finalElement.parentDefinition() }
        return context?.takeIf { isContext(it) }
    }

    override fun resolveParameter(element: ParadoxParameter): ParadoxParameterLightElement? {
        val name = element.name?.orNull() ?: return null
        val context = findContext(element) ?: return null
        return ParadoxParameterSupportFactory.resolveParameterForDefinition(element, name, context)
    }

    override fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterLightElement? {
        val name = element.name?.orNull() ?: return null
        val context = findContext(element) ?: return null
        return ParadoxParameterSupportFactory.resolveParameterForDefinition(element, name, context)
    }

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        return ParadoxParameterSupportFactory.resolveArgumentForDefinition(element, rangeInExpression, config)
    }

    override fun processContext(element: ParadoxParameterLightElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        val definitionName = element.definitionName ?: return true
        val definitionTypes = element.definitionTypes ?: return true
        if (definitionName.isParameterized()) return true // skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = element.project
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(definitionName, definitionType, selector).onlyMostRelevant(onlyMostRelevant).processAsync(processor)
    }

    override fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        val definitionName = contextReferenceInfo.definitionName ?: return true
        val definitionTypes = contextReferenceInfo.definitionTypes ?: return true
        if (definitionName.isParameterized()) return true // skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = contextReferenceInfo.project
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(definitionName, definitionType, selector).onlyMostRelevant(onlyMostRelevant).processAsync(processor)
    }

    override fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        if (isContext(element)) return null // check before walking children recursively
        return ParadoxParameterSupportFactory.getContextInfo(element)
    }

    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        return ParadoxParameterSupportFactory.getContextReferenceInfoForDefinition(element, from, *extraArgs)
    }

    override fun getContextKeyFromContext(context: ParadoxDefinitionElement): String? {
        val definitionInfo = context.definitionInfo ?: return null
        return "${definitionInfo.types.joinToString(".")}@${definitionInfo.name}"
    }

    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker {
        val configGroup = ChronicleFacade.getConfigGroup(parameterInfo.project, parameterInfo.gameType)
        return configGroup.modificationTrackerModel.definitionParameter
    }
}

class ParadoxInlineScriptParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxDefinitionElement): Boolean {
        if (element !is ParadoxScriptFile) return false
        return ParadoxInlineScriptManager.getInlineScriptExpression(element) != null
    }

    override fun findContext(element: PsiElement): ParadoxDefinitionElement? {
        // NOTE 这里需要兼容通过语言注入注入到脚本文件中的脚本片段中的参数（此时需要先获取最外面的 injectionHost）
        val finalElement = ChronicleInjectionManager.findTopHostElementOrThis(element, element.project)
        val context = finalElement.containingFile?.castOrNull<ParadoxScriptFile>()
        return context?.takeIf { isContext(it) }
    }

    override fun resolveParameter(element: ParadoxParameter): ParadoxParameterLightElement? {
        val name = element.name?.orNull() ?: return null
        val context = findContext(element) as? ParadoxScriptFile ?: return null
        return ParadoxParameterSupportFactory.resolveParameterForInlineScript(element, name, context)
    }

    override fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterLightElement? {
        val name = element.name?.orNull() ?: return null
        val context = findContext(element) as? ParadoxScriptFile ?: return null
        return ParadoxParameterSupportFactory.resolveParameterForInlineScript(element, name, context)
    }

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        return ParadoxParameterSupportFactory.resolveArgumentForInlineScript(element, rangeInExpression, config)
    }

    override fun processContext(element: ParadoxParameterLightElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        val expression = element.inlineScriptExpression ?: return true
        if (expression.isParameterized()) return true // skip if context name is parameterized
        val project = element.project
        return ParadoxInlineScriptManager.processInlineScriptFile(expression, project, element, onlyMostRelevant, processor)
    }

    override fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean): Boolean {
        val expression = contextReferenceInfo.inlineScriptExpression ?: return true
        if (expression.isParameterized()) return true // skip if context name is parameterized
        val project = contextReferenceInfo.project
        return ParadoxInlineScriptManager.processInlineScriptFile(expression, project, element, onlyMostRelevant, processor)
    }

    override fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        if (isContext(element)) return null // check before walking children recursively
        return ParadoxParameterSupportFactory.getContextInfo(element)
    }

    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        return ParadoxParameterSupportFactory.getContextReferenceInfoForInlineScript(element, from, *extraArgs)
    }

    override fun getContextKeyFromContext(context: ParadoxDefinitionElement): String? {
        if (context !is ParadoxScriptFile) return null
        val expression = ParadoxInlineScriptManager.getInlineScriptExpression(context) ?: return null
        return "inline_script@$expression"
    }

    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker {
        return ChronicleModificationTrackers.InlineScripts
    }
}

/**
 * @see ParadoxScriptValueReferenceExpression
 */
class ParadoxScriptValueInlineParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxDefinitionElement) = false

    override fun findContext(element: PsiElement) = null

    override fun resolveParameter(element: ParadoxParameter) = null

    override fun resolveConditionParameter(element: ParadoxConditionParameter) = null

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        return ParadoxParameterSupportFactory.resolveArgumentForScriptValueReference(element, rangeInExpression, config)
    }

    override fun processContext(element: ParadoxParameterLightElement, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean) = true

    override fun processContextReference(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxDefinitionElement) -> Boolean) = true

    override fun getContextInfo(element: ParadoxDefinitionElement) = null

    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        return ParadoxParameterSupportFactory.getContextReferenceInfoForScriptValueReference(element, from, *extraArgs)
    }

    override fun getContextKeyFromContext(context: ParadoxDefinitionElement) = null

    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker {
        val configGroup = ChronicleFacade.getConfigGroup(parameterInfo.project, parameterInfo.gameType)
        return configGroup.modificationTrackerModel.scriptValue
    }
}
