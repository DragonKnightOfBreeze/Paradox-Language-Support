package icu.windea.pls.expression.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.expression.*
import icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*

class ParadoxExpressionParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType("PARADOX_EXPRESSION_FILE", ParadoxExpressionLanguage)
    }
    
    override fun getWhitespaceTokens() = ParadoxExpressionTokenSets.WHITE_SPACES
    
    override fun getCommentTokens() = ParadoxExpressionTokenSets.COMMENTS
    
    override fun getStringLiteralElements() = ParadoxExpressionTokenSets.STRING_LITERALS
    
    override fun getFileNodeType() = FILE
    
    override fun createFile(viewProvider: FileViewProvider): ParadoxExpressionFile {
        return ParadoxExpressionFile(viewProvider)
    }
    
    override fun createElement(node: ASTNode): PsiElement {
        return  Factory.createElement(node)
    }
    
    override fun createParser(project: Project?): ParadoxExpressionParser {
        return ParadoxExpressionParser()
    }
    
    override fun createLexer(project: Project?): ParadoxExpressionLexerAdapter {
        return ParadoxExpressionLexerAdapter()
    }
    
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
        return MUST_NOT
    }
}
