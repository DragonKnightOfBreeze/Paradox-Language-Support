package icu.windea.pls.script

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.script.lexer.*
import icu.windea.pls.script.parser.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptParserDefinition : ParserDefinition {
    override fun getWhitespaceTokens() = ParadoxScriptTokenSets.WHITE_SPACES

    override fun getCommentTokens() = ParadoxScriptTokenSets.COMMENTS

    override fun getStringLiteralElements() = ParadoxScriptTokenSets.STRING_LITERALS

    override fun getFileNodeType() = ParadoxScriptFile.ELEMENT_TYPE

    override fun createFile(viewProvider: FileViewProvider): ParadoxScriptFile {
        return ParadoxScriptFile(viewProvider)
    }

    override fun createElement(node: ASTNode): PsiElement {
        return Factory.createElement(node)
    }

    override fun createParser(project: Project?): ParadoxScriptParser {
        return ParadoxScriptParser()
    }

    override fun createLexer(project: Project?): ParadoxScriptLexer {
        return ParadoxScriptLexer()
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT -> MUST_LINE_BREAK
            leftType == AT && rightType == SCRIPTED_VARIABLE_NAME_TOKEN -> MUST_NOT
            leftType == AT && rightType == SCRIPTED_VARIABLE_REFERENCE_TOKEN -> MUST_NOT
            leftType == PIPE || rightType == PIPE -> MUST_NOT
            else -> MAY
        }
    }
}
