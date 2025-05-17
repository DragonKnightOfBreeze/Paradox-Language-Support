package icu.windea.pls.localisation.lexer

import com.intellij.lexer.*
import com.intellij.psi.*
import com.intellij.psi.tree.*

class ParadoxLocalisationStringLiteralLexer(
    originalLiteralToken: IElementType
) : StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "$£§#", false, false) {
    override fun getTokenType(): IElementType? {
        if (myStart >= myEnd) return null

        //handle double left brackets '[['
        if (myStart < myBufferEnd - 1 && myBuffer[myStart] == '[' && myBuffer[myStart + 1] == '[') {
            return StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
        }
        return super.getTokenType()
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        super.start(buffer, startOffset, endOffset, initialState)
        locateToken()
    }

    override fun advance() {
        super.advance()
        locateToken()
    }

    private fun locateToken() {
        if (myEnd != myBufferEnd) return

        //handle double left brackets '[['
        var i = myStart
        if (i < myBufferEnd - 1 && myBuffer[i] == '[' && myBuffer[i + 1] == '[') {
            myEnd = i + 2
            return
        }
        while (i < myBufferEnd - 1) {
            if (myBuffer[i] == '[') {
                myEnd = i
                return
            }
            i++
        }
    }
}
