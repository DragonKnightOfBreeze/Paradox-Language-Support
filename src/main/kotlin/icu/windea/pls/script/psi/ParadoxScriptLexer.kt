package icu.windea.pls.script.psi

import com.intellij.lexer.*
import com.intellij.psi.tree.*

class ParadoxScriptLexer : MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer()), TOKENS_TO_MERGE) {
    companion object {
        private val TOKENS_TO_MERGE = TokenSet.create(
            ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN,
            ParadoxScriptElementTypes.STRING_TOKEN,
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_TOKEN,
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
        )
    }
    
    override fun getMergeFunction(): MergeFunction {
        return MergeFunction { type, originalLexer ->
            if(type !in TOKENS_TO_MERGE) {
                type
            } else {
                while(true) {
                    val tokenType = originalLexer!!.tokenType
                    if(tokenType !== type) break
                    if(originalLexer.bufferSequence.get(originalLexer.tokenStart -1) == '"') break //cannot merge quoted literals
                    if(originalLexer.bufferSequence.get(originalLexer.tokenStart) == '"') break //cannot merge quoted literals
                    originalLexer.advance()
                }
                type
            }
        }
    }
}