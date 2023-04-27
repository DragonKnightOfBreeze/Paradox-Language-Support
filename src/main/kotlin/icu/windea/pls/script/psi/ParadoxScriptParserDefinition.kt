package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

class ParadoxScriptParserDefinition : ParserDefinition {
    companion object {
        val FILE = ParadoxScriptStubElementTypes.FILE
    }
    
    override fun getWhitespaceTokens() = ParadoxScriptTokenSets.WHITE_SPACES
    
    override fun getCommentTokens() = ParadoxScriptTokenSets.COMMENTS
    
    override fun getStringLiteralElements() = ParadoxScriptTokenSets.STRING_LITERALS
    
    override fun getFileNodeType() = FILE
    
    override fun createFile(viewProvider: FileViewProvider): ParadoxScriptFile {
        return ParadoxScriptFile(viewProvider)
    }
    
    override fun createElement(node: ASTNode): PsiElement {
        return when(node.elementType) {
            PROPERTY -> SmartParadoxScriptProperty(node)
            PROPERTY_KEY -> SmartParadoxScriptPropertyKey(node)
            STRING -> SmartParadoxScriptString(node)
            else -> Factory.createElement(node)
        }
    }
    
    override fun createParser(project: Project?): ParadoxScriptParser {
        return ParadoxScriptParser()
    }
    
    override fun createLexer(project: Project?): ParadoxScriptLexerAdapter {
        return ParadoxScriptLexerAdapter()
    }
    
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        val leftType = left?.elementType
        val rightType = right?.elementType
        return when {
            leftType == COMMENT -> MUST_LINE_BREAK
            leftType == AT && rightType == SCRIPTED_VARIABLE_NAME_ID -> MUST_NOT
            leftType == AT && rightType == SCRIPTED_VARIABLE_REFERENCE_ID -> MUST_NOT
            leftType == PARAMETER_START || rightType == PARAMETER_END -> MUST_NOT
            leftType == KEY_STRING_SNIPPET || rightType == KEY_STRING_SNIPPET -> MUST_NOT
            leftType == VALUE_STRING_SNIPPET || rightType == VALUE_STRING_SNIPPET -> MUST_NOT
            leftType == PIPE || rightType == PIPE -> MUST_NOT
            else -> MAY
        }
    }
}
