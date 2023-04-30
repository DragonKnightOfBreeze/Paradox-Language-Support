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
            leftType == AT && rightType == SCRIPTED_VARIABLE_NAME_TOKEN -> MUST_NOT
            leftType == AT && rightType == SCRIPTED_VARIABLE_REFERENCE_TOKEN -> MUST_NOT
            leftType == PARAMETER_END && isParameterAwareType(right) -> MUST_NOT
            rightType == PARAMETER_START && isParameterAwareType(left) -> MUST_NOT
            leftType == PARAMETER_START || rightType == PARAMETER_END -> MUST_NOT
            leftType == PIPE || rightType == PIPE -> MUST_NOT
            else -> MAY
        }
    }
    
    private fun isParameterAwareType(node: ASTNode?) : Boolean {
        val parentType = node?.treeParent?.elementType ?: return false
        return parentType == SCRIPTED_VARIABLE_NAME
            || parentType == SCRIPTED_VARIABLE_REFERENCE
            || parentType == PROPERTY_KEY 
            || parentType == STRING
    }
}
