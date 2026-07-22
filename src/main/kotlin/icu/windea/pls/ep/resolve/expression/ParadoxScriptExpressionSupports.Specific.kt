package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// Game Type Specific

/**
 * @see CwtDataTypes.TechnologyWithLevel
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxScriptTechnologyWithLevelExpressionSupport : ParadoxScriptExpressionSupportBase() {
    // https://github.com/cwtools/cwtools-vscode/issues/58

    private val typeExpression = "<technology.repeatable>"

    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.TechnologyWithLevel
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val separatorIndex = text.indexOf('@')
        if (separatorIndex == -1) return
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        run {
            val offset = separatorIndex
            if (offset <= 0) return@run
            val attributesKey = ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
            val range1 = range.let { TextRange.create(it.startOffset, it.startOffset + offset) }
            ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range1, attributesKey, holder)
        }
        run {
            val offset = separatorIndex
            val attributesKey = ParadoxScriptHighlighterColors.SEMANTIC_MARKER
            val range2 = range.let { TextRange.create(it.startOffset + offset, it.startOffset + offset + 1) }
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range2).textAttributes(attributesKey).create()
        }
        run {
            val offset = text.length - separatorIndex - 1
            if (offset <= 0) return@run
            // annotate only if snippet after '@' is number like
            if (!text.substring(separatorIndex + 1).all { it.isExactDigit() }) return@run
            val attributesKey = ParadoxScriptHighlighterColors.NUMBER
            val range3 = range.let { TextRange.create(it.endOffset - offset, it.endOffset) }
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range3).textAttributes(attributesKey).create()
        }
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
        if (element !is ParadoxScriptStringExpressionElement) return emptyList()
        val separatorIndex = expressionText.indexOf('@')
        if (separatorIndex == -1) return emptyList() // no `@` -> ignore
        if (separatorIndex == 0) return emptyList() // no tech node -> ignore
        val range = rangeInElement ?: TextRange.create(0, expressionText.length).unquote(expressionText)
        val referenceRange = range.let { TextRange.create(it.startOffset, it.startOffset + separatorIndex) }
        val referenceConfigs = listOf(CwtValueConfig.createMock(config.configGroup, typeExpression))
        val referenceRole = ParadoxExpressionRole.Other
        val reference = ParadoxScriptExpressionPsiReference(element, referenceRange, referenceConfigs, referenceRole)
        return reference.to.singletonList()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionScriptExpressionSupport = ParadoxScriptExpressionSupport.EP_NAME.findExtension(ParadoxScriptDefinitionExpressionSupport::class.java) ?: return

        val separatorIndex = context.keyword.indexOf('@')
        if (separatorIndex != -1 && context.keywordOffset - separatorIndex > 0) return

        val config = CwtValueConfig.createMock(context.configGroup, typeExpression)
        val context = context.copy(isKey = null, config = config, configs = emptySet())
        definitionScriptExpressionSupport.complete(context, result)
    }
}
