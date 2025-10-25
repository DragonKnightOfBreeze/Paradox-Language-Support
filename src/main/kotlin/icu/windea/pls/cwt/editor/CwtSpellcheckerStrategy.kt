package icu.windea.pls.cwt.editor

import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.spellchecker.inspections.IdentifierSplitter
import com.intellij.spellchecker.inspections.PlainTextSplitter
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import icu.windea.pls.core.containsBlank
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtStringExpressionElement

// com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
// com.intellij.spellchecker.JavaSpellcheckingStrategy
// org.jetbrains.kotlin.idea.spellchecker.KotlinSpellcheckingStrategy

class CwtSpellcheckerStrategy : SpellcheckingStrategy(), DumbAware {
    private val emptyTokenizer = EMPTY_TOKENIZER
    private val blankAwareTokenizer = BlankAwareTokenizer()

    override fun getTokenizer(element: PsiElement?): Tokenizer<*> {
        if (element is PsiWhiteSpace) return emptyTokenizer
        if (isInjectedLanguageFragment(element)) return emptyTokenizer
        return when (element) {
            is CwtOptionComment -> emptyTokenizer
            is PsiComment -> super.getTokenizer(element)
            is CwtOptionKey -> blankAwareTokenizer
            is CwtStringExpressionElement -> blankAwareTokenizer
            else -> emptyTokenizer
        }
    }

    private class BlankAwareTokenizer : Tokenizer<PsiElement>() {
        override fun tokenize(element: PsiElement, consumer: TokenConsumer) {
            if (element.text.containsBlank()) {
                consumer.consumeToken(element, PlainTextSplitter.getInstance())
            } else {
                consumer.consumeToken(element, IdentifierSplitter.getInstance())
            }
        }
    }
}
