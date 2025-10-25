package icu.windea.pls.script.editor

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.spellchecker.inspections.IdentifierSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import icu.windea.pls.core.children
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

// com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
// com.intellij.spellchecker.JavaSpellcheckingStrategy
// org.jetbrains.kotlin.idea.spellchecker.KotlinSpellcheckingStrategy

class ParadoxScriptSpellcheckerStrategy : SpellcheckingStrategy(),DumbAware {
    private val emptyTokenizer = EMPTY_TOKENIZER
    private val scriptedVariableNameTokenizer = TokenAwareTokenizer(SCRIPTED_VARIABLE_NAME_TOKEN)
    private val scriptedVariableReferenceTokenizer = TokenAwareTokenizer(SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    private val parameterTokenizer = TokenAwareTokenizer(PARAMETER_TOKEN)
    private val conditionParameterTokenizer = TokenAwareTokenizer(CONDITION_PARAMETER_TOKEN)

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        if (element is PsiWhiteSpace) return emptyTokenizer
        if (isInjectedLanguageFragment(element)) return emptyTokenizer
        return when (element) {
            is PsiComment -> super.getTokenizer(element)
            is ParadoxScriptScriptedVariableName -> scriptedVariableNameTokenizer
            is ParadoxScriptedVariableReference -> scriptedVariableReferenceTokenizer
            is ParadoxScriptStringExpressionElement -> emptyTokenizer // 目前不做检查
            is ParadoxParameter -> parameterTokenizer
            is ParadoxConditionParameter -> conditionParameterTokenizer
            else -> emptyTokenizer
        }
    }

    private class TokenAwareTokenizer(
        private val tokenType: IElementType,
        private val useRename: Boolean = true,
    ) : Tokenizer<PsiElement>() {
        override fun tokenize(element: PsiElement, consumer: TokenConsumer) {
            val tokens = element.children().filter { it.elementType == tokenType }
            for (token in tokens) {
                val range = token.textRange
                if (range.isEmpty) continue
                val text = token.text
                if (!text.isIdentifier()) continue
                val offset = range.startOffset - element.textRange.startOffset
                if (offset < 0) continue
                consumer.consumeToken(element, text, useRename, offset, TextRange.allOf(text), IdentifierSplitter.getInstance())
            }
        }
    }
}
