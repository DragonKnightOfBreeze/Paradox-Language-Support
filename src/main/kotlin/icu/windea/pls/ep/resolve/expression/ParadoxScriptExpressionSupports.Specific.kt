package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.util.ParadoxAnnotateUtil
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// Game Type Specific

/**
 * @see CwtDataTypes.TechnologyWithLevel
 */
@ForGameType(ParadoxGameType.Stellaris)
class ParadoxScriptTechnologyWithLevelExpressionSupport : ParadoxScriptExpressionSupport {
    // https://github.com/cwtools/cwtools-vscode/issues/58

    private val typeExpression = "<technology.repeatable>"

    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.TechnologyWithLevel

    override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val separatorIndex = text.indexOf('@')
        if (separatorIndex == -1) return
        run {
            val offset = separatorIndex
            if (offset <= 0) return@run
            val attributesKey = ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
            val rangeInExpression = TextRange.create(rangeInExpression.startOffset, rangeInExpression.startOffset + offset)
            ParadoxAnnotateUtil.annotateExpression(element, rangeInExpression, holder, attributesKey)
        }
        run {
            val offset = separatorIndex
            val attributesKey = ParadoxScriptHighlighterColors.SEMANTIC_MARKER
            val rangeInExpression = TextRange.create(rangeInExpression.startOffset + offset, rangeInExpression.startOffset + offset + 1)
            ParadoxAnnotateUtil.annotateExpression(element, rangeInExpression, holder, attributesKey)
        }
        run {
            val offset = text.length - separatorIndex - 1
            if (offset <= 0) return@run
            // annotate only if snippet after '@' is number like
            if (!text.substring(separatorIndex + 1).all { it.isExactDigit() }) return@run
            val attributesKey = ParadoxScriptHighlighterColors.NUMBER
            val rangeInExpression = TextRange.create(rangeInExpression.endOffset - offset, rangeInExpression.endOffset)
            ParadoxAnnotateUtil.annotateExpression(element, rangeInExpression, holder, attributesKey)
        }
    }

    override fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
        if (element !is ParadoxScriptStringExpressionElement) return emptyList()
        val separatorIndex = text.indexOf('@')
        if (separatorIndex == -1) return emptyList() // no `@` -> ignore
        if (separatorIndex == 0) return emptyList() // no tech node -> ignore
        val offset = ParadoxExpressionService.getExpressionOffset(element)
        val referenceRange = TextRange.from(rangeInExpression.startOffset + offset, separatorIndex)
        val referenceConfigs = listOf(CwtValueConfig.mock(config.configGroup, typeExpression))
        val referenceRole = ParadoxExpressionRole.Other
        val reference = ParadoxScriptExpressionPsiReference(element, referenceRange, referenceConfigs, referenceRole)
        return reference.to.singletonList()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionScriptExpressionSupport = ParadoxScriptExpressionSupport.EP_NAME.findExtension(ParadoxScriptDefinitionExpressionSupport::class.java) ?: return

        val separatorIndex = context.keyword.indexOf('@')
        if (separatorIndex != -1 && context.keywordOffset - separatorIndex > 0) return

        val config = CwtValueConfig.mock(context.configGroup, typeExpression)
        val context = context.copy(isKey = null, config = config, configs = emptySet())
        definitionScriptExpressionSupport.complete(context, result)
    }
}
