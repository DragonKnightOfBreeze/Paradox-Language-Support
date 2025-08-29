package icu.windea.pls.script

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST_LINE_BREAK
import com.intellij.lang.ParserDefinition.SpaceRequirements.MUST_NOT
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.script.lexer.ParadoxScriptLexer
import icu.windea.pls.script.parser.ParadoxScriptParser
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.AT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COMMENT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.Factory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PIPE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

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
