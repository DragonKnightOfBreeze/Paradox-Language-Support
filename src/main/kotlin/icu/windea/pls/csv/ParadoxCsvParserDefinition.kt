package icu.windea.pls.csv

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST_LINE_BREAK
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import icu.windea.pls.csv.lexer.ParadoxCsvLexerFactory
import icu.windea.pls.csv.parser.ParadoxCsvParser
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COMMENT
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.Factory
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.HEADER
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.ROW
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvTokenSets

class ParadoxCsvParserDefinition : ParserDefinition {
    override fun getWhitespaceTokens() = ParadoxCsvTokenSets.WHITE_SPACES

    override fun getCommentTokens() = ParadoxCsvTokenSets.COMMENTS

    override fun getStringLiteralElements() = ParadoxCsvTokenSets.STRING_LITERALS

    override fun getFileNodeType() = ParadoxCsvFile.ELEMENT_TYPE

    override fun createFile(viewProvider: FileViewProvider) = ParadoxCsvFile(viewProvider)

    override fun createElement(node: ASTNode) = Factory.createElement(node)

    override fun createParser(project: Project?) = ParadoxCsvParser()

    override fun createLexer(project: Project?) = ParadoxCsvLexerFactory.createLexer(project)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT || rightType == COMMENT -> MUST_LINE_BREAK
            leftType == HEADER || rightType == HEADER -> MUST_LINE_BREAK
            leftType == ROW || rightType == ROW -> MUST_LINE_BREAK
            else -> MAY
        }
    }
}
