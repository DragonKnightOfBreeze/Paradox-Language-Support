package icu.windea.pls.csv

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.csv.lexer.*
import icu.windea.pls.csv.parser.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

class ParadoxCsvParserDefinition : ParserDefinition {
    override fun getWhitespaceTokens() = ParadoxCsvTokenSets.WHITE_SPACES

    override fun getCommentTokens() = ParadoxCsvTokenSets.COMMENTS

    override fun getStringLiteralElements() = ParadoxCsvTokenSets.STRING_LITERALS

    override fun getFileNodeType() = ParadoxCsvFileStubElementType.INSTANCE

    override fun createFile(viewProvider: FileViewProvider) = ParadoxCsvFile(viewProvider)

    override fun createElement(node: ASTNode) = Factory.createElement(node)

    override fun createParser(project: Project?) = ParadoxCsvParser()

    override fun createLexer(project: Project?) = ParadoxCsvLexerFactory.createLexer(project)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT || rightType == COMMENT -> MUST_LINE_BREAK
            leftType == ROW || rightType == ROW -> MUST_LINE_BREAK
            else -> MAY
        }
    }
}
