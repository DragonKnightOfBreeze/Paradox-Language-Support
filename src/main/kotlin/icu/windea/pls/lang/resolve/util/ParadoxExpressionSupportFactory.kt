package icu.windea.pls.lang.resolve.util

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.isStatic
import icu.windea.pls.config.config.resolveElementWithConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.util.withSearchScopeType
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxExpressionSupportFactory {
    // region Annotate Methods

    fun annotateExpression(element: ParadoxExpressionElement, rangeInExpression: TextRange, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        if (rangeInExpression.isEmpty) return
        val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
        val range = rangeInExpression.shiftRight(offset)

        val parameterRanges = ParadoxExpressionService.getParameterRangesInExpression(element)
        if (parameterRanges.isNotEmpty()) {
            val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
            if (finalRanges is List<*> && finalRanges.isEmpty()) return
            finalRanges.forEach { finalRange ->
                if (finalRange.isEmpty) return@forEach
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(finalRange).textAttributes(attributesKey).create()
            }
            return
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
    }

    fun annotateExpressionAsHighlightedReference(element: ParadoxExpressionElement, rangeInExpression: TextRange, holder: AnnotationHolder) {
        if (rangeInExpression.isEmpty) return
        val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
        val range = rangeInExpression.shiftRight(offset)
        val attributesKey = DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE
        holder.newSilentAnnotation(HighlightInfoType.HIGHLIGHTED_REFERENCE_SEVERITY).range(range).textAttributes(attributesKey).create()
    }

    fun annotateComplexExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        annotateComplexExpressionNode(element, expression, holder, config)
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        if (node.text.isEmpty()) return

        run {
            val attributesKey = node.getAttributesKey(element)
            val mustUseAttributesKey = attributesKey != ParadoxScriptHighlighterColors.PROPERTY_KEY && attributesKey != ParadoxScriptHighlighterColors.STRING
            if (attributesKey != null && mustUseAttributesKey) {
                annotateComplexExpressionNode(element, node, holder, attributesKey)
                return@run
            }
            val attributesKeyConfig = node.getAttributesKeyConfig(element)
            if (attributesKeyConfig != null) {
                ParadoxExpressionService.annotateScriptExpression(element, node.text, node.rangeInExpression, attributesKeyConfig, holder)
                return@run
            }
            if (attributesKey != null) {
                annotateComplexExpressionNode(element, node, holder, attributesKey)
            }
        }

        node.nodes.forEachFast { node ->
            annotateComplexExpressionNode(element, node, holder, config)
        }
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        if (node.text.isEmpty()) return

        // merge text attributes from HighlighterColors.TEXT and attributesKey for token nodes (in case foreground is not set)
        // do not apply this logic in tests
        if (node is ParadoxTokenNode && !ChronicleFacade.isUnitTestMode()) {
            val editorColorsManager = EditorColorsManager.getInstance()
            val schema = editorColorsManager.activeVisibleScheme ?: editorColorsManager.schemeForCurrentUITheme
            val textAttributes1 = schema.getAttributes(HighlighterColors.TEXT)
            val textAttributes2 = schema.getAttributes(attributesKey)
            val textAttributes = TextAttributes.merge(textAttributes1, textAttributes2)
            val offset = element.startOffset + ParadoxExpressionService.getExpressionOffset(element)
            val range = node.rangeInExpression.shiftRight(offset)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).enforcedTextAttributes(textAttributes).create()
            return
        }

        annotateExpression(element, node.rangeInExpression, holder, attributesKey)
    }

    // endregion

    // region Resolve Methods

    fun resolveModifier(element: ParadoxExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null // NOTE 1.4.0 - unnecessary to support yet
        return ParadoxModifierManager.resolveModifier(name, element, configGroup)
    }

    fun resolveEnumValue(element: ParadoxExpressionElement, expression: String, config: CwtConfig<*>): PsiElement? {
        resolveStaticEnumValue(expression, config)?.let { return it }
        resolveComplexEnumValue(element, expression, config)?.let { return it }
        return null
    }

    fun resolveStaticEnumValue(expression: String, config: CwtConfig<*>): PsiElement? {
        val dataExpression = config.configExpression ?: return null
        if (dataExpression.type != CwtDataTypes.EnumValue) return null
        val name = expression
        val enumName = dataExpression.metadata.value ?: return null
        val configGroup = config.configGroup
        val enumConfig = configGroup.enums[enumName] ?: return null
        val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
        val resolved = enumValueConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    fun resolveComplexEnumValue(element: ParadoxExpressionElement, expression: String, config: CwtConfig<*>): PsiElement? {
        val dataExpression = config.configExpression ?: return null
        if (dataExpression.type != CwtDataTypes.EnumValue) return null
        val name = expression
        val enumName = dataExpression.metadata.value ?: return null
        val configGroup = config.configGroup
        val complexEnumConfig = configGroup.complexEnums[enumName] ?: return null
        val project = configGroup.project
        val searchScopeType = complexEnumConfig.searchScopeType
        val selector = ParadoxComplexEnumValueSearch.selector(project, element).withSearchScopeType(searchScopeType)
        val info = ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() ?: return null
        val readWriteAccess = ReadWriteAccess.Read // usage
        return ParadoxComplexEnumValueLightElement(element, info.name, info.enumName, readWriteAccess, info.gameType, project)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, expression: String, config: CwtConfig<*>): PsiElement? {
        val dataExpression = config.configExpression ?: return null
        if (dataExpression.type !in CwtDataTypeSets.DynamicValue) return null
        val name = expression
        val configGroup = config.configGroup
        return ParadoxDynamicValueManager.resolveDynamicValue(element, name, dataExpression, configGroup)
    }

    fun resolveShaderEffect(element: ParadoxExpressionElement, expression: String, configGroup: CwtConfigGroup): PsiElement {
        val name = expression
        return ParadoxShaderEffectLightElement(element, name, configGroup.gameType, configGroup.project)
    }

    fun resolveMeshLocator(element: ParadoxExpressionElement, expression: String, configGroup: CwtConfigGroup): PsiElement {
        val name = expression
        return ParadoxMeshLocatorLightElement(element, name, configGroup.gameType, configGroup.project)
    }

    @Suppress("unused")
    fun resolveSystemScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val systemScopeConfig = configGroup.systemScopes[name] ?: return null
        val resolved = systemScopeConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.type.forScope() && it.isStatic } ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolveValueField(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.type.forValue() && it.isStatic } ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolvePredefinedLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.localisationLinks[name] ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolvePredefinedLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val commandConfig = configGroup.localisationCommands[name] ?: return null
        val resolved = commandConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    // endregion
}
