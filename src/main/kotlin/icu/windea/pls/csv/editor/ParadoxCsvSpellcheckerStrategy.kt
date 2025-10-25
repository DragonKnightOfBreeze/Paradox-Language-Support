package icu.windea.pls.csv.editor

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
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement

// com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
// com.intellij.spellchecker.JavaSpellcheckingStrategy
// org.jetbrains.kotlin.idea.spellchecker.KotlinSpellcheckingStrategy

class ParadoxCsvSpellcheckerStrategy : SpellcheckingStrategy(),DumbAware {
    val emptyTokenizer = EMPTY_TOKENIZER
    private val blankAwareTokenizer = BlankAwareTokenizer()

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        if (element is PsiWhiteSpace) return emptyTokenizer
        if (isInjectedLanguageFragment(element)) return emptyTokenizer
        return when (element) {
            is PsiComment -> super.getTokenizer(element)
            is ParadoxCsvExpressionElement -> blankAwareTokenizer
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
