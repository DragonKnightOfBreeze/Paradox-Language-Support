package icu.windea.pls.cwt

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST_LINE_BREAK
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import icu.windea.pls.cwt.lexer.CwtLexerFactory
import icu.windea.pls.cwt.parser.CwtParser
import icu.windea.pls.cwt.psi.CwtElementTypes.COMMENT
import icu.windea.pls.cwt.psi.CwtElementTypes.DOC_COMMENT
import icu.windea.pls.cwt.psi.CwtElementTypes.Factory
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_COMMENT
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtTokenSets

class CwtParserDefinition : ParserDefinition {
    override fun getWhitespaceTokens() = CwtTokenSets.WHITE_SPACES

    override fun getCommentTokens() = CwtTokenSets.COMMENTS

    override fun getStringLiteralElements() = CwtTokenSets.STRING_LITERALS

    override fun getFileNodeType() = CwtFile.ELEMENT_TYPE

    override fun createFile(viewProvider: FileViewProvider) = CwtFile(viewProvider)

    override fun createElement(node: ASTNode?) = Factory.createElement(node)

    override fun createParser(project: Project?) = CwtParser()

    override fun createLexer(project: Project?) = CwtLexerFactory.createLexer(project)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT -> MUST_LINE_BREAK
            leftType == OPTION_COMMENT || rightType == OPTION_COMMENT -> MUST_LINE_BREAK
            leftType == DOC_COMMENT || rightType == DOC_COMMENT -> MUST_LINE_BREAK
            else -> MAY
        }
    }
}
