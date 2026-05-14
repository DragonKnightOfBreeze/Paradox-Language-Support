package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxResolutionManager
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors

// Extra

/**
 * @see CwtDataTypes.ShaderEffect
 */
class ParadoxShaderEffectExpressionSupport: ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.ShaderEffect
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptHighlighterColors.SHADER_EFFECT_REFERENCE
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement {
        val configGroup = config.configGroup
        return ParadoxResolutionManager.resolveShaderEffect(element, expressionText, configGroup)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxCompletionManager.completeShaderEffect(context, result)
    }
}

/**
 * @see CwtDataTypes.MeshLocator
 */
class ParadoxMeshLocatorExpressionSupport: ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.MeshLocator
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptHighlighterColors.MESH_LOCATOR_REFERENCE
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement {
        val configGroup = config.configGroup
        return ParadoxResolutionManager.resolveMeshLocator(element, expressionText, configGroup)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxCompletionManager.completeMeshLocator(context, result)
    }
}
