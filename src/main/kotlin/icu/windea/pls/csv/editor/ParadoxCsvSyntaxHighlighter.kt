package icu.windea.pls.csv.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN
import com.intellij.psi.StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN
import com.intellij.psi.TokenType.BAD_CHARACTER
import com.intellij.psi.tree.IElementType
import icu.windea.pls.csv.lexer.ParadoxCsvLexerFactory
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COLUMN_TOKEN
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COMMENT
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.SEPARATOR
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.csv.editor.ParadoxCsvAttributesKeyArrays as KA

class ParadoxCsvSyntaxHighlighter(
    private val project: Project?,
    private val virtualFile: VirtualFile?
) : SyntaxHighlighter {
    override fun getTokenHighlights(tokenType: IElementType?) = when (tokenType) {
        SEPARATOR -> KA.SEPARATOR_KEYS
        COMMENT -> KA.COMMENT_KEYS
        COLUMN_TOKEN -> KA.STRING_KEYS
        VALID_STRING_ESCAPE_TOKEN -> KA.VALID_ESCAPE_KEYS
        INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_UNICODE_ESCAPE_TOKEN -> KA.INVALID_ESCAPE_KEYS
        BAD_CHARACTER -> KA.BAD_CHARACTER_KEYS
        else -> KA.EMPTY_KEYS
    }

    override fun getHighlightingLexer() = ParadoxCsvLexerFactory.createHighlightingLexer(project, selectGameType(virtualFile))
}
