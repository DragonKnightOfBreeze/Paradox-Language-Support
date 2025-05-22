package icu.windea.pls.localisation

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.localisation.lexer.*
import icu.windea.pls.localisation.parser.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

open class ParadoxLocalisationParserDefinition : ParserDefinition {
    override fun getWhitespaceTokens() = ParadoxLocalisationTokenSets.WHITE_SPACES

    override fun getCommentTokens() = ParadoxLocalisationTokenSets.COMMENTS

    override fun getStringLiteralElements() = ParadoxLocalisationTokenSets.STRING_LITERALS

    override fun getFileNodeType() = ParadoxLocalisationFileStubElementType.INSTANCE

    override fun createFile(viewProvider: FileViewProvider) = ParadoxLocalisationFile(viewProvider)

    override fun createElement(node: ASTNode) = Factory.createElement(node)

    override fun createParser(project: Project?) = ParadoxLocalisationParser()

    override fun createLexer(project: Project?) = ParadoxLocalisationLexerFactory.createLexer(project)

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT -> MUST_LINE_BREAK
            leftType == LOCALE_TOKEN && rightType == COLON -> MUST_NOT
            rightType == LOCALE_TOKEN -> MUST_LINE_BREAK
            rightType == PROPERTY_KEY_TOKEN -> MUST_LINE_BREAK
            leftType == COLORFUL_TEXT_START && rightType == COLOR_TOKEN -> MUST_NOT
            leftType == PARAMETER_START || rightType == PARAMETER_END -> MUST_NOT
            leftType == AT && rightType == SCRIPTED_VARIABLE_REFERENCE_TOKEN -> MUST_NOT
            leftType == ICON_START || rightType == ICON_END -> MUST_NOT
            leftType == PIPE || rightType == PIPE -> MUST_NOT
            leftType == COMMA && rightType != TokenType.WHITE_SPACE -> MUST // [stellaris] localisation concept
            leftType == TEXT_FORMAT_TOKEN && rightType != TokenType.WHITE_SPACE -> MUST // [ck3, vic3] localisation text format
            else -> MAY
        }
    }
}
