package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors

abstract class ParadoxExternalReferenceScriptExpressionSupport : ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.ShaderEffect
     */
    class ForShaderEffect : ParadoxExternalReferenceScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.ShaderEffect

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.SHADER_EFFECT_REFERENCE
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement {
            val configGroup = config.configGroup
            return ParadoxExpressionSupportFactory.resolveShaderEffect(element, text, configGroup)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeShaderEffect(context, result)
        }
    }

    /**
     * @see CwtDataTypes.MeshLocator
     */
    class ForMeshLocator : ParadoxExternalReferenceScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.MeshLocator

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.MESH_LOCATOR_REFERENCE
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement {
            val configGroup = config.configGroup
            return ParadoxExpressionSupportFactory.resolveMeshLocator(element, text, configGroup)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeMeshLocator(context, result)
        }
    }
}
