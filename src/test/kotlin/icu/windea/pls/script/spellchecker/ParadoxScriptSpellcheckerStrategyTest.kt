package icu.windea.pls.script.spellchecker

import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxScriptSpellcheckerStrategy
 */
@RunWith(JUnit4::class)
class ParadoxScriptSpellcheckerStrategyTest {
    @Test
    fun argumentTokenizer_usesArgumentToken() {
        val strategy = ParadoxScriptSpellcheckerStrategy()
        val tokenizerField = ParadoxScriptSpellcheckerStrategy::class.java.getDeclaredField("argumentTokenizer")
            .apply { isAccessible = true }
        val tokenizer = tokenizerField.get(strategy)
        val tokenTypeField = tokenizer.javaClass.getDeclaredField("tokenType").apply { isAccessible = true }
        Assert.assertSame(ParadoxScriptElementTypes.ARGUMENT_TOKEN, tokenTypeField.get(tokenizer))
    }
}
