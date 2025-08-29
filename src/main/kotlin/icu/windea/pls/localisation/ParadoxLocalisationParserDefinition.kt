package icu.windea.pls.localisation

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST_LINE_BREAK
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST_NOT
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.TokenType
import icu.windea.pls.localisation.lexer.ParadoxLocalisationLexerFactory
import icu.windea.pls.localisation.parser.ParadoxLocalisationParser
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.AT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLON
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLOR_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMA
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMENT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.Factory
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LOCALE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_END
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_START
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PIPE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets

open class ParadoxLocalisationParserDefinition : ParserDefinition {
    override fun getWhitespaceTokens() = ParadoxLocalisationTokenSets.WHITE_SPACES

    override fun getCommentTokens() = ParadoxLocalisationTokenSets.COMMENTS

    override fun getStringLiteralElements() = ParadoxLocalisationTokenSets.STRING_LITERALS

    override fun getFileNodeType() = ParadoxLocalisationFile.ELEMENT_TYPE

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
