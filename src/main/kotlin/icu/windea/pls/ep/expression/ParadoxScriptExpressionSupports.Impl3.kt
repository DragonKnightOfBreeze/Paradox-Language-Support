package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.configs
import icu.windea.pls.lang.codeInsight.completion.isKey
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.keywordOffset
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxScriptTechnologyWithLevelExpressionSupport : ParadoxScriptExpressionSupport {
    //https://github.com/cwtools/cwtools-vscode/issues/58

    private val typeExpression = "<technology.repeatable>"

    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.TechnologyWithLevel
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val separatorIndex = expressionText.indexOf('@')
        if (separatorIndex == -1) return
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        run {
            val offset = separatorIndex
            if (offset <= 0) return@run
            val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
            val range1 = range.let { TextRange.create(it.startOffset, it.startOffset + offset) }
            ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range1, attributesKey, holder)
        }
        run {
            val offset = separatorIndex
            val attributesKey = ParadoxScriptAttributesKeys.MARKER_KEY
            val range2 = range.let { TextRange.create(it.startOffset + offset, it.startOffset + offset + 1) }
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range2).textAttributes(attributesKey).create()
        }
        run {
            val offset = expressionText.length - separatorIndex - 1
            if (offset <= 0) return@run
            //annotate only if snippet after '@' is number like
            if (!expressionText.substring(separatorIndex + 1).all { it.isExactDigit() }) return@run
            val attributesKey = ParadoxScriptAttributesKeys.NUMBER_KEY
            val range3 = range.let { TextRange.create(it.endOffset - offset, it.endOffset) }
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range3).textAttributes(attributesKey).create()
        }
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val separatorIndex = expressionText.indexOf('@')
        if (separatorIndex == -1) return PsiReference.EMPTY_ARRAY
        val range = rangeInElement ?: TextRange.create(0, expressionText.length).unquote(expressionText)
        val offset = separatorIndex
        val range1 = range.let { TextRange.create(it.startOffset, it.startOffset + offset) }
        if (range1.isEmpty) return PsiReference.EMPTY_ARRAY
        val config1 = CwtValueConfig.resolve(emptyPointer(), config.configGroup, typeExpression)
        val reference = ParadoxScriptExpressionPsiReference(element, range1, config1, null)
        return arrayOf(reference)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val definitionScriptExpressionSupport = ParadoxScriptExpressionSupport.EP_NAME.findExtension(ParadoxScriptDefinitionExpressionSupport::class.java) ?: return

        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val separatorIndex = keyword.indexOf('@')
        if (separatorIndex != -1 && keywordOffset - separatorIndex > 0) return

        val config = context.config ?: return
        val configs = context.configs
        val isKey = context.isKey

        val config1 = CwtValueConfig.resolve(emptyPointer(), config.configGroup, typeExpression)
        context.config = config1
        context.configs = emptySet()
        context.isKey = null
        definitionScriptExpressionSupport.complete(context, result)

        context.config = config
        context.configs = configs
        context.isKey = isKey
    }
}
