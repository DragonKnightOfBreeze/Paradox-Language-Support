package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyWithLevelScriptExpressionSupport : ParadoxScriptExpressionSupport {
    //https://github.com/cwtools/cwtools-vscode/issues/58
    
    private val typeExpression = "<technology.repeatable>"
    
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.TechnologyWithLevel
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val separatorIndex = expression.indexOf('@')
        if(separatorIndex == -1) return
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        run {
            val offset = separatorIndex
            if(offset <= 0) return@run
            val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
            val range1 = range.let { TextRange.create(it.startOffset, it.startOffset + offset) }
            CwtConfigHandler.annotateScriptExpression(element, range1, attributesKey, holder)
        }
        run {
            val offset = separatorIndex
            val attributesKey = ParadoxScriptAttributesKeys.MARKER_KEY
            val range2 = range.let { TextRange.create(it.startOffset + offset, it.startOffset + offset + 1) }
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range2).textAttributes(attributesKey).create()
        }
        run {
            val offset = expression.length - separatorIndex - 1
            if(offset <= 0) return@run
            //annotate only if snippet after '@' is number like
            if(!expression.substring(separatorIndex + 1).all { it.isExactDigit() }) return@run
            val attributesKey = ParadoxScriptAttributesKeys.NUMBER_KEY
            val range3 = range.let { TextRange.create(it.endOffset - offset, it.endOffset) }
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range3).textAttributes(attributesKey).create()
        }
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val separatorIndex = expression.indexOf('@')
        if(separatorIndex == -1) return PsiReference.EMPTY_ARRAY
        val range = rangeInElement ?: TextRange.create(0, expression.length).unquote(expression)
        val offset = separatorIndex
        val range1 = range.let { TextRange.create(it.startOffset, it.startOffset + offset) }
        if(range1.isEmpty) return PsiReference.EMPTY_ARRAY
        val config1 = CwtValueConfig.resolve(emptyPointer(), config.configGroup, typeExpression)
        val reference = ParadoxScriptExpressionPsiReference(element, range1, config1, null)
        return arrayOf(reference)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val definitionScriptExpressionSupport = ParadoxScriptExpressionSupport.EP_NAME.findExtension(ParadoxDefinitionScriptExpressionSupport::class.java) ?: return
        
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val separatorIndex = keyword.indexOf('@')
        if(separatorIndex != -1 && keywordOffset - separatorIndex > 0) return
        
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